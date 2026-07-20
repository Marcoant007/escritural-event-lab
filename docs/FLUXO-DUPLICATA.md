# Como funciona o fluxo de uma duplicata escritural

Este documento explica, passo a passo, o que acontece — no mundo real e por dentro do sistema — em cada etapa do ciclo de vida de uma duplicata. É o material de apoio pra quem quer entender o domínio testando na prática (via Bruno, psql e Kafka UI).

## O que é uma duplicata escritural (e por que não é um boleto)

Duplicata é um **título de crédito**, não um mero instrumento de cobrança. Ela só existe porque houve uma venda mercantil a prazo (ou prestação de serviço a prazo) de verdade: o vendedor entregou a mercadoria ou prestou o serviço, e extrai a duplicata da própria nota fiscal dessa venda. É esse vínculo com uma transação real que dá à duplicata força jurídica — ela pode ser endossada, descontada em banco, levada a protesto.

Um **boleto**, por comparação, não tem essa exigência: serve pra cobrar qualquer coisa (conta de luz, mensalidade, qualquer dívida), não representa por si só um título de crédito, e não existe "aceite" de boleto — você paga ou não paga, ponto.

É exatamente esse "aceite" que marca a diferença. O comprador (sacado) não está só confirmando que concorda em pagar uma cobrança qualquer — ele está reconhecendo formalmente que **recebeu a mercadoria (ou o serviço) conforme negociado**. Se a mercadoria não chegou, veio com defeito, ou a quantidade/preço não bate com o que foi combinado, ele tem base legal pra recusar. A versão "escritural" da duplicata só existe de forma eletrônica, registrada num sistema — é esse registro que este laboratório simula, de forma simplificada (sem valor jurídico real, sem NF de fato vinculada — ver `docs/DOMAIN.md` pra saber o que foi deixado de fora de propósito).

Dois lados envolvidos:

- **Sacador** (`issuerDocument`) — quem vendeu/prestou o serviço, quem tem o direito de receber.
- **Sacado** (`payerDocument`) — quem comprou, quem deve pagar (ou recusar, se a transação não aconteceu como negociado).

## Ciclo de vida implementado hoje

```text
ISSUED (emitida) ──apresentar──> PRESENTED (apresentada) ──aceitar──> ACCEPTED (aceita)
                                                          └─recusar──> REJECTED (recusada)
```

Cada seta é uma transição protegida: só é permitida a partir do estado exato de origem. Tentar pular uma etapa (por exemplo, aceitar uma duplicata que nunca foi apresentada) é rejeitado.

## Passo a passo: o que acontece em cada etapa

Em toda transição, três coisas acontecem **na mesma transação** (uma só acontece se a outra também acontecer):

1. o novo status é gravado na tabela `invoice`;
2. um registro permanente do fato é gravado na tabela `invoice_history` (a trilha de auditoria — nunca é sobrescrita);
3. o mesmo fato é publicado como mensagem no tópico Kafka `duplicata-events`.

### 1. Emitir — nasce `ISSUED`

**No mundo real:** o vendedor entregou a mercadoria (ou prestou o serviço) a prazo, e formaliza esse crédito emitindo a duplicata a partir da nota fiscal dessa venda — "você me deve isso, pela venda que já aconteceu".

**Chamada:** `POST /invoices`

**O que acontece no código:**
```text
IssueInvoiceService.execute(command)   [@Transactional]
  1. Invoice.builder()...build()       -> valida: valor > 0, vencimento >= emissão,
                                           campos obrigatórios preenchidos
  2. invoice.issuedEvent()             -> InvoiceIssued (evento de domínio)
  3. repository.save(invoice)          -> INSERT em invoice (status=ISSUED)
  4. historyRepository.save(event)     -> INSERT em invoice_history (status=ISSUED)
  5. eventPublisher.publish(event)     -> mensagem no tópico duplicata-events
```

**Como conferir:** Bruno `fluxo-feliz/1 - Emitir duplicata` → response com `"status": "ISSUED"`. No banco, `select * from invoice order by created_at desc limit 1;`. No Kafka UI, uma mensagem nova com `eventType: "InvoiceIssued"`.

### 2. Consultar — não muda nada

**Chamada:** `GET /invoices/{id}`

Só leitura. Não gera evento, não grava histórico, não publica no Kafka — é a única operação do fluxo que não tem efeito colateral.

### 3. Apresentar — `ISSUED -> PRESENTED`

**No mundo real:** a duplicata é formalmente enviada ao comprador (ou ao banco dele), pra que ele se manifeste sobre a transação que a originou — recebeu a mercadoria/serviço como negociado ou não.

**Chamada:** `POST /invoices/{id}/presentation`

**O que acontece no código:**
```text
PresentInvoiceService.present(id)      [@Transactional]
  1. repository.findById(id)           -> carrega o agregado
  2. invoice.present()                 -> valida que status atual é ISSUED,
                                           muda pra PRESENTED, retorna InvoicePresented
  3. repository.save(invoice)          -> UPDATE em invoice (status=PRESENTED)
  4. historyRepository.save(event)     -> INSERT em invoice_history (status=PRESENTED)
  5. eventPublisher.publish(event)     -> mensagem no tópico duplicata-events
```

**Como conferir:** Bruno `fluxo-feliz/3 - Apresentar duplicata`. No banco, a mesma linha de `invoice` agora com `status=PRESENTED`, e uma **segunda** linha em `invoice_history` (a primeira, de `ISSUED`, continua lá — histórico nunca é apagado). No Kafka UI, uma segunda mensagem, mesma chave (`invoiceId`), `eventType: "InvoicePresented"`.

### 4a. Aceitar — `PRESENTED -> ACCEPTED`

**No mundo real:** o comprador confirma que recebeu a mercadoria (ou o serviço) conforme negociado, reconhecendo formalmente a dívida — é esse reconhecimento que torna a duplicata exigível.

**Chamada:** `POST /invoices/{id}/acceptance`

Mesma estrutura do passo 3 (`AcceptInvoiceService`), validando que o status atual é `PRESENTED`. Gera `InvoiceAccepted`.

**Como conferir:** Bruno `fluxo-feliz/4 - Aceitar duplicata`.

### 4b. Recusar — `PRESENTED -> REJECTED` (caminho alternativo)

**No mundo real:** o comprador nega o aceite porque a transação que originou a duplicata não aconteceu como negociado — mercadoria não entregue, veio com vício, ou há divergência de preço/quantidade com o que foi combinado. Não é uma contestação genérica da cobrança; é a negativa de uma condição específica da venda.

**Chamada:** `POST /invoices/{id}/rejection`

Mesma estrutura (`RejectInvoiceService`), gera `InvoiceRejected`. É **alternativo** ao passo 4a, não sequencial — uma duplicata `PRESENTED` vai pra `ACCEPTED` **ou** `REJECTED`, nunca as duas.

**Como conferir:** Bruno `fluxo-alternativo-recusa/` (é um fluxo separado, com sua própria emissão, pra não conflitar com o `invoiceId` do fluxo feliz).

## O que acontece se você pular uma etapa

Cada transição confere o status atual antes de mudar qualquer coisa. Se não bater (por exemplo, chamar `/acceptance` numa duplicata ainda `ISSUED`, sem ter passado por `/presentation`), o domínio lança `InvalidStatusTransitionException`, mapeada para **HTTP 409**. Nada é gravado — nem no banco, nem no Kafka (a exceção estoura antes do `@Transactional` chegar no fim). Dá pra reproduzir isso com `bruno/erros/3a` + `3b`.

## O que ainda não existe

- **Nenhum consumidor lê o tópico `duplicata-events` de verdade.** As mensagens são publicadas, mas hoje só o Kafka UI (ferramenta de inspeção) e um teste automatizado as leem — nada na aplicação reage a elas ainda. Esse é o próximo passo natural da Fase 4.
- **Pagamento, vencimento e protesto** (`PAID`, `OVERDUE`, `PROTEST_REQUESTED`, `PROTESTED`) existem no enum `InvoiceStatus`, mas não têm nenhuma transição implementada — são fases futuras do roadmap.
- **Cancelamento** (`CANCELLED`) também não tem transição implementada ainda.
- **A recusa não captura motivo.** No mundo real, recusar uma duplicata exige uma justificativa específica (mercadoria não entregue, vício, divergência de preço/quantidade). Aqui, `reject()` só muda o status pra `REJECTED` — não existe campo pra registrar por quê. É uma simplificação deliberada do laboratório (ver `docs/DOMAIN.md`), não um esquecimento.

## Onde ver isso rodando

| Ferramenta | Pra quê | Onde |
|---|---|---|
| Bruno | Disparar as requisições | pasta `bruno/` na raiz do projeto |
| Swagger UI | Ver os contratos de cada endpoint | <http://localhost:8080/q/swagger-ui> |
| Kafka UI | Ver as mensagens publicadas | <http://localhost:8090> → Topics → `duplicata-events` |
| psql | Ver o que foi persistido | `docker exec -it escritural-event-lab-postgres psql -U escritural -d escritural_event_lab` |

# Estado atual do laboratório

Este documento descreve o que já funciona, como o fluxo acontece por dentro, e o que falta para fechar a fase em andamento. É um retrato do momento — para o plano completo de fases, ver `docs/ROADMAP.md`; para o modelo de domínio, ver `docs/DOMAIN.md`; para decisões arquiteturais, ver `docs/DECISIONS.md`.

## Visão de produto (sem jargão técnico)

### O que é uma duplicata escritural, na vida real

É um título de cobrança. Quando uma empresa vende algo pra outra a prazo (não à vista), ela emite uma duplicata: um documento que formaliza "a empresa B me deve R$ X, com vencimento no dia Y". A versão "escritural" só existe de forma eletrônica, registrada num sistema — é esse registro que este laboratório simula (de forma simplificada, sem valor jurídico real — ver `docs/DOMAIN.md`).

### A jornada de uma duplicata (o que os status representam)

1. **Emitida** — o vendedor formalizou a cobrança: "você me deve isso".
2. **Apresentada** — a cobrança foi formalmente enviada pro comprador (ou pro banco dele) pra ele tomar ciência.
3. **Aceita** ou **Recusada** — o comprador confirma que a dívida é legítima, ou contesta ("não reconheço essa cobrança").
4. *(fases futuras)* **Paga** — o comprador quitou. **Vencida** — passou do prazo sem pagamento. **Protesto** — se não pagou, o credor pode registrar isso oficialmente em cartório, o que afeta o crédito do devedor.

Hoje o sistema cobre até o passo 3. As próximas fases do laboratório vão cobrir pagamento, vencimento e protesto.

### Por que "histórico" (o que acabamos de construir) importa pro produto

Hoje, se você olha uma duplicata no sistema, só vê o status atual: "está ACCEPTED". Numa disputa real ("vocês nunca me apresentaram essa cobrança!"), é preciso provar **quando** cada passo aconteceu — é uma prova documental. Sem histórico, cada mudança de status apaga o rastro da anterior. O que foi implementado: toda vez que uma duplicata é apresentada, o sistema guarda um registro permanente ("foi apresentada às 14h32 do dia X") que nunca é sobrescrito — o começo de uma trilha de auditoria.

O resto deste documento é a explicação técnica de **como** isso foi construído.

## O que já funciona (Fase 0, 1 e 2 concluídas)

API REST de duplicata escritural (`Invoice`), hexagonal, com Postgres via Flyway (`schema-management.strategy=validate`).

| Operação | Endpoint | Caso de uso |
|---|---|---|
| Emitir | `POST /invoices` | `IssueInvoiceUseCase` / `IssueInvoiceService` |
| Consultar por id | `GET /invoices/{id}` | `FindInvoiceUseCase` / `FindInvoiceService` |
| Apresentar | `POST /invoices/{id}/presentation` | `PresentInvoiceUseCase` / `PresentInvoiceService` |
| Aceitar | `POST /invoices/{id}/acceptance` | `AcceptInvoiceUseCase` / `AcceptInvoiceService` |
| Recusar | `POST /invoices/{id}/rejection` | `RejectInvoiceUseCase` / `RejectInvoiceService` |

Máquina de estados implementada: `ISSUED -> PRESENTED -> ACCEPTED` / `PRESENTED -> REJECTED`. Transição inválida lança `InvalidStatusTransitionException`, mapeada para `409`. Duplicata de número mapeada para `409`. Invoice inexistente mapeada para `404`. Validações de valor/data mapeadas para `400`.

Convenção de pastas: `adapters.in.web` (REST), `adapters.out.persistence` (JPA/Panache), `application.ports.{in,out}` (portas), `application.usecase` (casos de uso), `domain.model.aggregate` / `domain.model.enums` (domínio puro).

## Fase 3 concluída: evento de domínio + histórico

Objetivo da fase (`docs/ROADMAP.md`): registrar um histórico de transições, sem Kafka, aprendendo a diferença entre comando (intenção) e evento (fato consumado).

**Emissão e as 3 transições (`issue()`, `present()`, `accept()`, `reject()`) geram histórico.** Fluxo completo, passo a passo (usando `present()` como exemplo — as demais seguem a mesma forma; `issue()` só difere em criar o agregado em vez de mudar o status de um existente):

```text
1. POST /invoices/{id}/presentation
   -> InvoiceController.present(id)
2. -> PresentInvoiceService.present(id)   [@Transactional]
   a. repository.findById(id)             -> carrega o agregado Invoice
   b. invoice.present()                   -> valida transição ISSUED -> PRESENTED,
                                              muda o status, retorna InvoicePresented
                                              (evento de domínio, Java puro)
   c. repository.save(invoice)            -> grava o novo status na tabela invoice
   d. historyRepository.save(event)       -> grava uma linha na tabela invoice_history
3. Commit único: (c) e (d) só existem juntos ou nenhum dos dois,
   porque estão dentro do mesmo método @Transactional.
```

Peças novas:

- `domain/event/InvoiceEvent.java` — interface comum (`eventId`, `invoiceId`, `occurredAt`, `status`), extraída depois que os eventos convergiram na mesma forma.
- `domain/event/InvoiceIssued.java`, `InvoicePresented.java`, `InvoiceAccepted.java`, `InvoiceRejected.java` — cada um implementa `InvoiceEvent`, nomeado no passado, cada um com seu `status()` fixo.
- `application/ports/out/InvoiceHistoryRepositoryPort.java` — porta de saída, `save(InvoiceEvent)` (um método só, serve os 4).
- `adapters/out/persistence/entity/InvoiceHistoryEntity.java` + `InvoiceHistoryPanacheRepository` + `InvoiceHistoryRepositoryAdapter` — mesmo padrão de 4 camadas já usado para `Invoice` (domínio → porta → entidade JPA → adapter), aplicado ao conceito de histórico.
- `db/migration/V2__create_invoice_history.sql` — tabela `invoice_history` (`id`, `invoice_id` com FK, `status`, `occurred_at`) + índice em `invoice_id`.
- `IssueInvoiceService`, `PresentInvoiceService`, `AcceptInvoiceService`, `RejectInvoiceService` — cada um salva a invoice e o histórico dentro do mesmo `@Transactional`.

Testado: `InvoiceTest` (eventos com `eventId` não nulo, únicos por chamada, status correto — sem Docker) e `PresentInvoiceServiceTest` / `AcceptInvoiceServiceTest` / `RejectInvoiceServiceTest` (`@QuarkusTest`, confirmam as linhas gravadas em `invoice_history`, inclusive a de emissão, já que cada teste emite a invoice antes de transicionar).

### `eventId` implementado

Cada evento carrega `eventId` (gerado pelo próprio método de transição no agregado, um `UUID` novo por fato). O critério do roadmap "eventos possuem id e instante" está satisfeito. O `eventId` é reaproveitado como chave primária da linha em `invoice_history` — efeito colateral bom: se o mesmo evento for salvo duas vezes no futuro, a constraint de chave primária evita duplicar a linha, um gostinho do que a Fase 5 (idempotência) vai formalizar.

## Fase 3 fechada

Os 4 critérios do roadmap estão marcados em `docs/ROADMAP.md`. `issue()` também gera histórico: `Invoice.issuedEvent()` empacota `id`/`createdAt` já existentes no agregado recém-criado num `InvoiceIssued`, e `IssueInvoiceService` salva invoice + histórico no mesmo `@Transactional`, igual às outras transições.

## Fase 4 em andamento: Kafka de verdade

Objetivo da fase (`docs/ROADMAP.md`): sair do histórico interno (Fase 3, só no Postgres) para publicar os mesmos eventos de domínio num tópico Kafka de verdade, entendendo producer, consumer, ordenação por chave e tratamento de falha (incluindo DLQ e parking lot, adicionados ao escopo da fase depois da decisão de longo prazo do projeto).

### O que já existe: producer de `issue()`

Hoje, toda vez que uma duplicata é **emitida** (`POST /invoices`), além de salvar no banco e gravar o histórico (Fase 3), o sistema também publica uma mensagem no tópico Kafka `duplicata-events`. As outras transições (`present()`, `accept()`, `reject()`) ainda **não** publicam — só emissão, por enquanto.

Fluxo, ponta a ponta:

```text
1. POST /invoices
   -> IssueInvoiceService.execute(command)   [@Transactional]
   a. Invoice.builder()...build()            -> cria o agregado em memória
   b. invoice.issuedEvent()                  -> InvoiceIssued (evento de domínio, Java puro)
   c. repository.save(invoice)               -> grava a invoice no Postgres
   d. historyRepository.save(event)          -> grava uma linha em invoice_history (Fase 3)
   e. invoiceEventPublisherPort.publish(event) -> publica no Kafka (Fase 4, novo)
2. InvoiceEventPublisherAdapter monta o envelope e chama emitter.send(...)
3. SmallRye Reactive Messaging serializa o envelope em JSON e manda pro tópico
   duplicata-events, usando invoiceId como chave da mensagem.
```

Peças novas:

- `application/ports/out/InvoiceEventPublisherPort.java` — porta de saída, `publish(InvoiceEvent)`. Mesmo desenho da `InvoiceHistoryRepositoryPort` da Fase 3.
- `adapters/out/messaging/dto/InvoiceEventMessage.java` — o envelope que efetivamente trafega no Kafka: `eventId`, `eventType`, `eventVersion`, `aggregateId`, `occurredAt`, `correlationId`, `status`. Não é o mesmo objeto que `InvoiceEvent` do domínio — é a tradução desse evento pro formato de mensageria (mesma ideia de "DTO não é entidade de domínio", aplicada a eventos).
- `adapters/out/messaging/InvoiceEventPublisherAdapter.java` — implementa a porta usando `@Channel("duplicata-events") Emitter<Record<String, InvoiceEventMessage>>`. `Record.of(invoiceId, message)` garante que a chave Kafka é sempre o `invoiceId` — isso é o que permite ordenar por duplicata (mensagens com a mesma chave sempre caem na mesma partição, na ordem em que foram enviadas).
- `application.properties` — liga o canal lógico `duplicata-events` ao Kafka de verdade: `connector=smallrye-kafka`, tópico, serializer de chave (`StringSerializer`) e de valor (`ObjectMapperSerializer`, que serializa o envelope em JSON usando o `ObjectMapper` já gerenciado pelo Quarkus, o mesmo que a camada REST usa — por isso `Instant` funciona sem configuração extra).

### Decisões tomadas nessa fatia

- `eventType` não entra no domínio — é calculado no adapter (`event.getClass().getSimpleName()`), porque é um detalhe de formato de mensageria, não uma regra de negócio.
- `correlationId` é gerado como um novo `UUID` a cada publicação, por enquanto — é um placeholder consciente até a Fase 8 trazer propagação real de correlation id a partir do request HTTP.
- A publicação acontece dentro da mesma `@Transactional` do use case, logo depois do `historyRepository.save`. Isso é um dual-write consciente (se o commit falhar depois do `send`, a mensagem já foi pro Kafka mas o banco não gravou) — aceito por enquanto porque o roadmap já previa que outbox transacional é evolução posterior, não requisito da primeira publicação.
- `kafka.bootstrap.servers` passou a ser `%dev.`-only (antes era global). Sem isso, os testes tentavam conectar no Kafka do `docker-compose` (que pode nem estar de pé) em vez de deixar o Dev Services do Quarkus subir um Kafka efêmero pra cada execução de teste — mesmo ajuste que já existia pro Postgres.

### Como foi testado

`IssueInvoiceServiceTest` (`application/usecase`, `@QuarkusTest`) emite uma invoice e usa o **Kafka Companion** (`io.quarkus:quarkus-test-kafka-companion`, dependência de teste nova) pra consumir do tópico `duplicata-events` e confirmar: a mensagem chegou, com a chave certa (`invoiceId`), e o corpo (deserializado de volta pra `InvoiceEventMessage`) tem os campos esperados. A classe de teste precisa da anotação `@QuarkusTestResource(KafkaCompanionResource.class)` — sem ela, o Quarkus nunca ativa o mecanismo que injeta o `KafkaCompanion` no teste (é um resource opt-in, não automático). Suite completa (`mvnw test`, Docker Desktop rodando) passou depois dessa mudança, incluindo os testes que já existiam e que agora, de carona, também publicam eventos de emissão sem quebrar.

### O que ainda falta pra fechar a Fase 4

- `present()`, `accept()`, `reject()` ainda não publicam no Kafka (só `issue()`).
- Nenhum consumer existe ainda — ninguém lê o tópico `duplicata-events` de verdade (só o teste, via companion).
- Ordem por duplicata está garantida pela chave, mas ainda não foi exercitada com múltiplas mensagens da mesma invoice em sequência.
- DLQ e parking lot (critérios adicionados ao roadmap) ainda não têm nenhuma peça implementada.

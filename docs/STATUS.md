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

## Depois da Fase 3

Fase 4 do roadmap: Kafka de verdade (producer, consumer, tópico único `duplicata-events`, chave por `duplicataId`, consumer group, threads bloqueantes) — só começa depois da Fase 3 fechada, para não esconder domínio ainda instável atrás de infraestrutura de mensageria (`.claude/CLAUDE.md`).

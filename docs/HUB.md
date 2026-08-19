# Experimento: Hub de Integração com Apache Camel

## Objetivo

Simular, neste laboratório, um hub de integração parecido com o que existe no trabalho do usuário:
múltiplas origens heterogêneas (Kafka, IBM MQ) convergindo para uma saída única (Kafka), usando
Apache Camel como camada de roteamento.

Este experimento roda na branch `experiment/ibm-mq`, **fora** da sequência principal de
`docs/ROADMAP.md` (que segue para a Fase 5 — pagamento e idempotência, ainda não iniciada). É um
desvio consciente para aprender uma ferramenta específica de trabalho, não uma substituição do
roadmap do domínio de duplicata.

## Escopo decidido (2026-08-17)

- **Hub genérico, desacoplado do domínio Invoice.** Tópicos e filas próprios do hub
  (`hub-in`, `hub-out`), payload próprio do hub — não reaproveita `InvoiceEventMessage` nem o
  tópico `duplicata-events`. A ideia é praticar roteamento/EIP puro, como em um hub real que não
  conhece o schema de quem produz ou consome.
- **Mesma aplicação Quarkus.** Sem módulo Maven separado. Consistente com a decisão "monólito
  modular primeiro" já registrada em `docs/DECISIONS.md`. As rotas Camel ficam no pacote
  `br.com.marco.escritural.hub`, fora da hexagonal do Invoice (não há porta/domínio por trás —
  é infraestrutura de integração pura, então forçar `adapters.in/out` aqui seria abstração sem uso).

## Por que Camel, se o projeto já usa SmallRye Reactive Messaging?

| | SmallRye Reactive Messaging (já usado) | Apache Camel |
|---|---|---|
| Modelo | Anotações `@Incoming`/`@Outgoing` em métodos, um canal por método | DSL de rotas (`from().to()`), um grafo por `RouteBuilder` |
| Alcance | Conectores de mensageria (Kafka, AMQP, MQTT...) | +300 componentes (Kafka, JMS/IBM MQ, HTTP, arquivo, FTP...) |
| Padrões de integração (EIP) | Poucos, implícitos | Nativos: content-based router, splitter, aggregator, dead letter channel, idempotent consumer |
| Uso típico | Comunicação evento-a-evento dentro de um domínio homogêneo | Camada de integração entre sistemas heterogêneos — exatamente o papel de um hub |

Os dois convivem sem conflito no mesmo `CamelContext`/aplicação Quarkus — são extensões
independentes falando com o mesmo broker Kafka.

## Fases do experimento

### Fase H1 — Camel Quarkus + rota Kafka → Kafka

Objetivo: validar a integração Camel Quarkus (extensão, `RouteBuilder`, dev mode) com o menor
componente possível — reaproveitando o Kafka que já está rodando via `docker-compose.yml`, sem
tocar em IBM MQ ainda.

Critérios:

- [x] extensão `camel-quarkus-kafka` adicionada via `quarkus:add-extension`;
- [x] uma `RouteBuilder` consome de `hub-in` e produz em `hub-out`;
- [x] teste de integração cobre o fluxo `hub-in -> hub-out`;
- [ ] mensagem publicada manualmente (Kafka UI) chega transformada em `hub-out` (opcional, o teste automatizado já cobriu o fluxo).

### Fase H2 — IBM MQ no Docker + conexão JMS validada

Objetivo: subir um IBM MQ (Developer Edition) via Docker, entender fila/canal/queue manager, e
validar a conexão JMS a partir do Quarkus antes de rotear qualquer mensagem real.

Critérios:

- [x] serviço `ibmmq` no `docker-compose.yml`, saudável (`AMQ8003I: IBM MQ queue manager 'QM1' started`);
- [x] `ConnectionFactory` JMS configurada no Quarkus aponta para o queue manager;
- [x] uma rota trivial (`from("jms:queue:DEV.QUEUE.1")...to("log:...")`) confirma a conexão — validado publicando via `amqsput` e conferindo o log `Recebido do IBM MQ: ...` no `quarkus:dev`;
- [ ] console web (porta 9443) — não usado, validação foi via `amqsput` + log, suficiente para o objetivo.

**Nota**: essa fase foi validada manualmente, não com `@QuarkusTest`. Não existe Dev Services nativo do
Quarkus para IBM MQ, então automatizar isso exigiria subir o container via Testcontainers dentro do
próprio teste — decisão adiada para a Fase H3, quando o fluxo completo (duas origens, uma saída)
precisar mesmo de um teste de integração.

### Fase H3 — Rota IBM MQ → Kafka (o hub de fato)

Objetivo: duas entradas (Kafka `hub-in` e IBM MQ `DEV.QUEUE.1`) convergindo para uma única saída
Kafka (`hub-out`), com alguma transformação/normalização no meio — este é o comportamento que dá
nome ao "hub".

Critérios:

- [x] mensagem publicada no Kafka chega normalizada em `hub-out` (`HubRouteTest`, automatizado);
- [x] mensagem publicada no IBM MQ chega normalizada em `hub-out` (validado na mão: `amqsput` + Kafka UI);
- [x] formato de saída é único, independente da origem — envelope `HubMessage(source, payload, receivedAt)`;
- [x] teste de integração cobre o lado Kafka (`HubRouteTest`) e o lado IBM MQ
  (`IbmMqRouteTest`, via `IbmMqTestResource` — Testcontainers sobe um IBM MQ descartável antes da
  app iniciar, porta dinâmica injetada em `ibmmq.host`/`ibmmq.port`; dívida da Fase H2 fechada em
  2026-08-17).

### Fase H4 — EIPs adicionais (opcional, não iniciada)

Ideias para depois que H1-H3 estiverem sólidas, não implementar antes: content-based router
(rotear por tipo de mensagem), dead letter channel do próprio Camel (`onException`), idempotent
consumer (deduplicação nativa do Camel, para comparar com o padrão manual que já existe em
`InvoiceParkingLotConsumer`), teste de carga simples.

## Cenário invertido: Kafka → IBM MQ (banco A → banco B)

Motivação real do usuário: atuar como hub entre dois bancos, recebendo duplicata via Kafka de um
banco e encaminhando via IBM MQ pro outro. É a direção oposta das rotas H1-H3 (que convergem *para*
o Kafka) — aqui o Kafka é entrada e o IBM MQ é saída.

- **`BankForwardRoute`**: `from("kafka:duplicata-recebida?...")` → `to("jms:queue:DEV.QUEUE.2?...")`.
  Tópico e fila **diferentes** dos usados em H1-H3 de propósito: `duplicata-recebida` não é `hub-in`
  (não deve se misturar com o fluxo de normalização do hub), e `DEV.QUEUE.2` não é `DEV.QUEUE.1`
  (que já tem o `IbmMqProbeRoute` consumindo — publicar ali criaria um auto-consumo dentro do mesmo
  processo).
- Sem `HubMessage`/normalização — fluxo ponta a ponta simples, o payload que sai é o que entrou.
- **`BankForwardRouteTest`**: mesmo padrão de `IbmMqRouteTest` (Testcontainers via `IbmMqTestResource`),
  mas usando `ConsumerTemplate.receiveBody(...)` pra ler de volta da fila IBM MQ — par do
  `ProducerTemplate` usado nos testes que escrevem pra lá.

## Ferramenta de debug (`/hub.html`)

Página estática (`src/main/resources/META-INF/resources/hub.html`, sem framework/build step) +
`HubDebugResource` (`GET /hub/events` via SSE, `POST /hub/publish`) + `HubEventBus`
(`BroadcastProcessor` do Mutiny, em memória). As duas rotas (`HubRoute`, `IbmMqProbeRoute`) publicam
eventos `RECEBIDO`/`PUBLICADO_HUB_OUT` nesse barramento a cada mensagem processada, e a página exibe
ao vivo. Permite disparar mensagens de teste pros dois lados sem precisar de `amqsput`/Kafka UI.

**Achado técnico relevante**: `RouteBuilder` do Camel não pode ser `@ApplicationScoped`. O Quarkus
ArC gera um proxy de subclasse para beans normal-scoped, e `RouteBuilder.onException(...)` (herdado
do Camel) é `final` — o proxy fica com bytecode inválido (`IncompatibleClassChangeError` no boot).
Solução: manter o `RouteBuilder` como classe simples (não gerenciada pelo CDI) e buscar beans via
`CDI.current().select(Tipo.class).get()` dentro do `configure()`, em vez de `@Inject` de campo.

## Decisões técnicas já validadas

- **Versão do Camel Quarkus**: não fixar manualmente uma versão de BOM do Camel. Usar
  `./mvnw quarkus:add-extension` — confirmado na prática que ele resolve a BOM
  `io.quarkus.platform:quarkus-camel-bom`, pinada exatamente na mesma versão do
  `quarkus.platform.version` (3.37.3) já usada pelo projeto. Alinhamento exato, não aproximado —
  evita incompatibilidade de classpath entre o Quarkus core embutido no Camel Quarkus e o da aplicação.
- **Imagem IBM MQ**: `icr.io/ibm-messaging/mq:9.4.5.1-r1` (Developer Edition, licença gratuita para
  uso não produtivo). Confirmar tags atuais em `icr.io/ibm-messaging/mq` antes de subir, pois a IBM
  publica novas builds com frequência.
- **IBM MQ via JMS, não um componente dedicado**: não existe um "camel-ibm-mq" — a integração é via
  `camel-quarkus-jms` (JMS 2.0 genérico) + cliente Java da IBM (`com.ibm.mq.allclient`), igual ao
  que normalmente se usa em ambiente corporativo real.

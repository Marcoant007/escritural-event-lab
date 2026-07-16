# Roadmap de aprendizado

Marque uma fase apenas quando seus critérios estiverem realmente concluídos.

## Fase 0 — Bootstrap Quarkus e banco

Objetivos:

- subir o Dev Mode;
- entender Dev Services;
- conectar PostgreSQL;
- criar a primeira migration Flyway;
- usar Hibernate em `validate`;
- executar testes básicos.

Critérios:

- [x] aplicação inicia;
- [x] PostgreSQL está disponível;
- [x] Flyway executa a migration;
- [x] `flyway_schema_history` é criada;
- [x] Hibernate valida o schema;
- [x] testes passam.

## Fase 1 — Emitir e consultar duplicata

Objetivos:

- criar modelo de domínio;
- implementar porta de repositório;
- adapter Panache;
- POST e GET;
- validação;
- testes unitários e REST.

Critérios:

- [x] emitir duplicata válida;
- [x] rejeitar valor inválido;
- [x] rejeitar vencimento inválido;
- [x] impedir número duplicado no contexto definido;
- [x] consultar por id;
- [x] retornar 404;
- [x] testes passam.

## Fase 2 — Máquina de estados

Objetivos:

- apresentar;
- aceitar;
- recusar;
- proteger transições;
- mapear conflitos para HTTP 409.

Critérios:

- [x] `EMITIDA -> APRESENTADA`;
- [x] `APRESENTADA -> ACEITA`;
- [x] `APRESENTADA -> RECUSADA`;
- [x] transições inválidas falham;
- [x] testes de domínio e REST passam.

## Fase 3 — Eventos internos e histórico

Objetivos:

- criar eventos de domínio;
- registrar histórico;
- entender diferença entre comando e evento;
- manter tudo sem Kafka inicialmente.

Critérios:

- [x] eventos possuem id e instante;
- [x] histórico é persistido;
- [x] emissão e transição geram histórico;
- [x] transação garante consistência.

## Fase 4 — Kafka e notificação simulada

Objetivos:

- SmallRye Reactive Messaging;
- producer;
- consumer;
- tópico;
- chave por duplicata;
- consumer group;
- threads bloqueantes;
- dead-letter queue (DLQ);
- parking lot para eventos não processáveis.

Critérios:

- [ ] evento é publicado;
- [ ] consumidor recebe;
- [ ] ordem por duplicata é preservável;
- [ ] falha não é ignorada;
- [ ] falha de processamento é desviada para uma DLQ, sem perder a mensagem;
- [ ] parking lot isola eventos problemáticos sem bloquear o consumo dos demais;
- [ ] testes de integração passam.

## Fase 5 — Pagamento e idempotência

Objetivos:

- registrar pagamento total;
- processar evento repetido;
- tabela de eventos processados;
- compreender at-least-once.

Critérios:

- [ ] pagamento válido muda para `PAGA`;
- [ ] pagamento repetido não duplica efeito;
- [ ] `consumerName + eventId` é único;
- [ ] replay está testado.

## Fase 6 — Vencimento e scheduler

Objetivos:

- marcar aceitas vencidas;
- usar `@Scheduled`;
- processar em lotes;
- discutir múltiplas instâncias e locking.

Critérios:

- [ ] job encontra elegíveis;
- [ ] não altera duplicata paga;
- [ ] execução repetida é segura;
- [ ] testes não dependem de sleep.

## Fase 7 — Protesto e Fault Tolerance

Objetivos:

- mock de cartório;
- REST Client;
- retry;
- timeout;
- circuit breaker;
- fallback.

Critérios:

- [ ] chamada feliz funciona;
- [ ] timeout está observável;
- [ ] retry não duplica efeito;
- [ ] fallback mantém estado coerente;
- [ ] circuit breaker é demonstrado.

## Fase 8 — Observabilidade e carga

Objetivos:

- health;
- métricas;
- tracing;
- consumer lag;
- teste de carga;
- análise de throughput.

Critérios:

- [ ] métricas úteis expostas;
- [ ] correlation id propagado;
- [ ] gargalos identificados;
- [ ] relatório do experimento registrado.

## Evoluções opcionais

- Outbox transacional.
- Debezium.
- Pagamento parcial.
- Cessão e titularidade.
- Quartz clusterizado.
- Native image.
- Kubernetes.
- Separação do serviço de notificação.

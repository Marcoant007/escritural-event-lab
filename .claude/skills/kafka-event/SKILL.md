---
name: Kafka Event
description: Adiciona um evento Kafka ao laboratório com envelope, chave, producer, consumer, configuração e idempotência proporcional.
argument-hint: "<evento e reação desejada>"
disable-model-invocation: true
---

Implemente o fluxo Kafka: $ARGUMENTS

Antes de codificar:

1. Confirme no roadmap que a fase Kafka já foi alcançada.
2. Identifique o fato de negócio e quem o produz.
3. Defina quem consome e qual efeito observável será causado.
4. Explique por que esse fluxo precisa de Kafka em vez de chamada direta.

Implementação:

1. Use o tópico inicial `duplicata-events`, salvo decisão documentada.
2. Use `duplicataId` como chave.
3. Crie envelope versionado com:
   - eventId;
   - eventType;
   - eventVersion;
   - aggregateId;
   - occurredAt;
   - correlationId;
   - payload.
4. Configure channels com nomes claros.
5. Se o consumer usar Hibernate ORM/JDBC, processe em worker thread.
6. Se houver efeito persistente, implemente idempotência por consumidor e eventId.
7. Não confirme processamento silenciosamente em caso de falha.
8. Crie testes para:
   - publicação;
   - desserialização;
   - processamento;
   - replay do mesmo evento;
   - falha relevante.
9. Execute a suíte e explique partição, consumer group, offset e redelivery.
10. Não introduza outbox, DLQ ou múltiplos tópicos sem necessidade nesta tarefa.

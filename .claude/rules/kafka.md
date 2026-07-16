---
paths:
  - "src/main/java/**/infrastructure/messaging/**/*.java"
  - "src/test/java/**/infrastructure/messaging/**/*.java"
  - "src/main/resources/application*.properties"
---

# Kafka e mensageria

- Não introduzir Kafka antes da fase indicada no roadmap.
- Começar com um único tópico `duplicata-events`.
- Chave Kafka: `duplicataId`.
- Eventos são fatos no passado, como `DuplicataEmitida`.
- Comandos não devem ser nomeados como eventos.
- Envelope mínimo:
  - eventId;
  - eventType;
  - eventVersion;
  - aggregateId;
  - occurredAt;
  - correlationId;
  - payload.
- Consumidor com Hibernate ORM/JDBC deve ser bloqueante.
- Consumidor deve assumir redelivery.
- Efeito de negócio requer idempotência por `consumerName + eventId`.
- Não engolir erro para confirmar offset indevidamente.
- Retry e DLQ devem ser configurados conscientemente e testados.
- Não criar muitos tópicos ou consumers antes de existir necessidade.

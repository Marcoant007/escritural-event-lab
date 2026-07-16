---
paths:
  - "src/main/java/**/domain/**/*.java"
  - "src/test/java/**/domain/**/*.java"
---

# Regras do domínio

- Código de domínio deve ser Java puro.
- Não usar anotações de JPA, CDI, REST, Jackson ou Kafka no domínio.
- Preferir comportamento dentro do agregado a setters públicos.
- Não permitir mudança direta de status.
- Cada transição deve validar o estado atual.
- Falhas de regra devem usar exceções específicas do domínio.
- Dinheiro usa `BigDecimal` e escala definida conscientemente.
- Datas de negócio usam `LocalDate`; instantes de evento usam `Instant`.
- Objetos devem nascer válidos.
- Não adicionar conceitos jurídicos avançados sem atualizar `docs/DOMAIN.md`.
- Ao criar uma regra, escrever primeiro os cenários válidos e inválidos que serão testados.

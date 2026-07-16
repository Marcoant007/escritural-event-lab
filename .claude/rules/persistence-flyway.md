---
paths:
  - "src/main/java/**/infrastructure/persistence/**/*.java"
  - "src/main/resources/db/migration/**/*.sql"
  - "src/main/resources/application*.properties"
  - "src/test/resources/application*.properties"
---

# Persistência e Flyway

- Flyway controla o schema.
- Não usar `update` como estratégia de schema.
- Produção e desenvolvimento persistente devem usar `validate`.
- Migrations usam o padrão `V<numero>__<descricao>.sql`.
- Antes de criar migration, verificar a maior versão existente.
- Não reutilizar número.
- Não editar migration antiga para representar uma nova alteração.
- SQL deve ser explícito e compatível com PostgreSQL.
- Criar constraints, índices e unicidade quando a regra exigir.
- `NUMERIC(19,2)` é o padrão inicial para valores monetários.
- Mapear enums como texto, nunca ordinal.
- Repositórios Panache pertencem à infraestrutura.
- Mapper entre domínio e entidade JPA deve ser explícito.
- Sempre validar compatibilidade entre nullability no Java e no SQL.

---
name: Flyway Migration
description: Cria uma migration Flyway PostgreSQL segura e compatível com as entidades do laboratório.
argument-hint: "<mudança de schema>"
disable-model-invocation: true
---

Crie a migration para: $ARGUMENTS

Checklist:

1. Leia todas as migrations existentes em ordem.
2. Identifique a próxima versão disponível.
3. Leia as entidades JPA e mappers afetados.
4. Explique o SQL que será aplicado e os riscos.
5. Crie uma nova migration; não modifique migrations anteriores.
6. Garanta coerência de:
   - nomes;
   - tipos;
   - nullability;
   - defaults;
   - constraints;
   - índices;
   - enum textual.
7. Para coluna obrigatória em tabela com dados, planeje uma evolução segura em etapas.
8. Atualize entidade e mapper se necessário.
9. Execute os testes ou inicialize o contexto Quarkus para validar o schema.
10. Mostre como consultar `flyway_schema_history` para entender a execução.

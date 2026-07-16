---
name: Start Lab
description: Inicializa ou organiza o laboratório Quarkus de duplicata escritural, implementando apenas o menor bootstrap necessário.
argument-hint: "[opcional: objetivo inicial]"
disable-model-invocation: true
---

Inicialize ou organize o projeto para o objetivo: $ARGUMENTS

Procedimento:

1. Leia `.claude/CLAUDE.md`, `docs/ROADMAP.md`, `docs/DOMAIN.md` e `docs/DECISIONS.md`.
2. Inspecione `pom.xml`, `src`, configurações e testes existentes.
3. Não recrie um projeto que já existe.
4. Informe o estado atual e escolha apenas o menor passo necessário para deixar o laboratório executável.
5. Para um projeto vazio, configure somente:
   - Java 21;
   - Quarkus REST Jackson;
   - Hibernate ORM Panache;
   - PostgreSQL JDBC;
   - Flyway;
   - Hibernate Validator;
   - SmallRye OpenAPI;
   - SmallRye Health.
6. Não adicione Kafka, scheduler, fault tolerance, OIDC, Kubernetes ou native image nessa etapa.
7. Configure Flyway com uma migration inicial e Hibernate em `validate`.
8. Crie um health check ou endpoint mínimo apenas se necessário para confirmar o runtime.
9. Execute compilação e testes.
10. Explique:
    - como o Dev Mode funciona;
    - como Dev Services participa;
    - qual é a função do Flyway;
    - por que Kafka ainda não foi adicionado.
11. Atualize o progresso em `docs/ROADMAP.md` sem marcar fases futuras.

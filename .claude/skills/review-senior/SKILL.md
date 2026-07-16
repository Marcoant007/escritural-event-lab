---
name: Senior Review
description: Revisa uma mudança do laboratório como backend sênior, buscando erros de domínio, transação, concorrência, Kafka, banco e testes.
argument-hint: "[arquivo, pacote ou diff]"
disable-model-invocation: true
---

Revise o escopo: $ARGUMENTS

Prioridade da revisão:

1. Correção funcional e regras de negócio.
2. Transações e consistência banco/evento.
3. Concorrência e idempotência.
4. Contrato REST e tratamento de erros.
5. Schema, constraints e migrations.
6. Uso correto do Quarkus.
7. Testes ausentes ou frágeis.
8. Complexidade desnecessária.

Formato da resposta:

- Achados críticos.
- Achados importantes.
- Melhorias opcionais.
- Pontos positivos.
- Próximo ajuste recomendado.

Para cada achado, indique arquivo e trecho, explique o impacto e proponha uma correção concreta. Não invente problemas apenas para preencher a revisão.

---
name: Test Feature
description: Cria ou melhora testes de uma funcionalidade Quarkus com foco em comportamento, cobertura útil e resistência a mutações.
argument-hint: "<classe, pacote ou comportamento>"
disable-model-invocation: true
---

Teste este alvo: $ARGUMENTS

1. Leia produção e testes existentes.
2. Liste regras e comportamentos ainda não protegidos.
3. Escolha o nível adequado:
   - unitário de domínio;
   - aplicação com fake;
   - `@QuarkusTest`;
   - RestAssured;
   - integração Kafka.
4. Evite subir Quarkus quando um teste Java puro resolver.
5. Crie testes para caminhos relevantes, incluindo falhas.
6. Evite assertivas superficiais e mocks que apenas repetem a implementação.
7. Execute os testes.
8. Caso haja ferramenta de cobertura ou mutation configurada, execute-a e analise sobreviventes relevantes.
9. Informe quais bugs cada teste seria capaz de detectar.

---
paths:
  - "src/test/**/*.java"
  - "pom.xml"
---

# Testes

- Testes de domínio não devem subir Quarkus.
- Usar `@QuarkusTest` somente quando integração com runtime, REST ou persistência for necessária.
- Preferir fake em memória a mocks profundos.
- Não mockar a classe sob teste.
- Cobrir caminho feliz, transição inválida, inexistência e duplicidade.
- Verificar efeitos observáveis, não detalhes internos frágeis.
- Testes devem ser independentes e determinísticos.
- Evitar sleeps.
- Para concorrência, usar sincronização controlada e assertivas de resultado.
- Ao corrigir bug, primeiro criar teste que reproduza o problema.
- Quando mutation testing entrar, priorizar sobreviventes que revelam ausência de regra.

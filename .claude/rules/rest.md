---
paths:
  - "src/main/java/**/infrastructure/rest/**/*.java"
  - "src/test/java/**/infrastructure/rest/**/*.java"
---

# Regras REST

- Resources REST apenas validam entrada, convertem DTOs e chamam casos de uso.
- Usar Bean Validation nos DTOs.
- Não colocar regra de negócio no resource.
- Não retornar entidade JPA.
- Usar códigos HTTP coerentes:
  - 201 para emissão;
  - 200 para consulta e comandos com resposta;
  - 204 quando não houver corpo;
  - 400 para payload inválido;
  - 404 para duplicata inexistente;
  - 409 para conflito de estado ou duplicidade.
- Criar tratamento consistente para exceções de domínio.
- Testar contrato HTTP com RestAssured.

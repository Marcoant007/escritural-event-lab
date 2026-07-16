---
paths:
  - "src/main/java/**/application/**/*.java"
  - "src/test/java/**/application/**/*.java"
---

# Regras da aplicação

- Um caso de uso representa uma intenção de negócio.
- Casos de uso orquestram o domínio e as portas; não concentram regras que pertencem ao agregado.
- Delimitar transação na aplicação ou adaptador de entrada apropriado.
- Entradas devem ser commands ou parâmetros explícitos.
- Saídas devem evitar expor entidades JPA.
- Dependências externas são acessadas por portas.
- Não capturar `Exception` genérica sem uma estratégia explícita.
- Não adicionar retry dentro do domínio.
- Manter casos de uso pequenos e nomeados por verbo de negócio.

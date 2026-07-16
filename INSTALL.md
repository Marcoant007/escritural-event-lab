# Instalação do kit no projeto

## Estrutura

Copie as pastas `.claude` e `docs` para a raiz do projeto Quarkus:

```text
escritural-event-lab/
├── .claude/
├── docs/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
```

## No IntelliJ

1. Abra a raiz do projeto, não apenas `src`.
2. Inicie o Claude Code pela integração JetBrains ou pelo terminal na raiz.
3. Execute `/memory`.
4. Confirme que `.claude/CLAUDE.md` e as rules aparecem carregadas.
5. Digite `/` e confirme os comandos do projeto.

## Primeiro fluxo sugerido

Em projeto vazio ou recém-gerado:

```text
/start-lab preparar o bootstrap inicial
```

Depois:

```text
/next-step
```

Para implementar a primeira fatia:

```text
/implement-use-case emitir duplicata
```

Para estudar antes de codificar:

```text
/learn-quarkus Dev Services
/learn-quarkus CDI
/learn-quarkus Hibernate ORM com Panache
```

## Skills disponíveis

```text
/start-lab
/next-step
/implement-use-case
/flyway-migration
/kafka-event
/test-feature
/review-senior
/learn-quarkus
```

## Subagentes disponíveis

O Claude pode delegar análises para:

```text
quarkus-mentor
domain-reviewer
test-reviewer
```

Exemplos:

```text
Use o subagente quarkus-mentor para me explicar o fluxo de uma requisição.
Use o domain-reviewer para revisar a máquina de estados.
Use o test-reviewer para encontrar testes frágeis.
```

## Observação

Hooks automáticos não foram adicionados. Neste laboratório, é melhor executar testes de forma consciente e entender cada comando antes de automatizar.

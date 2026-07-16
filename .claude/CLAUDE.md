# Escritural Event Lab

## Objetivo do projeto

Este repositório é um laboratório pessoal para aprender Quarkus usando o domínio simplificado de duplicata escritural.

Prioridades, nesta ordem:

1. Entender o recurso do Quarkus que está sendo usado.
2. Implementar regras de negócio pequenas e testáveis.
3. Praticar arquitetura hexagonal sem abstrações prematuras.
4. Aprender PostgreSQL, Flyway, Kafka, idempotência, concorrência, scheduler, resiliência e observabilidade.
5. Evoluir o domínio progressivamente.

Este projeto não pretende ser uma escrituradora real, juridicamente completa ou integrada à B3.

## Papel do Claude

Atue como mentor técnico e pair programmer, não como gerador automático de projeto inteiro.

Em cada mudança:

1. Leia o código existente antes de propor alterações.
2. Explique brevemente o conceito do Quarkus envolvido.
3. Implemente a menor fatia vertical que gere aprendizado e valor.
4. Evite criar infraestrutura que ainda não será usada.
5. Escreva ou atualize testes junto da implementação.
6. Execute os testes relevantes.
7. Ao terminar, informe:
   - o que foi alterado;
   - o que o Quarkus está fazendo;
   - quais decisões foram tomadas;
   - qual é o próximo passo natural.

Não implemente vários módulos de uma vez. Não esconda complexidade com código mágico. Quando houver mais de uma opção válida, apresente o trade-off e escolha a mais simples para este laboratório.

## Stack

- Java 21
- Maven Wrapper
- Quarkus 3.x
- REST Jackson
- Hibernate ORM com Panache
- PostgreSQL
- Flyway
- Hibernate Validator
- SmallRye Reactive Messaging com Kafka, quando a fase chegar
- SmallRye Fault Tolerance, quando a fase chegar
- Scheduler, quando a fase chegar
- JUnit 5
- Quarkus Test
- RestAssured
- Mockito somente quando um fake simples não for melhor

## Arquitetura

Usar monólito modular com arquitetura hexagonal leve.

Estrutura de referência:

```text
src/main/java/br/com/marco/escritural
├── domain
│   ├── model
│   ├── event
│   ├── exception
│   └── port
├── application
│   ├── command
│   ├── usecase
│   └── service
└── infrastructure
    ├── rest
    ├── persistence
    ├── messaging
    ├── scheduler
    └── external
```

Regras:

- O domínio não depende de Quarkus, Panache, JPA, Kafka ou REST.
- Casos de uso dependem de portas do domínio.
- Adaptadores implementam portas.
- Entidades JPA ficam na infraestrutura de persistência.
- DTOs REST não são entidades de domínio.
- Não criar interfaces para classes sem necessidade real.
- Não criar `BaseService`, `GenericRepository`, `AbstractEntity` ou utilitários genéricos prematuramente.
- Manter uma única aplicação enquanto microsserviços não forem necessários para o aprendizado.

## Domínio simplificado inicial

Status permitidos:

```text
EMITIDA
APRESENTADA
ACEITA
RECUSADA
VENCIDA
PAGA
PROTESTO_SOLICITADO
PROTESTADA
CANCELADA
```

Transições iniciais:

```text
EMITIDA -> APRESENTADA
APRESENTADA -> ACEITA
APRESENTADA -> RECUSADA
ACEITA -> PAGA
ACEITA -> VENCIDA
VENCIDA -> PAGA
VENCIDA -> PROTESTO_SOLICITADO
PROTESTO_SOLICITADO -> PROTESTADA
```

O modelo é propositalmente simplificado. Não adicionar cessão, endosso, titularidade, pagamento parcial ou aceite presumido antes de a fase correspondente ser escolhida.

## Persistência

- Flyway é responsável por criar e evoluir o schema.
- Hibernate deve validar o schema, não atualizá-lo automaticamente.
- Usar `BigDecimal` para dinheiro.
- Usar `UUID` para identificadores.
- Usar `@Version` na entidade JPA quando a fase de concorrência começar.
- Migrations ficam em `src/main/resources/db/migration`.
- Nunca editar uma migration versionada já existente para corrigir uma evolução; criar uma nova.
- Em testes isolados descartáveis, resetar o banco é permitido.

Configuração desejada:

```properties
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.schema-management.strategy=validate
```

## Kafka

Kafka só entra depois de emissão, consulta e transições básicas estarem funcionando com testes.

Quando entrar:

- Entrega assumida: at-least-once.
- Todo evento possui `eventId`, `eventType`, `eventVersion`, `aggregateId`, `occurredAt` e `correlationId`.
- Usar `duplicataId` como chave da mensagem.
- Consumidores que causam efeito devem ser idempotentes.
- Acesso JDBC/Hibernate em consumidor deve ocorrer em contexto bloqueante.
- Não prometer exactly-once de ponta a ponta.
- Começar com um único tópico `duplicata-events`.
- Outbox é uma evolução posterior, não requisito da primeira publicação Kafka.

## Testes

A mudança só está concluída quando o comportamento relevante estiver testado.

Camadas:

1. Domínio: testes unitários rápidos, sem Quarkus.
2. Aplicação: casos de uso com fakes ou mocks pequenos.
3. REST e persistência: `@QuarkusTest` e RestAssured.
4. Kafka: testes de integração apenas quando Kafka for introduzido.

Práticas:

- Testar transições válidas e inválidas.
- Testar regras, não getters triviais.
- Usar Arrange, Act, Assert.
- Nomear testes pelo comportamento.
- Buscar cobertura alta no domínio e aplicação.
- Escrever testes resistentes a mutation testing.
- Não alterar a regra apenas para fazer o teste passar.

## Comandos do projeto

Detecte o sistema operacional e use o Maven Wrapper apropriado:

```bash
./mvnw quarkus:dev
./mvnw test
./mvnw verify
```

No Windows:

```powershell
.\mvnw.cmd quarkus:dev
.\mvnw.cmd test
.\mvnw.cmd verify
```

## Definition of Done

Antes de considerar uma tarefa concluída:

- Compilação passa.
- Testes relevantes passam.
- Migration e entidade estão compatíveis.
- Não existem imports ou código morto introduzidos.
- Erros de negócio possuem resposta coerente.
- A regra está no domínio ou caso de uso correto.
- A implementação foi explicada ao usuário.
- O próximo passo não foi implementado antecipadamente.

## Documentação do laboratório

Leia estes arquivos quando precisar de contexto:

- `docs/ROADMAP.md`
- `docs/DOMAIN.md`
- `docs/DECISIONS.md`

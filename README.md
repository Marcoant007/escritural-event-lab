# escritural-event-lab

![Java](https://img.shields.io/badge/Java-21-orange)
![Quarkus](https://img.shields.io/badge/Quarkus-3.37-blue)
![Maven](https://img.shields.io/badge/build-Maven-C71A36)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Kafka](https://img.shields.io/badge/Kafka-SmallRye%20Reactive%20Messaging-231F20)

Laboratório pessoal de estudo do ecossistema **Quarkus**: arquitetura hexagonal, eventos de domínio, mensageria com Kafka e infraestrutura evoluindo em fatias pequenas e testadas.

O objetivo aqui não é construir um produto — é entender, na prática, um recurso do Quarkus por vez (Dev Services, Reactive Messaging, Fault Tolerance, Scheduler...), sempre apoiado em testes e em uma arquitetura que mantém o domínio isolado de framework.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Quarkus 3.37 |
| Build | Maven (wrapper) |
| Persistência | PostgreSQL + Flyway + Hibernate ORM (Panache) |
| Mensageria | Kafka via SmallRye Reactive Messaging |
| Documentação de API | OpenAPI / Swagger UI |
| Testes | JUnit 5, RestAssured, Dev Services (containers efêmeros via Testcontainers) |
| Boilerplate | Lombok |

## Arquitetura

Monólito modular com arquitetura hexagonal leve — o domínio não conhece Quarkus, JPA, REST ou Kafka:

```text
domain/          regras de negócio e eventos, Java puro
application/     casos de uso, orquestração, portas (in/out)
adapters/
├── in/web/      REST, DTOs, exception mappers
└── out/
    ├── persistence/   Hibernate + Panache, entidades JPA
    └── messaging/     producer/consumer Kafka
```

Casos de uso dependem de portas (`application/ports`); adapters implementam essas portas. Isso permite trocar Postgres por outro banco, ou Kafka por outro broker, sem tocar em regra de negócio.

## O que já foi explorado

- API REST com um fluxo de estados protegido por validações e mapeamento explícito de erros (400/404/409)
- Eventos de domínio internos com trilha de auditoria persistida
- Producer Kafka publicando eventos de domínio, com chave de particionamento consciente
- Testes de integração usando Dev Services do Quarkus (Postgres e Kafka efêmeros, sem infraestrutura manual)
- Documentação de API via OpenAPI/Swagger

O roadmap completo de fases de aprendizado está em [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Rodando localmente

Subir dependências (Postgres, Kafka, Kafka UI):

```shell
docker compose up -d
```

Rodar em modo dev (live coding):

```shell
./mvnw quarkus:dev
```

Com a aplicação rodando:

- Swagger UI: <http://localhost:8080/q/swagger-ui>
- OpenAPI (JSON/YAML): <http://localhost:8080/q/openapi>
- Dev UI: <http://localhost:8080/q/dev/>

## Testes

```shell
./mvnw test
```

Os testes de integração sobem Postgres e Kafka automaticamente via Dev Services — não é necessário ter o `docker compose` no ar para rodar a suite.

## Empacotamento

```shell
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Build nativo (requer GraalVM ou `-Dquarkus.native.container-build=true`):

```shell
./mvnw package -Dnative
```

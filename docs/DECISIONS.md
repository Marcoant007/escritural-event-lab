# Decisões arquiteturais

## 1. Monólito modular primeiro

Decisão: manter um único serviço até existir um motivo de aprendizado para separar.

Motivo: reduzir infraestrutura e permitir foco em Quarkus, domínio e mensageria.

## 2. Arquitetura hexagonal leve

Decisão: domínio e aplicação independentes de adapters, sem criar abstrações genéricas.

Motivo: praticar isolamento sem transformar o projeto em uma coleção de interfaces sem valor.

## 3. Hibernate ORM imperativo

Decisão: usar Hibernate ORM com JDBC e Panache Repository.

Motivo: é adequado para o primeiro laboratório e torna explícito que acesso ao banco é bloqueante.

Hibernate Reactive poderá ser comparado em um experimento futuro.

## 4. Flyway controla o schema

Decisão: usar migrations desde a primeira tabela e Hibernate em `validate`.

Motivo: aprender evolução explícita, reproduzível e versionada do banco.

## 5. Kafka entra depois do núcleo

Decisão: implementar primeiro os casos de uso e eventos internos.

Motivo: Kafka não deve esconder um domínio ainda instável.

## 6. Entrega at-least-once

Decisão: assumir redelivery e construir consumidores idempotentes.

Motivo: é um modelo realista e didático para processamento distribuído.

## 7. Testes por comportamento

Decisão: testes unitários no domínio, testes de aplicação com fakes e integração apenas nas bordas.

Motivo: manter feedback rápido e evitar usar `@QuarkusTest` para tudo.

## 8. Domínio propositalmente simplificado

Decisão: usar uma máquina de estados única no início.

Motivo: aprender progressivamente. A separação entre aceite, liquidação, protesto e titularidade será uma refatoração futura consciente.

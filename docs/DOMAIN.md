# Domínio simplificado

## Propósito

Este modelo existe para ensinar Quarkus e sistemas orientados a eventos. Ele representa conceitos inspirados em duplicata escritural, mas não pretende cobrir integralmente leis, regulações, interoperabilidade ou regras de uma escrituradora autorizada.

## Convenção de nomenclatura

Decisão: o código usa identificadores em inglês; este documento descreve os conceitos em português com o identificador de código entre parênteses.

## Entidade principal

### Duplicata (`Invoice`)

Atributos iniciais:

- id (`id`);
- número (`number`);
- documento do sacador (`issuerDocument`);
- documento do sacado (`payerDocument`);
- valor (`amount`);
- data de emissão (`issueDate`);
- data de vencimento (`dueDate`);
- status (`status`);
- data de criação (`createdAt`);
- data de atualização (`updatedAt`).

Invariantes iniciais:

- valor deve ser maior que zero;
- sacador e sacado devem estar informados;
- data de vencimento não pode ser anterior à emissão;
- número não pode ser vazio;
- uma duplicata nasce como `EMITIDA`.

## Estados

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

## Transições

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

A duplicata não deve expor `setStatus`.

## Casos de uso por ordem

1. Emitir duplicata.
2. Consultar duplicata.
3. Apresentar duplicata.
4. Aceitar duplicata.
5. Recusar duplicata.
6. Registrar histórico.
7. Publicar eventos.
8. Registrar pagamento.
9. Marcar vencimento.
10. Solicitar protesto.

## Eventos iniciais

- `DuplicataEmitida`;
- `DuplicataApresentada`;
- `DuplicataAceita`;
- `DuplicataRecusada`;
- `DuplicataPaga`;
- `DuplicataVencida`;
- `ProtestoSolicitado`;
- `DuplicataProtestada`.

Evento descreve um fato que já ocorreu. Não usar nomes imperativos como `AceitarDuplicataEvent`.

## Fora do escopo inicial

- aceite presumido jurídico;
- cessão;
- endosso;
- ônus;
- aval;
- pagamento parcial;
- múltiplos titulares;
- integração real com B3;
- protesto real;
- assinatura digital;
- requisitos regulatórios.

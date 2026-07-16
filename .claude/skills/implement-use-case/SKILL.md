---
name: Implement Use Case
description: Implementa uma única fatia vertical de negócio em arquitetura hexagonal, com domínio, aplicação, adapter e testes.
argument-hint: "<caso de uso>"
disable-model-invocation: true
---

Implemente somente este caso de uso: $ARGUMENTS

Fluxo obrigatório:

1. Inspecione implementações semelhantes no repositório.
2. Descreva os cenários:
   - caminho feliz;
   - recurso inexistente;
   - estado inválido;
   - duplicidade ou concorrência, quando aplicável.
3. Defina a mudança mínima no domínio.
4. Defina ou reutilize a porta necessária.
5. Implemente o caso de uso.
6. Implemente apenas os adapters indispensáveis.
7. Crie ou ajuste migration somente se o armazenamento mudar.
8. Crie testes de domínio, aplicação e integração proporcionais à mudança.
9. Execute os testes relevantes e depois a suíte.
10. Revise imports, transação, validações e mapeamentos.
11. Explique o fluxo completo, da entrada ao banco, e o papel do Quarkus em cada etapa.
12. Não implemente o próximo caso de uso.

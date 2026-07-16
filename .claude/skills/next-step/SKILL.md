---
name: Next Step
description: Analisa o estado atual do laboratório e escolhe a próxima menor tarefa de aprendizado, sem pular fases.
argument-hint: "[opcional: implemente]"
disable-model-invocation: true
---

Analise o repositório e determine o próximo passo do laboratório.

1. Leia o roadmap e o código atual.
2. Verifique testes, migrations e pendências reais.
3. Escolha exatamente uma tarefa pequena.
4. Explique:
   - objetivo;
   - conceito de Quarkus estudado;
   - arquivos que serão envolvidos;
   - critérios de conclusão;
   - riscos ou dúvidas.
5. Se `$ARGUMENTS` contiver `implemente`, execute a tarefa, escreva testes e valide o resultado.
6. Caso contrário, apenas apresente a proposta, sem alterar arquivos.
7. Não pule para Kafka, scheduler ou resiliência antes de o núcleo anterior estar concluído.

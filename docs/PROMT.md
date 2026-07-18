Você é o agente oficial deste projeto.

Antes de executar qualquer tarefa:

1. Leia docs/AI_RULES.md.
2. Leia README.md.
3. Analise a estrutura atual do projeto.
4. Identifique padrões já utilizados.
5. Siga os padrões existentes.
6. Nunca introduza tecnologias diferentes sem autorização.
7. Sempre explique brevemente o plano antes de modificar arquivos.
8. Mantenha um histórico das decisões em docs/DECISIONS.md.
9. Ao finalizar uma tarefa, registre:
    - O que foi alterado.
    - Motivo da alteração.
    - Arquivos modificados.
    - Próximos passos recomendados.

Toda nova sessão deve consultar AI_RULES.md e DECISIONS.md antes de iniciar.

Leia docs/AI_RULES.md, docs/CONTEXT.md, docs/DECISIONS.md e docs/TODO.md.

Entenda o projeto antes de agir.

Após cada tarefa:
- Atualize docs/CONTEXT.md.
- Atualize docs/DECISIONS.md se houver decisão arquitetural.
- Atualize docs/TODO.md.
- Mantenha os arquivos concisos.

Prompt:
Vou gerar uma nova versão do aplicativo. Para isso interprete todos os commits desde o dia Jul 10, 2026 e também docs/DECISIONS.md. Então você vai colocar as notas de atualização lá em resources/updates.json.
A nova atualização será v1.1.0 e você vai alterar em Main.version também e também no gradle.properties. E dado isso, salve essas etapas em docs/CONTEXT.md ou no docs/AI_RULES.md para que sempre que eu pedir para atualizar o projeto, o agente deverá seguir as etapas descritas.

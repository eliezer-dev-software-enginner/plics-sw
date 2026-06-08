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
lá no método "Entrar" em AuthScreenViewModel o usuario pode ter usado a licensa "QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ" até o dia 11 (inclusive), pois essa é uma licensa de teste. Após esse prazo essa licensa não estará mais válida e então ele não poderá acessar mais. Inclusive na HomeScreen no método onMount devemos verificar se a licensa salva é a de teste e se está vencida, se for o caso ele será redirecionado para a tela AuthScreen.
E então após a implementação, crie testes.

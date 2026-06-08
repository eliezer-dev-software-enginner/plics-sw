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
ao clicar em Finalizar Venda sem ter selecionado "venda fiada", estou com esse erro abaixo, e acho que sei o problema, como não selecionei um cliente por não ter clicado em venda fiada, clente selecionado é nulo, mas na verdade era para ter usado o cliente padrão:
Exception in thread "" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "clienteId" is null
at my_app.screens.pdvScreen.PDVScreenViewModel.lambda$finalizarVenda$0(PDVScreenViewModel.java:206)
at java.base/java.util.concurrent.ThreadPerTaskExecutor$TaskRunner.run(ThreadPerTaskExecutor.java:291)
at java.base/java.lang.VirtualThread.run(VirtualThread.java:456)
Exception in thread "" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "clienteId" is null
at my_app.screens.pdvScreen.PDVScreenViewModel.lambda$finalizarVenda$0(PDVScreenViewModel.java:206)
at java.base/java.util.concurrent.ThreadPerTaskExecutor$TaskRunner.run(ThreadPerTaskExecutor.java:291)
at java.base/java.lang.VirtualThread.run(VirtualThread.java:456)
Exception in thread "" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "clienteId" is null
at my_app.screens.pdvScreen.PDVScreenViewModel.lambda$finalizarVenda$0(PDVScreenViewModel.java:206)
at java.base/java.util.concurrent.ThreadPerTaskExecutor$TaskRunner.run(ThreadPerTaskExecutor.java:291)
at java.base/java.lang.VirtualThread.run(VirtualThread.java:456)
Exception in thread "" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "clienteId" is null
at my_app.screens.pdvScreen.PDVScreenViewModel.lambda$finalizarVenda$0(PDVScreenViewModel.java:206)
at java.base/java.util.concurrent.ThreadPerTaskExecutor$TaskRunner.run(ThreadPerTaskExecutor.java:291)
at java.base/java.lang.VirtualThread.run(VirtualThread.java:456)

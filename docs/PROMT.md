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
Quero que você crie um arquivo no root do projeto em markdown chamado testes. Onde eu como usuário vou poder testar o aplicativo. 
Por exemplo, se eu for testar cliente, você vai fornecer nome de teste, dados que a tela ClienteScreen pede. E claro terá testes para validar as entradas, com efeito esperado. 
No exemplo de cliente, poderia ter casos de uso: "Se preencher todos os campos" a aplicação deve aceitar". Se não preencher nome a aplicação não deve deixar. Deve mostrar alert. 
Você pode simular alguns perfis de negócios para poder testar vários fluxos. Por exemplo, você pode criar testes para uma "loja de roupa", testes para uma "casa de ração", testes para "uma lanchonete", testes para um "açougue", testes para um "Mercado" e etc...
Para cada teste posso ter um campo reservado para eu preencher se a aplicação teve algum erro, ou inconsistencia para poder resolver posteriormente.


Nesse trecho abaixo na HomeScreen, após a navegação ser realizada o conteudo desenhado ainda é o da HomeScreen, analise para mim o porque isso está acontecendo:
@Override
public void onMount() {
if (viewModel.isLicensaTesteExpirada()) {
ctx.navigate("entrar-com-credenciais");
return;
}
viewModel.calcularFinanceiroMesAtual();
}
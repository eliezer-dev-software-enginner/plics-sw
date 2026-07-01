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
Estou enfrentando um problema com o requestFocus() no input de quantidade.
Após selecionar um produto era para ele ficar com o cursor nele piscando e isso não acontece.
Analise ComprasScreen e ComprasScreenViewModel o reqst acontece no método selecionarProduto() eu já validei que o método requestFocus() está sendo chamado corretamente.

    private Row formSecondRow() {
        Component quantidadeInput = Components.InputColumnDecimal("Quantidade", vm.qtd, "Ex: 1,500",vm.quantidadeRef);

        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(quantidadeInput)
                .r_child(Components.InputColumnCurrency("Desconto em R$", vm.descontoEmDinheiro))
                .r_child(Components.SelectColumn("Tipo de pagamento",Data.tiposPagamentoList, vm.tipoPagamentoSelected, it -> it))
                .r_child(Components.SelectColumn("Refletir no estoque?",Data.simNaoList, vm.opcaoEstoqueSelected, it -> it))
                .r_child(Components.TextAreaColumn("Observação", vm.observacao, ""));
    }
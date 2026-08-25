package my_app.screens.pedidosScreen.details;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.components.layout_components.Stack;
import megalodonte.props.ButtonProps;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.domain.Data;
import my_app.domain.components.Components;
import my_app.screens.pdvScreen.ItemVenda;

public class PedidoDetails implements ScreenComponent {

    private final ScreenContext ctx;
    private final PedidoTrocaViewModel vm;
    private final Integer pedidoId;

    public PedidoDetails(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new PedidoTrocaViewModel(ctx);
        this.pedidoId = Integer.parseInt(ctx.getParams().get("id"));
    }

    @Override
    public void onMount() {
        vm.preparar(pedidoId);
    }

    @Override
    public Component render() {
        return new Stack().children(
                Components.ScrollPaneDefault(
                        new Container(new ContainerProps().fillHeight()).children(
                                new Card(new Column().children(
                                        Components.FormTitle("Trocar venda #" + pedidoId),
                                        new SpacerVertical(5),
                                        new TextFlow(Components.FormSubtitle(
                                                "Quer trocar algum produto desta venda? Clique no produto que deseja substituir, escolha o novo produto e confirme a troca. O produto devolvido volta automaticamente para o estoque."
                                        )),
                                        new SpacerVertical(10),
                                        Show.when(vm.trocaSearchVisible, () -> new Row().children(
                                                new Column().children(
                                                        new Text("Buscando substituto para o produto selecionado "),
                                                        Components.SelectDropDownSearch("Buscar produto para troca", vm.trocaBuscaInput, "Nome ou código...",
                                                                vm.trocaSugestoes, vm.trocaProdutoEncontrado, vm.trocaSugestoesVisiveis),
                                                        new SpacerVertical(5),
                                                        new Row(new RowProps().spacingOf(10)).children(
                                                                Show.when(vm.trocaShowPesquisarBtn, () ->
                                                                        new Button("Pesquisar produto").onClick(() -> {
                                                                            vm.trocaBuscaInput.set("");
                                                                            vm.trocaQuantidadeInput.set("1");
                                                                            vm.trocaProdutoEncontrado.set(null);
                                                                        })
                                                                ),
                                                                Show.when(vm.trocaShowTrocarBtn, () ->
                                                                        new Button("Trocar").onClick(() -> {
                                                                            vm.handleTrocaPesquisarClick();
                                                                            vm.updateTrocaBtnStates();
                                                                        })
                                                                )
                                                        )
                                                ),
                                                new SpacerHorizontal().fill()
                                        )),
                                        new SpacerVertical(10),
                                        Components.FormTitle("Produtos da troca (clique para substituir)"),
                                        new SimpleTable<ItemVenda>()
                                                .fromData(vm.trocaItens)
                                                .header()
                                                .columns()
                                                .column("Cod", it -> it.produto.getCodigoBarras())
                                                .column("Nome", it -> it.produto.getDescricao())
                                                .column("Qtd.", it -> it.quantidade)
                                                .column("Vl. Unit.", it -> it.produto.getPrecoVenda())
                                                .column("Total", ItemVenda::totalItem)
                                                .build()
                                                .onItemSelectChange(vm.trocaItemSelected::set)
                                ))))
                )
                .fillHeight()
                .childInCorner(
                new Row(new RowProps().spacingOf(10).hugWidth().paddingRight(30)).children(
                        Components.SelectColumn("Forma de pagamento", Data.tiposPagamentoList, vm.trocaFormaPagamento, it -> it),
                        new Button("Confirmar troca", new ButtonProps().success().height(40)).onClick(() -> vm.confirmarTroca(() -> ctx.selfStage().close()))
                ),
                Stack.Corner.BOTTOM_RIGHT,
                10
        );
    }
}

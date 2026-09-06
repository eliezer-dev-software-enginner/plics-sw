package my_app.screens.pedidosScreen;

import disgust.io.ButtonsPack;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ButtonVariant;
import megalodonte.props.ContainerProps;
import megalodonte.props.FlowRowProps;
import megalodonte.props.RowProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.AppRoutes;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.domain.components.Components;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilled;
import org.kordamp.ikonli.entypo.Entypo;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

public class PedidosScreen implements ScreenComponent {

    private final ScreenContext ctx;
    private final PedidosScreenViewModel vm;

    public PedidosScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new PedidosScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public Component render() {
        return new Container(new ContainerProps().paddingAll(10)).children(
                Components.FormTitle("Histórico do Caixa — Vendas PDV"),
                new SpacerVertical(10),
                Components.searchInputFill(vm.searchState, "Pesquisar por cliente ou forma de pagamento"),
                new SpacerVertical(10),
                new Row(new megalodonte.props.RowProps().spacingOf(10)).children(
                        new Column(new megalodonte.props.ColumnProps().fillWidth()).children(pedidosTable()),
                        new Column(new megalodonte.props.ColumnProps().fillWidth()).children(itensDoPedidoSelecionado())
                )
        );
    }

    Component pedidosTable() {
        return new SimpleTable<PedidoModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID",           it -> "#" + it.getId())
                .column("Cliente",      vm::nomeClienteDoPedido)
                .column("Total",        it -> CurrencyPack.toBRLCurrency(it.getTotalLiquido()))
                .column("Pagamento", PedidoModel::getFormaPagamento)
                .column("Fiado?",       it -> it.getFiado() != null && it.getFiado() == 1 ? "Sim" : "Não")
                .column("Data",         it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .column("Status",       it -> Boolean.TRUE.equals(it.getDevolvida()) ? "Devolvida" : "-")
                .build()
                .onItemSelectChange(vm.pedidoSelecionado::set);
    }

    Component itensDoPedidoSelecionado() {
        return new Card(new Column().children(
                Components.FormSubtitle("Itens da venda selecionada"),
                new SpacerVertical(10),
                Show.when(vm.temPedidoSelecionado, () ->
                        new FlowRow(new FlowRowProps().spacingOf(10)).children(
                                //new Button("Imprimir venda").onClick(vm::imprimirVendaSelecionada),
                                ButtonsPack.ContainedButtonWithIconStart("Imprimir venda", ButtonVariant.PRIMARY,
                                        AntDesignIconsFilled.PRINTER,
                                        vm::imprimirVendaSelecionada),
                                ButtonsPack.ContainedButtonWithIconStart("Excluir venda selecionada", ButtonVariant.DANGER,
                                                AntDesignIconsFilled.DELETE, vm::handleClickMenuDelete)
                        )
                ),
                Show.when(vm.podeDevolver, () ->
                        new Row(new RowProps().spacingOf(10)).children(
                                ButtonsPack.ContainedButtonWithIconStart("Devolver venda selecionada", ButtonVariant.WARNING,
                                        Entypo.BACK_IN_TIME,
                                        vm::handleClickMenuDevolucaoVenda),
                                ButtonsPack.ContainedButtonWithIconStart("Trocar venda selecionada", ButtonVariant.SUCCESS,
                                        Entypo.SWAP,
                                        this::handleClickMenuTroca)
                        )
                ),
                new SimpleTable<PedidoItemModel>()
                        .fromData(vm.itensDoPedidoSelecionado)
                        .header()
                        .columns()
                        .column("Produto", PedidoItemModel::getProdutoCod)
                        .column("Qtd.", PedidoItemModel::getQuantidade)
                        .column("Vl. Unit.", it -> CurrencyPack.toBRLCurrency(it.getPrecoUnitario()))
                        .column("Total",     it -> CurrencyPack.toBRLCurrency(it.getTotalItem()))
                        .build(),
                new SpacerVertical(15)

        ));
    }

    void handleClickMenuTroca() {
        var pedido = vm.pedidoSelecionado.get();
        if (pedido == null) return;
        ctx.router().spawnWindow(AppRoutes.Screens.PEDIDO_DETAILS.name() + "/" + pedido.getId(), e -> {});
    }
}

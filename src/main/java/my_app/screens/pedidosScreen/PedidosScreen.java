package my_app.screens.pedidosScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.Card;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class PedidosScreen implements ScreenComponent {

    private final PedidosScreenViewModel vm;

    public PedidosScreen(ScreenContext ctx) {
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
                Components.searchInput(vm.searchState, "Pesquisar por cliente ou forma de pagamento"),
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
                .column("Total",        it -> Utils.toBRLCurrency(it.getTotalLiquido()))
                .column("Pagamento", PedidoModel::getFormaPagamento)
                .column("Fiado?",       it -> it.getFiado() != null && it.getFiado() == 1 ? "Sim" : "Não")
                .column("Data",         it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.pedidoSelecionado::set);
    }

    Component itensDoPedidoSelecionado() {
        return new Card(new Column().children(
                Components.FormTitle("Itens da venda selecionada"),
                new SpacerVertical(10),
                new SimpleTable<PedidoItemModel>()
                        .fromData(vm.itensDoPedidoSelecionado)
                        .header()
                        .columns()
                        .column("Produto", PedidoItemModel::getProdutoCod)
                        .column("Qtd.", PedidoItemModel::getQuantidade)
                        .column("Vl. Unit.", it -> Utils.toBRLCurrency(it.getPrecoUnitario()))
                        .column("Total",     it -> Utils.toBRLCurrency(it.getTotalItem()))
                        .build(),
                new SpacerVertical(15),
                Show.when(vm.temPedidoSelecionado, () ->
                        new Row(new megalodonte.props.RowProps().spacingOf(10)).children(
                                new Button("Imprimir venda").onClick(vm::imprimirVendaSelecionada),
                                new Button("Excluir venda selecionada").onClick(vm::handleClickMenuDelete)
                        )
                )
        ));
    }
}

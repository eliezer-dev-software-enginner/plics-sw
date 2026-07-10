package my_app.screens.pedidosScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.SimpleTable;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.router.v4.ScreenContext;
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
        vm.loadPedidos();
    }

    @Override
    public Component render() {
        return new Container()
                .children(
                        Components.FormTitle("Vendas PDV"),
                        new Row().children(
                                pedidosTable(),
                                itensDoPedidoSelecionado()
                        )
                );
    }

    Component pedidosTable() {
        return new SimpleTable<PedidoModel>()
                .fromData(vm.pedidos)
                .header()
                .columns()
                .column("ID",           it -> "#" + it.getId())
                .column("Cliente",      it -> it.getClienteId() != null ? it.getClienteId().toString() : "Consumidor")
                .column("Total",        it -> Utils.toBRLCurrency(it.getTotalLiquido()))
                .column("Pagamento", PedidoModel::getFormaPagamento)
                .column("Fiado?",       it -> it.getFiado() != null && it.getFiado() == 1 ? "Sim" : "Não")
                .column("Data",         it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.pedidoSelecionado::set);
    }

    Component itensDoPedidoSelecionado() {
        return new Column().children(
                Components.FormTitle("Itens do pedido"),
                new SimpleTable<PedidoItemModel>()
                        .fromData(vm.itensDoPedidoSelecionado)
                        .header()
                        .columns()
                        .column("Produto", PedidoItemModel::getProdutoCod)
                        .column("Qtd.", PedidoItemModel::getQuantidade)
                        .column("Vl. Unit.", it -> Utils.toBRLCurrency(it.getPrecoUnitario()))
                        .column("Total",     it -> Utils.toBRLCurrency(it.getTotalItem()))
                        .build()
        );
    }
}

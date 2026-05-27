package my_app.screens.pedidosScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.SimpleTable;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models_old.PedidoItemModel;
import my_app.db.models_old.PedidoModel;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

// PedidosScreen.java
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
                .column("ID",           it -> "#" + it.id)
                .column("Cliente",      it -> it.clienteId != null ? it.clienteId.toString() : "Consumidor")
                .column("Total",        it -> Utils.toBRLCurrency(it.totalLiquido))
                .column("Pagamento",    it -> it.formaPagamento)
                .column("Fiado?",       it -> it.isFiado ? "Sim" : "Não")
                .column("Data",         it -> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
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
                        .column("Produto",   it -> it.produtoCod)
                        .column("Qtd.",      it -> it.quantidade)
                        .column("Vl. Unit.", it -> Utils.toBRLCurrency(it.precoUnitario))
                        .column("Total",     it -> Utils.toBRLCurrency(it.totalItem))
                        .build()
        );
    }
}
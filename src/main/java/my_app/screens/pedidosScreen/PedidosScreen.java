package my_app.screens.pedidosScreen;

import javafx.stage.Stage;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.Card;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.domain.Data;
import my_app.domain.components.Components;
import my_app.screens.pdvScreen.ItemVenda;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;

public class PedidosScreen implements ScreenComponent {

    private final ScreenContext ctx;
    private final PedidosScreenViewModel vm;
    private Stage stageTroca;

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
                .column("Status",       it -> Boolean.TRUE.equals(it.getDevolvida()) ? "Devolvida" : "-")
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
                        new Row(new RowProps().spacingOf(10)).children(
                                new Button("Imprimir venda").onClick(vm::imprimirVendaSelecionada),
                                new Button("Excluir venda selecionada").onClick(vm::handleClickMenuDelete)
                        )
                ),
                new SpacerVertical(15),
                Show.when(vm.podeDevolver, () ->
                        new Row(new RowProps().spacingOf(10)).children(
                                new Button("Devolver venda selecionada").onClick(vm::handleClickMenuDevolucaoVenda),
                                new Button("Trocar venda selecionada").onClick(this::handleClickMenuTroca)
                        )
                )
        ));
    }

    void handleClickMenuTroca() {
        vm.prepararTroca(() -> stageTroca = Components.ShowModal(trocaContent(), ctx, 640));
    }

    Component trocaContent() {
        return new Card(new Column().children(
                Components.FormTitle("Trocar venda selecionada"),
                new SpacerVertical(5),
                new Text("Os produtos da venda original voltam ao estoque e ela fica registrada como devolvida. "
                        + "Adicione abaixo os produtos novos que o cliente vai levar."),
                new SpacerVertical(10),
                Components.FormTitle("Produtos da venda original"),
                new SimpleTable<PedidoItemModel>()
                        .fromData(vm.trocaItensOriginais)
                        .header()
                        .columns()
                        .column("Produto", PedidoItemModel::getProdutoCod)
                        .column("Qtd.", PedidoItemModel::getQuantidade)
                        .column("Vl. Unit.", it -> Utils.toBRLCurrency(it.getPrecoUnitario()))
                        .column("Total", it -> Utils.toBRLCurrency(it.getTotalItem()))
                        .build(),
                new SpacerVertical(10),
                Components.FormTitle("Buscar produto novo"),
                Components.SelectDropDownSearch("Buscar produto", vm.trocaBuscaInput, "Nome ou código...",
                        vm.trocaSugestoes, vm.trocaProdutoEncontrado, vm.trocaSugestoesVisiveis),
                new Row(new RowProps().spacingOf(10)).children(
                        Components.InputColumnComEnterHandler("Quantidade", vm.trocaQuantidadeInput, "Ex: 1",
                                vm::adicionarItemTroca),
                        new Button("Adicionar item").onClick(vm::adicionarItemTroca)
                ),
                new SpacerVertical(10),
                Components.FormTitle("Itens da troca"),
                new SimpleTable<ItemVenda>()
                        .fromData(vm.trocaItens)
                        .header()
                        .columns()
                        .column("Cod", it -> it.produto.getCodigoBarras())
                        .column("Nome", it -> it.produto.getDescricao())
                        .editableColumn("Qtd.", it -> it.quantidade,
                                (it, val) -> {
                                    try {
                                        vm.atualizarQuantidadeItemTroca(it, new BigDecimal(val));
                                    } catch (NumberFormatException e) {
                                        // valor inválido, ignora
                                    }
                                })
                        .column("Vlr. Unit.", it -> it.produto.getPrecoVenda())
                        .column("Total", ItemVenda::totalItem)
                        .end()
                        .build(),
                new SpacerVertical(10),
                Components.SelectColumn("Forma de pagamento", Data.tiposPagamentoList, vm.trocaFormaPagamento, it -> it),
                new SpacerVertical(5),
                new Button("Confirmar troca").onClick(() -> vm.confirmarTroca(() -> stageTroca.close()))
        ));
    }
}

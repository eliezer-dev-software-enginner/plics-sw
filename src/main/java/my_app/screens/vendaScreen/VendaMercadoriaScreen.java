package my_app.screens.vendaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import megalodonte.components.layout_components.Row;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import megalodonte.components.SimpleTable;
import megalodonte.props.RowProps;
import my_app.db.models.VendaModel;

public class VendaMercadoriaScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final VendaMercadoriaScreenViewModel vm;

    public VendaMercadoriaScreen(ScreenContext ctx) {
        this.vm = new VendaMercadoriaScreenViewModel(ctx);
    }

    @Override
    public void onMount() { vm.fetchData(); }

    @Override
    public Component render() { return mainView(vm.focusState); }

    @Override
    public Component form() {
        return new Column(new ColumnProps().spacingOf(10)).children(
                Components.FormTitle("Cadastrar Nova Venda"),
                new SpacerVertical(20),
                formFirstRow(),
                formSecondRow(),
                new Row(new RowProps().spacingOf(15))
                        .r_child(Components.TextWithValue("Estoque anterior:", vm.estoqueAnterior))
                        .r_child(Components.TextWithValue("Estoque após venda:", vm.estoqueAtual)),
                Components.displayOperationsRow(vm.totais),
                Components.aPrazoForm(vm.parcelas, vm.tipoPagamentoIsAPrazo, vm.totais.totalLiquido),
                Components.actionButtons(vm.btnText, this::handleAddOrUpdate, vm::clearForm)
        );
    }

    private Row formSecondRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.InputColumnCurrency("Pc. de venda", vm.pcVenda))
                .r_child(Components.InputColumnCurrency("Desconto em R$", vm.descontoEmDinheiro))
                .r_child(Components.SelectColumn("Tipo de pagamento",
                        Data.tiposPagamentoList, vm.tipoPagamentoSelecionado, it -> it))
                .r_child(Components.SelectColumn("Refletir no estoque?",
                        Data.simNaoList, vm.opcaoEstoqueSelected, it -> it))
                .r_child(Components.TextAreaColumn("Observação", vm.observacao, ""));
    }

    private Row formFirstRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10)).children(
                Components.DatePickerColumn(vm.dataVenda, "Data de venda"),
                Components.SelectColumn("Cliente", vm.clientes, vm.clienteSelected, f -> f.getNome(), true),
                Components.InputColumn("N NF/Pedido compra", vm.numeroNota, "Ex: 12345678920"),
                Components.InputColumnComDynamicSearch("Código do produto", vm.codigo, "xxxxxxxx",
                        vm.sugestoesProduto, vm.produtoEncontrado, vm.sugestoesProdutoVisible),
                Components.InputColumnDecimal("Quantidade", vm.qtd, "Ex: 2"),
                Components.InputColumn("Descrição do produto",
                        vm.produtoEncontrado.map(p -> p != null ? p.getDescricao() : ""),
                        "Ex: Paraiso", true)
        );
    }

    @Override
    public Component table() {
        return new SimpleTable<VendaModel>()
                .fromData(vm.vendas)
                .header()
                .columns()
                .column("ID", it -> it.getId())
                .column("Quantidade", it -> it.getQuantidade())
                .column("Total líquido", it -> Utils.toBRLCurrency(it.getTotalLiquido()))
                .column("Data", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.vendaSelected::set);
    }

    @Override
    public ViewModelScreenContract viewModel() { return vm; }
}

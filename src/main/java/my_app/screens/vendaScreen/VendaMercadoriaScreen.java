package my_app.screens.vendaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.domain.ContratoTelaCrudV2;
import my_app.domain.ContratoTelaCrudV3;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import megalodonte.components.Scroll;
import megalodonte.components.SimpleTable;
import megalodonte.props.RowProps;
import my_app.db.models.VendaModel;

public class VendaMercadoriaScreen implements ScreenComponent, ContratoTelaCrudV3 {

    private final VendaMercadoriaScreenViewModel vm;

    public VendaMercadoriaScreen(ScreenContext ctx) {
        this.vm = new VendaMercadoriaScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchData();
    }

    @Override
    public Component render() {
        return mainView(vm.focusState);
    }

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
                displayOperationsRow(),
                Components.aPrazoForm(vm.parcelas, vm.tipoPagamentoIsAPrazo, vm.totalLiquido),
                Components.actionButtons(vm.btnText, vm::handleAddOrUpdate, vm::clearForm)
        );
    }

    private Row displayOperationsRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", vm.totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", vm.descontoFormatado))
                .r_child(Components.TextWithValue("Total geral(líquido): ", vm.totalLiquido.map(Utils::toBRLCurrency)));
    }

    private Row formSecondRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.InputColumnCurrency("Pc. de venda", vm.pcVenda))
                .r_child(Components.InputColumnCurrency("Desconto em R$", vm.descontoEmDinheiro))
                .r_child(Components.SelectColumn("Tipo de pagamento",
                        vm.tiposPagamento, vm.tipoPagamentoSelecionado, it -> it))
                .r_child(Components.SelectColumn("Refletir no estoque?",
                        vm.opcoesEstoque, vm.opcaoEstoqueSelected, it -> it))
                .r_child(Components.TextAreaColumn("Observação", vm.observacao, ""));
    }

    private Row formFirstRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10)).children(
                Components.DatePickerColumn(vm.dataVenda, "Data de venda"),
                Components.SelectColumn("Cliente", vm.clientes, vm.clienteSelected, f -> f.nome, true),
                Components.InputColumn("N NF/Pedido compra", vm.numeroNota, "Ex: 12345678920"),
                Components.InputColumnComFocusHandler("Código", vm.codigo, "xxxxxxxx", vm::buscarProduto),
                Components.InputColumn("Quantidade", vm.qtd, "Ex: 2"),
                Components.InputColumn("Descrição do produto",
                        vm.produtoEncontrado.map(p -> p != null ? p.descricao : ""),
                        "Ex: Paraiso", true)
        );
    }

    @Override
    public Component table() {
        return new SimpleTable<VendaModel>()
                .fromData(vm.vendas)
                .header()
                .columns()
                .column("ID", it -> it.id)
                .column("Quantidade", it -> it.quantidade)
                .column("Total líquido", it -> Utils.toBRLCurrency(it.totalLiquido))
                .column("Data", it -> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(vm.vendaSelected::set);
    }

    @Override
    public ViewModelv2 viewModel() {
        return vm;
    }

    @Override
    public void handleClickNew() {
        vm.modoEdicao.set(false);
        vm.clearForm();
    }

    @Override
    public void handleAddOrUpdate() {
        vm.handleAddOrUpdate();
    }

    @Override
    public void clearForm() {
        vm.clearForm();
    }
}
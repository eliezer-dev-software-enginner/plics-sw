package my_app.screens.comprasScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.*;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.screens.components.Components;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import my_app.services.*;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

//TODO: finalizar implementações
//TODO: lista de compras para exibir na tabela
public class ComprasScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final ComprasScreenViewModel vm;

    public ComprasScreen(ScreenContext ctx) {
        this.vm = new ComprasScreenViewModel(ctx);
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
                Components.FormTitle("Cadastrar Nova Compra"),
                new SpacerVertical(20),
                formFirstRow(),
                formSecondRow(),
                new Row(new RowProps().spacingOf(15))
                        .r_child(Components.TextWithValue("Estoque anterior:", vm.estoqueAnterior))
                        .r_child(Components.TextWithValue("Estoque após compra:", vm.estoqueAtual)),
                displayOperationsRow(),
                Components.aPrazoForm(vm.parcelas, vm.tipoPagamentoSelectedIsAPrazo, vm.totalLiquido),
                Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm)
        );
    }

    private Row formFirstRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10)).children(
                Components.DatePickerColumn(vm.dataCompra, "Data de compra", "dd/mm/yyyy"),
                Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, f -> f.nome, true),
                Components.InputColumn("N NF/Pedido compra", vm.numeroNota, "Ex: 12345678920"),
                Components.InputColumnComDynamicSearch("Código do produto", vm.codigo, "xxxxxxxx",
                        vm.sugestoesProduto, vm.produtoEncontrado, vm.sugestoesProdutoVisible),
                Components.InputColumn("Descrição do produto", vm.produtoEncontrado.map(p -> p != null ? p.descricao : ""), "Ex: Paraiso",true),
                Components.InputColumnCurrency("Pc. de compra", vm.pcCompra)
        );
    }

    private Row formSecondRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.InputColumnDecimal("Quantidade", vm.qtd, "Ex: 1,500"))
                .r_child(Components.InputColumnCurrency("Desconto em R$", vm.descontoEmDinheiro))
                .r_child(Components.SelectColumn("Tipo de pagamento",Data.tiposPagamentoList, vm.tipoPagamentoSeleced, it -> it))
                .r_child(Components.SelectColumn("Refletir no estoque?",Data.simNaoList, vm.opcaoEstoqueSelected, it -> it))
                .r_child(Components.TextAreaColumn("Observação", vm.observacao, ""));
    }

    private Row displayOperationsRow() {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", vm.totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", vm.descontoComputed))
                .r_child(Components.TextWithValue("Total geral(líquido): ", vm.totalLiquido.map(Utils::toBRLCurrency)));
    }

    @Override
    public Component table() {
        return new SimpleTable<CompraModel>()
                .fromData(vm.compras)
                .header()
                .columns()
                .column("ID", it -> it.id, (double) 90)
                .column("Quantidade", it -> it.quantidade)
                .column("N. Nota", it -> it.numeroNota)
                .column("Fornecedor", it -> it.fornecedor == null ? "" : it.fornecedor.nome)
                .column("Total liq. de compra", it -> Utils.toBRLCurrency(it.totalLiquido))
                .column("Data de criação", it -> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(it -> vm.compraSelected.set(it));
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }
}
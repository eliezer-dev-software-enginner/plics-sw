package my_app.screens.comprasScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.CompraModel;
import my_app.db.models.FornecedorModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ComprasScreen implements ScreenComponent, ContratoTelaCrudV3<CompraModel> {
    private final ComprasScreenViewModel vm;
    private final ScreenContext ctx;

    public ComprasScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new ComprasScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public Component form() {
        return new Column(new ColumnProps().spacingOf(10)).children(
                Components.FormTitle("Cadastrar Nova Compra"),
                new SpacerVertical(ThemeManager.theme().spacing().lg()),
                formFirstRow(),
                formSecondRow(),
                new Row(new RowProps().spacingOf(15))
                        .r_child(Components.TextWithValue("Estoque anterior:", vm.estoqueAnterior))
                        .r_child(Components.TextWithValue("Estoque após compra:", vm.estoqueAtual)),
                Components.displayOperationsRow(vm.totais),
                Components.aPrazoForm(vm.parcelas, vm.tipoPagamentoSelectedIsAPrazo, vm.totais.totalLiquido),
                Components.actionButtons(vm.btnText, this::handleAddOrUpdate)
        );
    }

    @Override
    public Component itemDetails(CompraModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da compra", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Produto: ", model.getProdutoModel() != null ? model.getProdutoModel().getDescricao() : model.getProdutoCod()))
                .c_child(Components.TextWithDetails("- Código de barras: ", model.getProdutoCod()))
                .c_child(Components.TextWithDetails("Fornecedor: ", model.getFornecedor() != null ? model.getFornecedor().getNome() : "-"))
                .c_child(Components.TextWithDetails("Quantidade: ", Utils.quantidadeTratada(model.getQuantidade())))
                .c_child(Components.TextWithDetails("Preço de compra (unitário): ", Utils.toBRLCurrency(model.getPrecoDeCompra())))
                .c_child(Components.TextWithDetails("Desconto: ", Utils.toBRLCurrency(model.getDescontoEmReais())))
                .c_child(Components.TextWithDetails("Total líquido: ", Utils.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Forma de pagamento: ", model.getTipoPagamento()))
                .c_child(Components.TextWithDetails("Nº Nota: ", model.getNumeroNota()))
                .c_child(Show.when(model.getDataValidade() != null && model.getDataValidade() > 0,
                        () -> Components.TextWithDetails("Data de validade: ", DateUtils.millisToBrazilianDate(model.getDataValidade()))))
                .c_child(Components.TextWithDetails("Data da compra: ", DateUtils.millisToBrazilianDate(model.getDataCompra())))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.millisToBrazilianDateTime(model.getDataCriacaoMillis())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }

    private Component formFirstRow() {
        return new FlowRow(new FlowRowProps().spacingOf(10)).children(
                Components.DatePickerColumn(vm.dataCompra, "Data de compra"),
                Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, FornecedorModel::getNome, true),
                Components.InputColumn("N NF/Pedido compra", vm.numeroNota, "Ex: 12345678920"),
                Components.InputColumnComDynamicSearch("Código do produto", vm.codigo, "xxxxxxxx",
                        vm.sugestoesProduto, vm.produtoEncontrado, vm.sugestoesProdutoVisible),
                Components.InputColumn("Descrição do produto", vm.produtoEncontrado.map(p -> p != null ? p.getDescricao() : ""), "Ex: Paraiso",true),
                Components.InputColumnCurrency("Pc. de compra", vm.pcCompra)
        );
    }

    private Row formSecondRow() {
        Component quantidadeInput = Components.InputColumnDecimal("Quantidade", vm.qtd, "Ex: 1,500",vm.quantidadeRef);

        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(quantidadeInput)
                .r_child(Components.InputColumnCurrency("Desconto em R$", vm.descontoEmDinheiro))
                .r_child(Components.SelectColumn("Tipo de pagamento",Data.tiposPagamentoList, vm.tipoPagamentoSelected, it -> it))
                .r_child(Components.SelectColumn("Refletir no estoque?",Data.simNaoList, vm.opcaoEstoqueSelected, it -> it))
                .r_child(Components.TextAreaColumn("Observação", vm.observacao, "Alguma observação sobre esta compra?"));
    }

    @Override
    public SimpleTable table() {
        return new SimpleTable<CompraModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", CompraModel::getId, (double) 90)
                .column("Produto", it -> it.getProdutoModel().getDescricao(), (double) 90)
                .column("Quantidade", CompraModel::getQuantidade)
                .column("N. Nota", CompraModel::getNumeroNota)
                .column("Fornecedor", it -> it.getFornecedor() == null ? "" : it.getFornecedor().getNome())
                .column("Total liq. de compra", it -> Utils.toBRLCurrency(it.getTotalLiquido()))
                .column("Data de criação", it -> DateUtils.millisToBrazilianDateTime(it.getDataCriacaoMillis()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.compraSelected::set)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), ctx, 600));
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }
}

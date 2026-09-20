package my_app.screens.comprasScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.CompraModel;
import my_app.utils.Utils;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

public class ComprasScreen implements ScreenComponent, ScreenContract<CompraModel> {
    private final ComprasScreenViewModel vm;
    private final ScreenContextInterface ctx;

    public ComprasScreen(ScreenContextInterface ctx) {
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
    public Component itemDetails(CompraModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da compra", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Produto: ", model.getProdutoModel() != null ? model.getProdutoModel().getDescricao() : model.getProdutoCod()))
                .c_child(Components.TextWithDetails("- Código de barras: ", model.getProdutoCod()))
                .c_child(Components.TextWithDetails("Fornecedor: ", model.getFornecedor() != null ? model.getFornecedor().getNome() : "-"))
                .c_child(Components.TextWithDetails("Quantidade: ", Utils.quantidadeTratada(model.getQuantidade())))
                .c_child(Components.TextWithDetails("Preço de compra (unitário): ", CurrencyPack.toBRLCurrency(model.getPrecoDeCompra())))
                .c_child(Components.TextWithDetails("Desconto: ", CurrencyPack.toBRLCurrency(model.getDescontoEmReais())))
                .c_child(Components.TextWithDetails("Total líquido: ", CurrencyPack.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Forma de pagamento: ", model.getTipoPagamento()))
                .c_child(Components.TextWithDetails("Nº Nota: ", model.getNumeroNota()))
                .c_child(Show.when(model.getDataValidade() != null && model.getDataValidade() > 0,
                        () -> Components.TextWithDetails("Data de validade: ", DatePack.millisToBrazilianDate(model.getDataValidade()))))
                .c_child(Components.TextWithDetails("Data da compra: ", DatePack.millisToBrazilianDate(model.getDataCompra())))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.millisToBrazilianDateTime(model.getDataCriacaoMillis())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }

    @Override
    public SimpleTable<CompraModel> table() {
        return new SimpleTable<CompraModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", CompraModel::getId, (double) 90)
                .column("Produto", it -> it.getProdutoModel().getDescricao(), (double) 90)
                .column("Quantidade", CompraModel::getQuantidade)
                .column("N. Nota", CompraModel::getNumeroNota)
                .column("Fornecedor", it -> it.getFornecedor() == null ? "" : it.getFornecedor().getNome())
                .column("Total liq. de compra", it -> CurrencyPack.toBRLCurrency(it.getTotalLiquido()))
                .column("Data de criação", it -> DatePack.millisToBrazilianDateTime(it.getDataCriacaoMillis()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.selected::set)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), ctx, 600));
    }

    @Override
    public ViewModelScreenContract<CompraModel> viewModel() {
        return vm;
    }
}

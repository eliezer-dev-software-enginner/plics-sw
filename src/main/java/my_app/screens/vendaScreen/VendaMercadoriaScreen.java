package my_app.screens.vendaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.ImageProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.VendaModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

public class VendaMercadoriaScreen implements ScreenComponent, ScreenContract<VendaModel> {
    private final VendaMercadoriaScreenViewModel vm;
    private final ScreenContext screenContext;

    public VendaMercadoriaScreen(ScreenContext ctx) {
        this.vm = new VendaMercadoriaScreenViewModel(ctx);
        this.screenContext = ctx;
    }

    @Override
    public void onMount() { vm.fetchListData(); }

    @Override
    public void onDestroy() {
        ScreenContract.super.onDestroy();
    }

    @Override
    public Component render() { return mainView(vm.focusState); }

    @Override
    public SimpleTable<VendaModel> table() {
        return new SimpleTable<VendaModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", VendaModel::getId)
                .imageColumn("Imagem", it -> it.getProduto().getImagem())
                .column("Produto", it -> it.getProduto().getDescricao())
                .column("Preço de venda", it -> CurrencyPack.toBRLCurrency(it.getPrecoUnitario()))
                .column("Quantidade", VendaModel::getQuantidade)
                .column("Total líquido", it -> CurrencyPack.toBRLCurrency(it.getTotalLiquido()))
                .column("Data", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .column("Status", it -> Boolean.TRUE.equals(it.getDevolvida()) ? "Devolvida" : "-")
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.selected::set)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), this.screenContext, 550));
    }

    public Component itemDetails(VendaModel model) {
        var validade = model.getDataValidade() != null ? DatePack.millisToBrazilianDateTime(model.getDataValidade()) : "Sem validade";
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da venda de mercadoria", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Show.when(model.getProduto().getImagem()!=null,
                            ()->new Image(model.getProduto().getImagem(), new ImageProps().size(100)))
                )
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Código do produto: ", model.getProdutoCod()))
                .c_child(Components.TextWithDetails("Nome do produto: ", model.getProduto().getDescricao()))
                .c_child(Components.TextWithDetails("Id do cliente: ", model.getCliente().getId()))
                .c_child(Components.TextWithDetails("Nome do cliente: ", model.getCliente().getNome()))
                .c_child(Components.TextWithDetails("Número da nota: ", model.getNumeroNota()))
                .c_child(Components.TextWithDetails("Tipo de pagamento: ", model.getTipoPagamento()))
                .c_child(Components.TextWithDetails("Quantidade: ", model.getQuantidade()))
                .c_child(Components.TextWithDetails("Preço de venda: ", CurrencyPack.toBRLCurrency(model.getPrecoUnitario())))
                .c_child(Components.TextWithDetails("Desconto: ", CurrencyPack.toBRLCurrency(model.getDesconto())))
                .c_child(Components.TextWithDetails("Frete: ", CurrencyPack.toBRLCurrency(model.getFrete())))
                .c_child(Components.TextWithDetails("Total da venda: ", CurrencyPack.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Validade: ", validade))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true))
                .c_child(Show.when(Boolean.TRUE.equals(model.getDevolvida()),
                        () -> Components.TextWithDetails("Devolvida em: ",
                                DatePack.millisToBrazilianDateTime(model.getDataDevolucao()))
                ))
                .c_child(new Row(new RowProps().spacingOf(10)).children(
                        new Button("Imprimir nota de venda").onClick(() -> vm.imprimirNotaDeVenda(model)),
                        new Button("Imprimir (modo alternativo)").onClick(() -> vm.imprimirNotaDeVendaAlternativo(model))
                ))
                .c_child(Show.when(!Boolean.TRUE.equals(model.getDevolvida()),
                        () -> new Button("Devolver venda").onClick(() -> vm.handleClickMenuDevolucao(model))
                ));
    }

    @Override
    public ViewModelScreenContract<VendaModel> viewModel() { return vm; }
}

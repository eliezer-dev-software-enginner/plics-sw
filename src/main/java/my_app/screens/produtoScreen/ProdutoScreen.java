package my_app.screens.produtoScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Image;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.props.ImageProps;
import megalodonte.props.TextProps;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ProdutoModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

public class ProdutoScreen implements ScreenComponent, ScreenContract<ProdutoModel> {
    private final ProdutoScreenViewModel vm;
    private final ThemeInterface theme = ThemeManager.theme();

    public ProdutoScreen(ScreenContext ctx) {
        this.vm = new ProdutoScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ScreenContract.super.onDestroy();
    }

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public SimpleTable<ProdutoModel> table() {
        var simpleTable = new SimpleTable<ProdutoModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", ProdutoModel::getId, 70.0)
                .imageColumn("Imagem", ProdutoModel::getImagem)
                .column("Código", ProdutoModel::getCodigoBarras)
                .column("Descrição", ProdutoModel::getDescricao)
                .column("Cor", it -> it.getCor() != null ? it.getCor() : "")
                .column("Tamanho", it -> it.getTamanho() != null ? it.getTamanho() : "")
                .column("Estoque", ProdutoModel::getEstoque)
                .column("Est. Mínimo", ProdutoModel::getEstoqueMinimo)
                .column("Preço de compra", it -> CurrencyPack.toBRLCurrency(it.getPrecoCompra()))
                .column("Preço de venda", it -> CurrencyPack.toBRLCurrency(it.getPrecoVenda()))
                .column("Categoria", it -> it.getCategoria() != null ? it.getCategoria().getNome() : "")
                .column("Data de criação", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.selected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), vm.getCtx(), 600));

        return simpleTable;
    }

     public Component itemDetails(ProdutoModel model) {
        var validade = model.getValidade() != null ? DatePack.millisToBrazilianDateTime(model.getValidade()) : "Sem validade";

        return new Column(new ColumnProps().paddingAll(theme.spacing().md()))
                .children(
                        new Text("Detalhes do produto", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())),
                        new SpacerVertical(20),
                        Show.when(model.getImagem()!=null, ()->new Image(model.getImagem(), new ImageProps().size(100))),
                        Components.TextWithDetails("ID: ", model.getId()),
                        Components.TextWithDetails("Código: ", model.getCodigoBarras()),
                        Components.TextWithDetails("Cor: ", model.getCor() != null ? model.getCor() : "-"),
                        Components.TextWithDetails("Tamanho: ", model.getTamanho() != null ? model.getTamanho() : "-"),
                        Components.TextWithDetails("Modelo: ", model.getModelo() != null ? model.getModelo() : "-"),
                        Components.TextWithDetails("Descrição: ", model.getDescricao()),
                        Components.TextWithDetails("Fornecedor: ", model.getFornecedor().getNome()),
                        Components.TextWithDetails("Categoria: ", model.getCategoria().getNome()),
                        Components.TextWithDetails("Tipo de unidade: ", model.getUnidade()),
                        Components.TextWithDetails("Marca: ", model.getMarca()),
                        Components.TextWithDetails("Estoque: ", model.getEstoque()),
                        Components.TextWithDetails("Estoque Mínimo: ", model.getEstoqueMinimo()),
                        Components.TextWithDetails("Preço de compra (R$): ", CurrencyPack.toBRLCurrency(model.getPrecoCompra())),
                        Components.TextWithDetails("Frete (R$): ", CurrencyPack.toBRLCurrency(model.getFrete())),
                        Components.TextWithDetails("Preço de venda (R$): ", CurrencyPack.toBRLCurrency(model.getPrecoVenda())),
                        Components.TextWithDetails("Ganho líquido estimado (R$): ", CurrencyPack.toBRLCurrency(model.getTotalLiquido())),
                        Components.TextWithDetails("Garantia: ", model.getGarantia()),
                        Components.TextWithDetails("Aceita devolução/troca: ", Boolean.TRUE.equals(model.getAceitaDevolucao()) ? "Sim" : "Não"),
                        Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())),
                        Components.TextWithDetails("Validade: ", validade),
                        Components.TextWithDetails("Observação: ", model.getObservacoes(), true)
                );
    }

    @Override
    public ViewModelScreenContract<ProdutoModel> viewModel() {
        return vm;
    }
}

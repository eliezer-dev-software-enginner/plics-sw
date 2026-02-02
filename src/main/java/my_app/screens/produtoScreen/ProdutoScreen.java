package my_app.screens.produtoScreen;

import javafx.stage.FileChooser;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.components.Component;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.styles.TextStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ProdutoModel;
import my_app.domain.ContratoTelaCrud;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ProdutoScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final ProdutoScreenViewModel vm;
    private final Theme theme = ThemeManager.theme();

    public ProdutoScreen(Router router) {
        this.router = router;
        this.vm = new ProdutoScreenViewModel();
    }

    @Override
    public void onMount() {
        vm.loadInicial();
    }

    public Component render() {
        return new Column(new ColumnProps().paddingAll(15), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(commonCustomMenus())
                .c_child(new SpacerVertical(20))
                .c_child(createHeaderSection())
                .c_child(new SpacerVertical(30))
                .c_child(new Scroll(createMainContent()));
    }

    private Component createHeaderSection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(5))
                        .c_child(Components.FormTitle("Cadastro de Produtos"))
                        .c_child(new SpacerVertical(10))
                        .c_child(new Text("Gerencie o catálogo de produtos do seu estabelecimento",
                                new TextProps().variant(TextVariant.BODY), new TextStyler().color("#6b7280"))),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    private Component createMainContent() {
        return new Column(new ColumnProps().spacingOf(30))
                .c_child(createFormSection())
                .c_child(createTableSection());
    }

    private Component createFormSection() {
        Runnable handleChangeImage = () -> {
            var stage = this.router.getCurrentActiveStage();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Escolha a imagem");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("imagens",
                    "*.png", "*.jpg", "*.jpeg"));
            var file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                IO.print("caminho: " + file.toPath().toUri());
                vm.imagem.set(file.toPath().toUri().toString());
            }
        };

        return new Card(
                new Column(new ColumnProps().paddingAll(5))
                        .c_child(new Text("Dados do Produto",
                                new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row()
                                .r_child(ProdutoComponents.ContainerLeft(vm))
                                .r_child(Components.CardImageSelector(vm.imagem, handleChangeImage)))
                        .c_child(new SpacerVertical(25))
                        .c_child(Components.actionButtons(vm.btnText,this::handleAddOrUpdate, this::clearForm)),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    private Component createTableSection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(25))
                        .c_child(new Row(new RowProps().spacingOf(10))
                                .r_child(new Text("Produtos Cadastrados",
                                        new TextProps().fontSize(20).bold()))
                        )
                        .c_child(new SpacerVertical(15))
                        .c_child(table()),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<ProdutoModel>();
        simpleTable.fromData(vm.produtos)
                .header()
                .columns()
                .column("ID", it-> it.id)
                .column("Código", it-> it.codigoBarras)
                .column("Estoque", it-> it.estoque)
                .column("Descrição", it-> it.descricao)
                .column("Preço de compra", it-> Utils.toBRLCurrency(it.precoCompra))
                .column("Preço de venda", it-> Utils.toBRLCurrency(it.precoVenda))
                .column("Categoria", it ->   it.categoria != null ? it.categoria.nome : "")
                .column("Data de criação", it-> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(vm.produtoSelected::set);

        return simpleTable;
    }

    @Override
    public Component form() {
        return null;
    }


    @Override
    public void handleClickNew() {
        vm.modoEdicao.set(false);
        vm.limparFormulario();
    }

    @Override
    public void handleClickMenuEdit() {
        handleClickMenuClone();
        vm.modoEdicao.set(true);
    }

    @Override
    public void handleClickMenuDelete() {
        vm.modoEdicao.set(false);

        if (vm.produtoSelected.isNull()) return;
        var produtoSelected = vm.produtoSelected.get();

        var bodyMessage = "Tem certeza que deseja excluir o produto: %s com código: %s?".formatted(produtoSelected.descricao, produtoSelected.codigoBarras);
        Components.ShowAlertAdvice(bodyMessage, () -> {
            Async.Run(() -> {
                try {
                    vm.excluir();
                    //vm.refreshProdutos();
                    UI.runOnUi(() -> {
                        vm.limparFormulario();
                        Components.ShowPopup(router, "Produto excluído com sucesso");
                    });

                } catch (Exception e) {
                    Components.ShowAlertError("Erro ao excluir produto: " + e.getMessage());
                }
            });
        });
    }

    @Override
    public void handleClickMenuClone() {
        vm.modoEdicao.set(false);

        if(vm.produtoSelected.get() == null) return;
        final var model = vm.produtoSelected.get();

        vm.codigoBarras.set(model.codigoBarras);
        vm.descricao.set(model.descricao);
        vm.precoCompra.set(Utils.deRealParaCentavos( model.precoCompra));
        vm.precoVenda.set(Utils.deRealParaCentavos( model.precoVenda));
        //vm.margem.set(model.);
        //vm.lucro.set("0");
        vm.comissao.set(model.comissao);
        vm.garantia.set(model.garantia);
        vm.marca.set(model.marca);
        vm.unidadeSelected.set(model.unidade);
        vm.estoque.set(Utils.quantidadeTratada(model.estoque));
        vm.validade.set(model.validade);
        vm.observacoes.set(model.observacoes);
        vm.imagem.set(model.imagem);
    }

    @Override
    public void handleAddOrUpdate() {
        vm.salvarOuAtualizar(router);
    }

    @Override
    public void clearForm() {
        this.vm.limparFormulario();
    }

}

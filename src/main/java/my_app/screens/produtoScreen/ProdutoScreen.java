package my_app.screens.produtoScreen;

import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import megalodonte.*;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.Component;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.styles.RowStyler;
import megalodonte.styles.TextStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ProdutoModel;
import my_app.screens.components.Components;


import static my_app.screens.produtoScreen.ProdutoComponents.*;

public class ProdutoScreen {
    private final Router router;
    private final ProdutoScreenViewModel vm;
    private final Theme theme = ThemeManager.theme();

    public ProdutoScreen(Router router) {
        this.router = router;
        this.vm = new ProdutoScreenViewModel();
    }

    public Component render() {
        var scroll = new ScrollPane();
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setStyle("-fx-background-color: transparent;-fx-border-color: transparent;");
        scroll.setContent(createMainContent().getJavaFxNode());

        return new Column(new ColumnProps().paddingAll(15), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(this::handleClickNew, this::handleClickEdit, this::handleClickDelete))
                .c_child(new SpacerVertical(20))
                .c_child(createHeaderSection())
                .c_child(new SpacerVertical(30))
                .c_child(Component.CreateFromJavaFxNode(scroll));
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
        Runnable handleChangeImage = ()->{
           var stage = this.router.getCurrentActiveStage();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Escolha a imagem");
            fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("imagens",
                    "*.png","*.jpg", "*.jpeg"));
            var file = fileChooser.showOpenDialog(stage);

            if(file != null){
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
                        .c_child(createActionButtons()),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }


    void handleClickNew() {
        limparFormulario();
        IO.println("Formulário limpo para novo produto");
    }

    void handleClickEdit() {
        var produtoSelected = vm.produtoSelected.get();
        if(produtoSelected==null)return;

        vm.modoEdicao.set(true);
        if (vm.modoEdicao.get()) {
            fillInputs(vm.produtoSelected.get());
        }
    }

    void handleClickDelete() {
        var produtoSelected = vm.produtoSelected.get();
        if(produtoSelected==null)return;

        var bodyMessage = "Tem certeza que deseja excluir o produto: %s com código: %s?".formatted(produtoSelected.descricao,produtoSelected.codigoBarras);
        Components.ShowAlertAdvice(bodyMessage, ()->{
            Async.Run(()->{
                try {
                    vm.excluir();
                    vm.refreshProdutos();
                    UI.runOnUi(()->{
                        limparFormulario();
                        Components.ShowPopup(router, "Produto excluído com sucesso");
                    });


                } catch (Exception e) {
                    IO.println("Erro ao excluir produto: " + e.getMessage());
                }
            });
        });


    }

    private Component createTableSection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(25))
                        .c_child(new Row(new RowProps().spacingOf(10))
                                .r_child(new Text("Produtos Cadastrados",
                                        new TextProps().fontSize(20).bold()))
                                .r_child(new Button("Atualizar",
                                        new ButtonProps()
                                                .height(35)
                                                .bgColor("#2563eb")
                                                .textColor("white")
                                                .onClick(() -> vm.refreshProdutos()))))
                        .c_child(new SpacerVertical(15))
                        .c_child(ProdutosTable(vm.produtos, vm)),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    private Component createActionButtons() {
        return new Row(new RowProps().spacingOf(10))
                .r_child(new Button(vm.btnText,
                        new ButtonProps()
                                .fillWidth()
                                .height(35)
                                .bgColor("#10b981")
                                .textColor("white")
                                .fontSize(16)
                                .onClick(this::handleSalvarOrUpdate)))
                .r_child(new Button("Limpar",
                        new ButtonProps()
                                .fillWidth()
                                .height(35)
                                .bgColor("#6b7280")
                                .textColor("white")
                                .fontSize(16)
                                .onClick(this::limparFormulario))
                );
    }

    private void handleSalvarOrUpdate() {
        Async.Run(() -> {
            try {
                vm.salvar();
                vm.refreshProdutos();

                UI.runOnUi(() -> {
                    IO.println("Produto salvo com sucesso");
                    limparFormulario();
                });

            } catch (Exception e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });

    }

    private void fillInputs(ProdutoModel model) {
        vm.codigoBarras.set(model.codigoBarras);
        vm.descricao.set(model.descricao);
        vm.precoCompra.set(model.precoCompra.toPlainString());
        vm.precoVenda.set(model.precoVenda.toPlainString());
        //vm.margem.set(model.);
        //vm.lucro.set("0");
        vm.comissao.set(model.comissao);
        vm.garantia.set(model.garantia);
        vm.marca.set(model.marca);
        vm.unidadeSelected.set(model.unidade);
        vm.estoque.set(model.estoque.toPlainString());
        vm.validade.set(model.validade);
        vm.observacoes.set(model.observacoes);
        vm.imagem.set(model.imagem);
    }

    private void limparFormulario() {
        vm.codigoBarras.set("");
        vm.descricao.set("");
        vm.precoCompra.set("0");
        vm.precoVenda.set("0");
        vm.margem.set("0");
        vm.lucro.set("0");
        vm.comissao.set("");
        vm.garantia.set("");
        vm.marca.set("");
        vm.unidadeSelected.set("UN");
        vm.estoque.set("0");
        vm.validade.set("");
        vm.observacoes.set("");
        vm.imagem.set("/assets/produto-generico.png");
    }

    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }
}

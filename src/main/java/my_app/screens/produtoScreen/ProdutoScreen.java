package my_app.screens.produtoScreen;

import javafx.stage.FileChooser;
import megalodonte.ComputedState;
import megalodonte.Show;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.components.Component;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ProdutoModel;
import my_app.domain.ContratoTelaCrud;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.util.List;

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
        return new Column(new ColumnProps().paddingAll(15).bgColor(theme.colors().background()))
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
                                (TextProps) new TextProps().variant(TextVariant.BODY).color("#6b7280"))),
                new CardProps()
                        .padding(0)
                        .borderRadius(12)
        );
    }

    private Component createMainContent() {
        return new Column(new ColumnProps().spacingOf(30))
                .c_child(createFormSection())
                .c_child(table());
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
                                (TextProps) new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row()
                                .r_child(ContainerLeft(vm))
                                .r_child(Components.CardImageSelector(vm.imagem, handleChangeImage)))
                        .c_child(new SpacerVertical(25))
                        .c_child(Components.actionButtons(vm.btnText,this::handleAddOrUpdate, this::clearForm)),
                new CardProps()
                        .padding(0)
                        .borderRadius(12)
        );
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<ProdutoModel>();
        simpleTable.fromData(vm.produtos)
                .header()
                .columns()
                .column("ID", it-> it.id, 70.0)
                .column("Código", it-> it.codigoBarras)
                .column("Estoque", it-> it.estoque)
                .column("Descrição", it-> it.descricao)
                .column("Preço de compra", it-> Utils.toBRLCurrency(it.precoCompra))
                .column("Preço de venda", it-> Utils.toBRLCurrency(it.precoVenda))
                .column("Categoria", it ->   it.categoria != null ? it.categoria.nome : "")
                .column("Data de criação", it-> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(vm.produtoSelected::set)
                .onItemDoubleClick(it-> {
                    IO.println("ok");
                    Components.ShowModal( ItemDetails(it), router, 550);
                });

        return simpleTable;
    }

    public Component ContainerLeft(ProdutoScreenViewModel vm) {
        var rowProps = new RowProps().spacingOf(10);

        Runnable handleGerarCodigoBarras = ()->{
            final var codigo = Utils.gerarCodigoBarrasEAN13();
            vm.codigoBarras.set(codigo);
        };


       var showValidadePicker = ComputedState.of(()->vm.perecivelSelected.get().equals("Sim"), vm.perecivelSelected);

        return new Column(new ColumnProps().spacingOf(20))
                .c_child(
                        new Row(rowProps)
                                .r_child(new Row(new RowProps().bottomVertically())
                                        .r_child(Components.InputColumn("SKU(Código de barras)", vm.codigoBarras, ""))
                                        .r_child(new Button("Gerar", new ButtonProps().height(37).textColor("#FFF"))
                                                .onClick(handleGerarCodigoBarras)
                                        )
                                )
                                .r_child(Components.InputColumn("Descrição curta", vm.descricao, ""))
                                .r_child(Components.SelectColumn("Unidade", vm.unidades, vm.unidadeSelected, it -> it))
                                .r_child(Components.InputColumn("Marca", vm.marca, ""))
                ).c_child(new Row(rowProps)
                                .r_child(Components.InputColumnCurrency("Preço de compra", vm.precoCompra))
                                //.r_child(Components.InputColumn("Margem %", vm.margem, ""))
//                        .r_child(Components.InputColumn("Lucro", vm.lucro,Entypo.CREDIT))
                                .r_child(Components.InputColumnCurrency("Preço de venda", vm.precoVenda))
                                .r_child(Components.SelectColumn("Categoria", vm.categorias, vm.categoriaSelected, it -> it.nome))
                                .r_child(Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, it -> it.nome))
                                .r_child(Components.SelectColumn("É perecível?", List.of("Sim", "Não"), vm.perecivelSelected, it-> it))
                                .r_child(Show.when(showValidadePicker, ()-> Components.DatePickerColumn(vm.validade, "Validade"))
                                )

                ).c_child(new Row(rowProps)
                        .r_child(Components.InputColumn("Garantia", vm.garantia, ""))
                        //.r_child(Components.DatePickerColumn(vm.validade, "Validade"))
                        .r_child(Components.InputColumn("Comissão", vm.comissao, ""))
                )
                .c_child(new Row(rowProps)
                        .r_child(Components.TextAreaColumn("Observações", vm.observacoes, ""))
                        .r_child(Components.InputColumnNumeric("Estoque", vm.estoque, ""))//fornecedor padrão
                );
    }

    @Override
    public Component form() {
        return null;
    }


    Component ItemDetails(ProdutoModel model){
        var validade = model.validade!= null?  DateUtils.millisToBrazilianDateTime(model.dataCriacao): "Sem validade";
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do produto", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.id))
                .c_child(Components.TextWithDetails("Código: ", model.codigoBarras))
                .c_child(Components.TextWithDetails("Descrição: ", model.descricao))
                .c_child(Components.TextWithDetails("Fornecedor: ", model.fornecedor.nome))
                .c_child(Components.TextWithDetails("Categoria: ", model.categoria.nome))
                .c_child(Components.TextWithDetails("Tipo de unidade: ", model.unidade))
                .c_child(Components.TextWithDetails("Marca: ", model.marca))
                .c_child(Components.TextWithDetails("Estoque: ", model.estoque))
                .c_child(Components.TextWithDetails("Preço de compra (R$): ", Utils.toBRLCurrency(model.precoCompra)))
                .c_child(Components.TextWithDetails("Preço de venda (R$): ", Utils.toBRLCurrency(model.precoVenda)))
                .c_child(Components.TextWithDetails("Ganho líquido estimado (R$): ", Utils.toBRLCurrency(model.totalLiquido)))
                .c_child(Components.TextWithDetails("Garantia: ", model.garantia))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.millisToBrazilianDateTime(model.dataCriacao)))
                .c_child(Components.TextWithDetails("Validade: ", validade))
                .c_child(Components.TextWithDetails("Observação: ", model.observacoes,true));
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
        vm.validade.set(DateUtils.millisParaLocalDate(model.validade));
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

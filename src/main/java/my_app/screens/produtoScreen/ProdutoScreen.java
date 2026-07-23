package my_app.screens.produtoScreen;

import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.Show;
import my_app.db.models.CategoriaModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.util.List;

public class ProdutoScreen implements ScreenComponent, ContratoTelaCrudV3 {
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
        ContratoTelaCrudV3.super.onDestroy();
    }

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public Component form() {
        Runnable handleChangeImage = () -> {
            var stage = vm.getCtx().selfStage();

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

//        var leftColumn = new Column().children(ContainerLeft(vm));
//        javafx.scene.layout.HBox.setHgrow(leftColumn.getNode(), javafx.scene.layout.Priority.ALWAYS);

        return new Card(
                //new Container(new ContainerProps().paddingAll(5).bgColor("green"))
                new Container(new ContainerProps().paddingAll(5))
                        .children(
                                new Text("Dados do Produto",
                                        new TextProps().variant(TextVariant.BODY).bold()),
                                new SpacerVertical(20),
                                new Row(new RowProps().spacingOf(10))
                                        .children(
                                               ContainerLeft(vm),
                                                //leftColumn,
                                                Components.CardImageSelector(vm.imagem, handleChangeImage)
                                       // ),
                                        )
//                                new SpacerVertical(25),
//                                Components.actionButtons(vm.btnText, this::handleAddOrUpdate)
                        ),
                new CardProps()
                        .padding(10)
                        .borderRadius(12)
//                        .bgColor("red")
                        .fillWidth()
        );
    }

    @Override
    public SimpleTable table() {
        var simpleTable = new SimpleTable<ProdutoModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", ProdutoModel::getId, 70.0)
                .column("Código", ProdutoModel::getCodigoBarras)
                .column("Cor", it -> it.getCor() != null ? it.getCor() : "")
                .column("Tamanho", it -> it.getTamanho() != null ? it.getTamanho() : "")
                .column("Estoque", ProdutoModel::getEstoque)
                .column("Est. Mínimo", ProdutoModel::getEstoqueMinimo)
                .column("Descrição", ProdutoModel::getDescricao)
                .column("Preço de compra", it -> Utils.toBRLCurrency(it.getPrecoCompra()))
                .column("Preço de venda", it -> Utils.toBRLCurrency(it.getPrecoVenda()))
                .column("Categoria", it -> it.getCategoria() != null ? it.getCategoria().getNome() : "")
                .column("Data de criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.produtoSelected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> Components.ShowModal(ItemDetails(it), vm.getCtx(), 600));

        return simpleTable;
    }

    private Component coresCheckboxes() {
        var outerColumn = new Column(new ColumnProps().spacingOf(3));

        outerColumn.c_child(new Text("Cores", new TextProps().fontSize(theme.typography().small())));
        vm.cores.onChange(listaCores->{
            var cbSize = 4;
            var rows = (int) Math.ceil((double) listaCores.size() / cbSize);

            for (int r = 0; r < rows; r++) {
                var row = new Row(new RowProps().spacingOf(8));
                int start = r * cbSize;
                int end = Math.min(start + cbSize, listaCores.size());
                for (int i = start; i < end; i++) {
                    var cor = listaCores.get(i);
                    var cb = new CheckBox(cor.getNome());

                    cb.setSelected(vm.coresSelecionadas.get().contains(cor.getNome()));
                    //checkbox -> coresSelecionadas
                    cb.selectedProperty().addListener((obs, old, selected) -> {
                        var current = new java.util.ArrayList<>(vm.coresSelecionadas.get());
                        if (selected) { if (!current.contains(cor.getNome())) current.add(cor.getNome()); }
                        else { current.remove(cor.getNome()); }
                        vm.coresSelecionadas.set(current);
                    });

                    //checkbox <- coresSelecionadas
                    vm.coresSelecionadas.subscribe(coresSelecionadas-> cb.setSelected(coresSelecionadas.contains(cor.getNome())));

                    row.r_child(Component.CreateFromJavaFxNode(cb));
                }
                outerColumn.c_child(row);
            }
        });

        return outerColumn;
    }

    public Component ContainerLeft(ProdutoScreenViewModel vm) {
        //var rowProps = new RowProps().spacingOf(10);

        Runnable handleGerarCodigoBarras = () -> {
            final var codigo = Utils.gerarCodigoBarrasEAN13();
            vm.codigoBarras.set(codigo);
        };

        var showValidadePicker = ComputedState.of(() -> vm.perecivelSelected.get().equals("Sim"), vm.perecivelSelected);

        //return new FlowRow(new FlowRowProps().fillWidth().spacingOf(theme.spacing().md()).bgColor("yellow"))
        return new FlowRow(new FlowRowProps().fillWidth().spacingOf(theme.spacing().md()))
                .children(
                        Components.InputWithButtonRow("SKU(Código de barras)", "Gerar", vm.codigoBarras, handleGerarCodigoBarras),
                        Components.InputColumn("Descrição curta", vm.descricao, ""),
                        Components.SelectColumn("Unidade", Data.unidadesDeMedidaList, vm.unidadeSelected, it -> it),
                        Components.InputColumn("Marca", vm.marca, ""),
                        coresCheckboxes(),
                        Components.InputColumn("Tamanho", vm.tamanhoSelected, ""),
                        Components.InputColumn("Modelo", vm.modelo, ""),
                        Components.InputColumnCurrency("Preço de compra", vm.precoCompra),
                        Components.InputColumnCurrency("Preço de venda", vm.precoVenda),
                        Components.SelectColumn("Categoria", vm.categorias, vm.categoriaSelected, CategoriaModel::getNome),
                        Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, FornecedorModel::getNome),
                        Components.SelectColumn("É perecível?", List.of("Sim", "Não"), vm.perecivelSelected, it -> it),
                        Show.when(showValidadePicker, () -> Components.DatePickerColumn(vm.validade, "Validade")),
                        Components.InputColumn("Garantia", vm.garantia, ""),
                        Components.InputColumn("Comissão", vm.comissao, ""),
                        Components.TextAreaColumn("Observações", vm.observacoes, ""),
                        Components.InputColumnNumeric("Estoque", vm.estoque, ""),
                        Components.InputColumnNumeric("Estoque Mínimo", vm.estoqueMinimo, "")
                );
    }

    Component ItemDetails(ProdutoModel model) {
        var validade = model.getValidade() != null ? DateUtils.millisToBrazilianDateTime(model.getValidade()) : "Sem validade";

        return new Column(new ColumnProps().paddingAll(20))
                .children(
                        new Text("Detalhes do produto", new TextProps().variant(TextVariant.SUBTITLE)),
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
                        Components.TextWithDetails("Preço de compra (R$): ", Utils.toBRLCurrency(model.getPrecoCompra())),
                        Components.TextWithDetails("Preço de venda (R$): ", Utils.toBRLCurrency(model.getPrecoVenda())),
                        Components.TextWithDetails("Ganho líquido estimado (R$): ", Utils.toBRLCurrency(model.getTotalLiquido())),
                        Components.TextWithDetails("Garantia: ", model.getGarantia()),
                        Components.TextWithDetails("Data de criação: ", DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())),
                        Components.TextWithDetails("Validade: ", validade),
                        Components.TextWithDetails("Observação: ", model.getObservacoes(), true)
                );
    }

    @Override
    public ViewModelScreenContract<ProdutoModel> viewModel() {
        return vm;
    }
}

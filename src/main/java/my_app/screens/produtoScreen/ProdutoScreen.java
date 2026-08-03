package my_app.screens.produtoScreen;

import javafx.stage.FileChooser;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.CategoriaModel;
import my_app.db.models.CorModel;
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
                                        new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()),
                                new SpacerVertical(ThemeManager.theme().spacing().sm()),
                                new Row(new RowProps().spacingOf(ThemeManager.theme().spacing().sm()))
                                        .children(
                                               ContainerLeft(vm),
                                                Components.CardImageSelector(vm.imagem, handleChangeImage)
                                        ),
                                new SpacerVertical(ThemeManager.theme().spacing().md()),
                                Components.actionButtons(vm.btnText, this::handleAddOrUpdate)
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
                .imageColumn("Imagem", ProdutoModel::getImagem)
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
        var checkboxesPorCor = ForEachState.of(vm.cores, this::corCheckbox);

        return new Column(new ColumnProps().spacingOf(3))
                .c_child(new Text("Cores", new TextProps().fontSize(theme.typography().small())))
                .c_child(new FlowRow(new FlowRowProps().spacingOf(8).width(620)).items(checkboxesPorCor));
    }

    private Checkbox corCheckbox(CorModel cor) {
        var selecionado = new State<>(vm.coresSelecionadas.get().contains(cor.getNome()));

        // checkbox -> coresSelecionadas
        selecionado.subscribe(isSelected -> {
            var current = new java.util.ArrayList<>(vm.coresSelecionadas.get());
            boolean jaSelecionado = current.contains(cor.getNome());
            if (isSelected && !jaSelecionado) {
                current.add(cor.getNome());
                vm.coresSelecionadas.set(current);
            } else if (!isSelected && jaSelecionado) {
                current.remove(cor.getNome());
                vm.coresSelecionadas.set(current);
            }
        });

        // coresSelecionadas -> checkbox
        vm.coresSelecionadas.subscribe(selecionadas -> selecionado.set(selecionadas.contains(cor.getNome())));

        return new Checkbox(cor.getNome(), selecionado, new CheckboxProps().fontSize(theme.typography().small()));
    }

    public Component ContainerLeft(ProdutoScreenViewModel vm) {
        Runnable handleGerarCodigoBarras = () -> {
            final var codigo = Utils.gerarCodigoBarrasEAN13();
            vm.codigoBarras.set(codigo);
        };

        var showValidadePicker = ComputedState.of(() -> vm.perecivelSelected.get().equals("Sim"), vm.perecivelSelected);

        //return new FlowRow(new FlowRowProps().fillWidth().spacingOf(theme.spacing().md()).bgColor("yellow"))
        return new FlowRow(new FlowRowProps().fillWidth().spacingOf(theme.spacing().md()))
                .children(
                        Components.InputWithButtonRow("SKU(Código de barras)", "Ex: 7891234567895", "Gerar", vm.codigoBarras, handleGerarCodigoBarras),
                        Components.InputColumn("Nome", vm.descricao, "Ex: Camiseta Polo M",150),
                        Components.SelectColumn("Unidade", Data.unidadesDeMedidaList, vm.unidadeSelected, it -> it),
                        Components.InputColumn("Marca", vm.marca, "Ex: Nike",150),
                        coresCheckboxes(),
                        Components.InputColumn("Tamanho", vm.tamanhoSelected, "Ex: M",90),
                        Components.InputColumn("Modelo", vm.modelo, "Ex: Slim Fit",150),
                        Components.InputColumnCurrency("Preço de compra", vm.precoCompra),
                        Components.InputColumnCurrency("Preço de venda", vm.precoVenda),
                        Components.SelectColumn("Categoria", vm.categorias, vm.categoriaSelected, CategoriaModel::getNome),
                        Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, FornecedorModel::getNome),
                        Components.SelectColumn("É perecível?", List.of("Sim", "Não"), vm.perecivelSelected, it -> it),
                        Show.when(showValidadePicker, () -> Components.DatePickerColumn(vm.validade, "Validade")),
                        Components.InputColumn("Garantia", vm.garantia, "Ex: 12 meses",150),
                        Components.TextAreaColumn("Observações", vm.observacoes, "Ex: Produto frágil, manusear com cuidado"),
                        Components.InputColumnNumeric("Estoque", vm.estoque, "Ex: 100"),
                        Components.InputColumnNumeric("Estoque Mínimo", vm.estoqueMinimo, "Ex: 10")
                );
    }

    Component ItemDetails(ProdutoModel model) {
        var validade = model.getValidade() != null ? DateUtils.millisToBrazilianDateTime(model.getValidade()) : "Sem validade";

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

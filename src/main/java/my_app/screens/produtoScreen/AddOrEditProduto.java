package my_app.screens.produtoScreen;

import disgust.io.br.Pack;
import javafx.stage.FileChooser;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.base.async.RunnableThrowing;
import megalodonte.base.components.Component;
import megalodonte.base.state.State;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Card;
import megalodonte.components.Checkbox;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.models.CorModel;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.models.ProdutoModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ProdutoService;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class AddOrEditProduto extends ScreenAddOrEdit<ProdutoModel,ProdutoScreenViewModel> {

    public AddOrEditProduto(ScreenContext screenContext) {
      super(screenContext);
    }

    @Override
    public Component render() {
        return new Card(
                Components.ScrollPaneDefault( new Container(new ContainerProps().paddingAll(5))
                                .children(
                                        new Text("Dados do Produto",
                                                new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()),
                                        new SpacerVertical(ThemeManager.theme().spacing().sm()),
                                        new Row(new RowProps().spacingOf(ThemeManager.theme().spacing().sm()))
                                                .children(
                                                        ContainerLeft(viewModel),
                                                        Components.CardImageSelector(viewModel.imagem, handleChangeImage)
                                                ),
                                        new SpacerVertical(ThemeManager.theme().spacing().md()),
                                        Components.actionButtons(viewModel.btnText, this::handleAddOrUpdate)
                                )),
                new CardProps()
                        .paddingAll(10)
                        .borderRadius(12)
//                        .bgColor("red")
                        .fillWidth()
                //new Container(new ContainerProps().paddingAll(5).bgColor("green"))

        );
    }

    public Component ContainerLeft(ProdutoScreenViewModel viewModel) {
        RunnableThrowing handleGerarCodigoBarras = () -> {
            final var codigo = Utils.gerarCodigoBarrasEAN13();
            viewModel.codigoBarras.set(codigo);
        };

        var showValidadePicker = ComputedState.of(() -> viewModel.perecivelSelected.get().equals("Sim"), viewModel.perecivelSelected);

        //return new FlowRow(new FlowRowProps().fillWidth().spacingOf(theme.spacing().md()).bgColor("yellow"))
        return new FlowRow(new FlowRowProps().fillWidth().spacingOf(theme.spacing().md()))
                .children(
                        disgust.io.Pack.InputWithButtonRow("SKU(Código de barras) *", "Ex: 7891234567895", "Gerar", viewModel.codigoBarras, handleGerarCodigoBarras),
                        disgust.io.Pack.InputColumn("Nome *", viewModel.descricao, "Ex: Camiseta Polo M",150),
                        Components.SelectColumn("Unidade", Data.unidadesDeMedidaList, viewModel.unidadeSelected, it -> it),
                        disgust.io.Pack.InputColumn("Marca", viewModel.marca, "Ex: Nike",150),
                        coresCheckboxes(),
                        disgust.io.Pack.InputColumn("Tamanho", viewModel.tamanhoSelected, "Ex: M",90),
                        disgust.io.Pack.InputColumn("Modelo", viewModel.modelo, "Ex: Slim Fit",150),
                        Pack.InputColumnCurrency("Preço de compra", viewModel.precoCompra),
                        Pack.InputColumnCurrency("Frete", viewModel.frete),
                        Pack.InputColumnCurrency("Preço de venda", viewModel.precoVenda),
                        disgust.io.Pack.SelectColumn("Categoria", viewModel.categorias, viewModel.categoriaSelected, CategoriaModel::getNome),
                        disgust.io.Pack.SelectColumn("Fornecedor", viewModel.fornecedores, viewModel.fornecedorSelected, FornecedorModel::getNome),
                        Components.SelectColumn("É perecível?", List.of("Sim", "Não"), viewModel.perecivelSelected, it -> it),
                        Show.when(showValidadePicker, () -> Components.DatePickerColumn(viewModel.validade, "Validade")),
                        disgust.io.Pack.InputColumn("Garantia", viewModel.garantia, "Ex: 12 meses",150),
                        Components.SelectColumn("Aceita devolução/troca?", Data.simNaoList, viewModel.aceitaDevolucao, it -> it),
                        Components.TextAreaColumn("Observações", viewModel.observacoes, "Ex: Produto frágil, manusear com cuidado", 60, 160),
                        Components.InputColumnNumeric("Estoque", viewModel.estoque, "Ex: 100"),
                        Components.InputColumnNumeric("Estoque Mínimo", viewModel.estoqueMinimo, "Ex: 10")
                );
    }

    private Component coresCheckboxes() {
        var checkboxesPorCor = ForEachState.of(viewModel.cores, this::corCheckbox);

        return new Column(new ColumnProps().spacingOf(3))
                .c_child(new Text("Cores", new TextProps().fontSize(theme.typography().small())))
                .c_child(new FlowRow(new FlowRowProps().spacingOf(8).width(620)).items(checkboxesPorCor));
    }

    private Checkbox corCheckbox(CorModel cor) {
        var selecionado = new State<>(viewModel.coresSelecionadas.get().contains(cor.getNome()));

        // checkbox -> coresSelecionadas
        selecionado.subscribe(isSelected -> {
            var current = new java.util.ArrayList<>(viewModel.coresSelecionadas.get());
            boolean jaSelecionado = current.contains(cor.getNome());
            if (isSelected && !jaSelecionado) {
                current.add(cor.getNome());
                viewModel.coresSelecionadas.set(current);
            } else if (!isSelected && jaSelecionado) {
                current.remove(cor.getNome());
                viewModel.coresSelecionadas.set(current);
            }
        });

        // coresSelecionadas -> checkbox
        viewModel.coresSelecionadas.subscribe(selecionadas -> selecionado.set(selecionadas.contains(cor.getNome())));

        return new Checkbox(cor.getNome(), selecionado, new CheckboxProps().fontSize(theme.typography().small()));
    }

    RunnableThrowing handleChangeImage = () -> {
        var stage = viewModel.getCtx().selfStage();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha a imagem");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("imagens",
                "*.png", "*.jpg", "*.jpeg"));
        var file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            IO.print("caminho: " + file.toPath().toUri());
            viewModel.imagem.set(file.toPath().toUri().toString());
        }
    };

    @Override
    protected ProdutoScreenViewModel getViewModel(ScreenContext screenContext) {
        return new ProdutoScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<ProdutoModel> getService() throws SQLException {
        return new ProdutoService();
    }

    @Override
    protected String getTitle() {
        return "produto";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(AddOrEditProduto.class);
    }

}

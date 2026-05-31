package my_app.screens.produtoScreen;

import javafx.stage.FileChooser;
import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.Show;
import my_app.db.models.ProdutoModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.util.List;

public class ProdutoScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final ProdutoScreenViewModel vm;

    public ProdutoScreen(ScreenContext ctx) {
        this.vm = new ProdutoScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadInicial();
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

        return new Card(
                new Column(new ColumnProps().paddingAll(5))
                        .c_child(new Text("Dados do Produto",
                                new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row()
                                .r_child(ContainerLeft(vm))
                                .r_child(new SpacerHorizontal(90))
                                .r_child(Components.CardImageSelector(vm.imagem, handleChangeImage)))
                        .c_child(new SpacerVertical(25))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm)),
                new CardProps()
                        .padding(10)
                        .borderRadius(12)
        );
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<ProdutoModel>();
        simpleTable.fromData(vm.produtos)
                .header()
                .columns()
                .column("ID", it -> it.getId(), 70.0)
                .column("Código", it -> it.getCodigoBarras())
                .column("Estoque", it -> it.getEstoque())
                .column("Descrição", it -> it.getDescricao())
                .column("Preço de compra", it -> Utils.toBRLCurrency(it.getPrecoCompra()))
                .column("Preço de venda", it -> Utils.toBRLCurrency(it.getPrecoVenda()))
                .column("Categoria", it -> it.getCategoria() != null ? it.getCategoria().getNome() : "")
                .column("Data de criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.produtoSelected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> Components.ShowModal(ItemDetails(it), vm.getCtx(), 550));

        return simpleTable;
    }

    public Component ContainerLeft(ProdutoScreenViewModel vm) {
        var rowProps = new RowProps().spacingOf(10);

        Runnable handleGerarCodigoBarras = () -> {
            final var codigo = Utils.gerarCodigoBarrasEAN13();
            vm.codigoBarras.set(codigo);
        };

        var showValidadePicker = ComputedState.of(() -> vm.perecivelSelected.get().equals("Sim"), vm.perecivelSelected);

        return new Column(new ColumnProps().spacingOf(20))
                .c_child(
                        new Row(rowProps).children(
                                Components.InputWithButtonRow("SKU(Código de barras)", "Gerar", vm.codigoBarras, handleGerarCodigoBarras),
                                Components.InputColumn("Descrição curta", vm.descricao, ""),
                                Components.SelectColumn("Unidade", Data.unidadesDeMedidaList, vm.unidadeSelected, it -> it),
                                Components.InputColumn("Marca", vm.marca, "")
                        )
                ).c_child(new Row(rowProps)
                        .r_child(Components.InputColumnCurrency("Preço de compra", vm.precoCompra))
                        .r_child(Components.InputColumnCurrency("Preço de venda", vm.precoVenda))
                        .r_child(Components.SelectColumn("Categoria", vm.categorias, vm.categoriaSelected, it -> it.getNome()))
                        .r_child(Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, it -> it.getNome()))
                        .r_child(Components.SelectColumn("É perecível?", List.of("Sim", "Não"), vm.perecivelSelected, it -> it))
                        .r_child(Show.when(showValidadePicker, () -> Components.DatePickerColumn(vm.validade, "Validade"))
                        )
                ).c_child(new Row(rowProps)
                        .r_child(Components.InputColumn("Garantia", vm.garantia, ""))
                        .r_child(Components.InputColumn("Comissão", vm.comissao, ""))
                )
                .c_child(new Row(rowProps)
                        .r_child(Components.TextAreaColumn("Observações", vm.observacoes, ""))
                        .r_child(Components.InputColumnNumeric("Estoque", vm.estoque, ""))
                );
    }

    Component ItemDetails(ProdutoModel model) {
        var validade = model.getValidade() != null ? DateUtils.millisToBrazilianDateTime(model.getValidade()) : "Sem validade";
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do produto", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Código: ", model.getCodigoBarras()))
                .c_child(Components.TextWithDetails("Descrição: ", model.getDescricao()))
                .c_child(Components.TextWithDetails("Fornecedor: ", model.getFornecedor().getNome()))
                .c_child(Components.TextWithDetails("Categoria: ", model.getCategoria().getNome()))
                .c_child(Components.TextWithDetails("Tipo de unidade: ", model.getUnidade()))
                .c_child(Components.TextWithDetails("Marca: ", model.getMarca()))
                .c_child(Components.TextWithDetails("Estoque: ", model.getEstoque()))
                .c_child(Components.TextWithDetails("Preço de compra (R$): ", Utils.toBRLCurrency(model.getPrecoCompra())))
                .c_child(Components.TextWithDetails("Preço de venda (R$): ", Utils.toBRLCurrency(model.getPrecoVenda())))
                .c_child(Components.TextWithDetails("Ganho líquido estimado (R$): ", Utils.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Garantia: ", model.getGarantia()))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Validade: ", validade))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacoes(), true));
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }
}

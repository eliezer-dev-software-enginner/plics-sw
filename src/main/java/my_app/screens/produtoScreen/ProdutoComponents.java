package my_app.screens.produtoScreen;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.props.*;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ProdutoModel;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.Locale;

public class ProdutoComponents {
    private static Theme theme = ThemeManager.theme();

    public static Component ContainerLeft(ProdutoScreenViewModel vm) {
        var rowProps = new RowProps().spacingOf(10);
        return new Column(new ColumnProps().spacingOf(20))
                .c_child(
                        new Row(rowProps)
                                .r_child(new Row(new RowProps().bottomVertically())
                                        .r_child(Components.InputColumn("SKU(Código de barras)", vm.codigoBarras, ""))
                                        .r_child(new Button("Gerar", new ButtonProps().height(37)
                                                .textColor("#FFF")))
                                )
                                .r_child(Components.InputColumn("Descrição curta", vm.descricao, ""))
                                .r_child(Components.SelectColumn("Unidade", vm.unidades, vm.unidadeSelected, it -> it))
                                .r_child(Components.InputColumn("Marca", vm.marca, ""))
                ).c_child(new Row(rowProps)
                                .r_child(Components.InputColumnCurrency("Preço de compra", vm.precoCompra))
                                .r_child(Components.InputColumn("Margem %", vm.margem, ""))
//                        .r_child(Components.InputColumn("Lucro", vm.lucro,Entypo.CREDIT))
                                .r_child(Components.InputColumnCurrency("Preço de venda", vm.precoVenda))
                ).c_child(new Row(rowProps)
                        .r_child(Components.SelectColumn("Categoria", vm.categorias.get(), vm.categoriaSelected, it -> it))
                        .r_child(Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected, it -> it))
                        .r_child(Components.InputColumn("Garantia", vm.garantia, ""))
                        .r_child(Components.InputColumn("Validade", vm.validade, ""))
                        .r_child(Components.InputColumn("Comissão", vm.comissao, ""))
                )
                .c_child(new Row(rowProps)
                        .r_child(Components.TextAreaColumn("Observações", vm.observacoes, ""))
                        .r_child(Components.InputColumn("Estoque", vm.estoque, ""))//fornecedor padrão
                );
    }

    public static Component ProdutosTable(ObservableList<ProdutoModel> produtos,
                                          ProdutoScreenViewModel vm) {
        TableView<ProdutoModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Coluna Código de Barras
        TableColumn<ProdutoModel, String> codigoCol = new TableColumn<>("Código");
        codigoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().codigoBarras)
        );
        codigoCol.setMaxWidth(120);

        // Coluna Descrição
        TableColumn<ProdutoModel, String> descricaoCol = new TableColumn<>("Descrição");
        descricaoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().descricao)
        );
        descricaoCol.setPrefWidth(300);

        // Coluna Preço Compra
        TableColumn<ProdutoModel, String> precoCompraCol = new TableColumn<>("Preço Compra");
        precoCompraCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().precoCompra != null ?
                                NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(data.getValue().precoCompra) :
                                "R$ 0,00"
                )
        );
        precoCompraCol.setMaxWidth(120);

        // Coluna Preço Venda
        TableColumn<ProdutoModel, String> precoVendaCol = new TableColumn<>("Preço Venda");
        precoVendaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().precoVenda != null ?
                                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                                        .format(
                                                data.getValue().precoVenda) :
                                "R$ 0,00"
                )
        );
        precoVendaCol.setMaxWidth(120);

        // Coluna Estoque
        TableColumn<ProdutoModel, String> estoqueCol = new TableColumn<>("Estoque");
        estoqueCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().estoque != null ? String.valueOf(data.getValue().estoque) : "0"
                )
        );
        estoqueCol.setMaxWidth(80);

        // Coluna Categoria
        TableColumn<ProdutoModel, String> categoriaCol = new TableColumn<>("Categoria");
        categoriaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().categoria != null ? data.getValue().categoria.nome : ""
                )
        );
        categoriaCol.setPrefWidth(200);

        table.getColumns().addAll(codigoCol, descricaoCol, precoCompraCol, precoVendaCol, estoqueCol, categoriaCol);
        table.setItems(produtos);

        // Estilo bonito para a tabela
        table.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-background-color: white; " +
                        "-fx-control-inner-background: #f8f9fa; " +
                        "-fx-table-cell-border-color: #e9ecef; " +
                        "-fx-table-header-border-color: #dee2e6; " +
                        "-fx-selection-bar: #2563eb; " +
                        "-fx-selection-bar-non-focused: #93c5fd;"
        );

        Utils.onItemTableSelectedChange(table, vm.produtoSelected::set);

        return Component.CreateFromJavaFxNode(table);
    }

}

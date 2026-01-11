package my_app.screens.produtoScreen;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.*;
import my_app.db.models.ProdutoModel;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdutoComponents {

    public static Component ContainerLeft (ProdutoScreenViewModel vm){
        var rowProps = new RowProps().spacingOf(10);
        return new Column(new ColumnProps().spacingOf(20))
                .c_child(
                        new Row(rowProps)
                                .r_child(new Row(new RowProps().bottomVertically())
                                        .r_child(InputColumn("SKU(Código de barras)", vm.codigoBarras))
                                        .r_child(new Button("Gerar", new ButtonProps().height(40)))
                                )
                                .r_child(InputColumn("Descrição curta", vm.descricao))
                                .r_child(SelectColumn("Unidade", vm.unidades ,vm.unidadeSelected))
                                .r_child(InputColumn("Marca", vm.marca))
                ).c_child(new Row(rowProps)
                        .r_child(InputColumn("Preço de compra", vm.precoCompra, vm.precoCompraRaw, Entypo.CREDIT))
                        .r_child(InputColumn("Margem %", vm.margem))
                        .r_child(InputColumn("Lucro", vm.lucro,Entypo.CREDIT))
                        .r_child(InputColumn("Preço de venda", vm.precoVenda, vm.precoVendaRaw, Entypo.CREDIT))
                ).c_child(new Row(rowProps)
                        .r_child(SelectColumn("Categoria",vm.categorias, vm.categoriaSelected))
                        .r_child(SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected))//fornecedor padrão
                        .r_child(InputColumn("Garantia", vm.garantia))
                        .r_child(InputColumn("Validade", vm.validade))
                        .r_child(InputColumn("Comissão", vm.comissao))
                )
                .c_child(new Row(rowProps)
                        .r_child(TextAreaColumn("Observações", vm.observacoes))
                        .r_child(InputColumn("Estoque", vm.estoque))//fornecedor padrão
                );
    }
    public static Component ContainerRight(){

        State<String> imagemState = new State<>("/assets/produto-generico.png");

        return new Card(
                new Column(new ColumnProps().centerHorizontally().spacingOf(15))
                        .c_child(new Text("Foto do produto",new TextProps().fontSize(20).bold()))
                        .c_child(new Image(imagemState, new ImageProps().size(120)))
                        .c_child(new SpacerVertical().fill())
                        .c_child(new Button("Inserir imagem", new ButtonProps().fontSize(20).bgColor("#A6B1E1"))),
                new CardProps().height(300).padding(20)
        );
    }

    public static Component TextAreaColumn(String label, State<String> inputState){
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(new TextAreaInput(inputState,new InputProps().fontSize(20).height(140)));
    }

    public static Component SelectColumn(String label, List<String> list, State<String> stateSelected){
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(25)))
                .c_child(new Select<String>(new SelectProps().height(40))
                        .items(list)
                        .value(stateSelected));
    }

    public static Component SelectColumn(String label, State<List<String>> listState, State<String> stateSelected){
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(25)))
                .c_child(new Select<String>(new SelectProps().height(40))
                        .items(listState.get())
                        .value(stateSelected));
    }

    private static final NumberFormat BRL =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public static Component InputColumn(String label, State<String> inputState, State<String> rawState, Ikon icon){
        var fonticon = FontIcon.of(icon, 15, Color.web("green"));

        var inputProps = new InputProps().fontSize(22).height(40);

        var input = icon == Entypo.CREDIT? new Input(inputState, inputProps)
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");
                    if (numeric.isEmpty()) return "";

                    // Atualiza o state com o valor bruto (em centavos)
                    rawState.set(numeric);

                    BigDecimal raw = new BigDecimal(numeric).movePointLeft(2);

                    return BRL.format(raw);
                }) : new Input(inputState, inputProps);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(input.left(fonticon));
    }

    public static Component InputColumn(String label, State<String> inputState, Ikon icon){
        var fonticon = FontIcon.of(icon, 15, Color.web("green"));
        var inputProps = new InputProps().fontSize(22).height(40);
        var input = new Input(inputState, inputProps);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(input.left(fonticon));
    }

    public static Component InputColumn(String label, State<String> inputState){
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(new Input(inputState,new InputProps().fontSize(20).height(40)));
    }

    public static Component ProdutosTable(ObservableList<ProdutoModel> produtos) {
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
                    NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(data.getValue().precoVenda) : 
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

        return Component.CreateFromJavaFxNode(table);
    }

}

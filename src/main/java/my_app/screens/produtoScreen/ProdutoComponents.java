package my_app.screens.produtoScreen;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.CardProps;
import megalodonte.props.TextProps;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

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
                        .r_child(InputColumn("Preço de compra", vm.precoCompra, Entypo.CREDIT))
                        .r_child(InputColumn("Margem %", vm.margem))
                        .r_child(InputColumn("Lucro", vm.lucro,Entypo.CREDIT))
                        .r_child(InputColumn("Preço de venda", vm.precoVenda,Entypo.CREDIT))
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

    public static Component InputColumn(String label, State<String> inputState, Ikon icon){
        var fonticon = FontIcon.of(icon, 15, Color.web("green"));

        var inputProps = new InputProps().fontSize(22).height(40);

        var input = icon == Entypo.CREDIT? new Input(inputState, inputProps)
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");
                    if (numeric.isEmpty()) return "";

                    BigDecimal raw = new BigDecimal(numeric).movePointLeft(2);

                    return BRL.format(raw);
                }) : new Input(inputState, inputProps);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(input.left(fonticon));
    }

    public static Component InputColumn(String label, State<String> inputState){
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(22)))
                .c_child(new Input(inputState,new InputProps().fontSize(20).height(40)));
    }

}

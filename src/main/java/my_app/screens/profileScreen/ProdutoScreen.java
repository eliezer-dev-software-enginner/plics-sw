package my_app.screens.profileScreen;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.Component;
import megalodonte.components.Image;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.CardProps;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdutoScreen {
    Router router;
    ProdutoScreenViewModel vm;

    public ProdutoScreen(Router router) {
        this.vm = new ProdutoScreenViewModel();
    }

    public Component render (){
        return new Column(new ColumnProps().paddingAll(15), new ColumnStyler().bgColor("#fff"))
                .child(Menu())
                .child(new SpacerVertical(30))
                .child(
                        new Row(new RowProps().paddingAll(20)
                                .spacingOf(20), new RowStyler().borderWidth(1).borderColor("black").borderRadius(1))
                                .child(ContainerLeft())
                                .child(new SpacerHorizontal().fill())
                                .child(ContainerRight())
                )
                .child(new SpacerVertical(30))
                .child(new Text("Informaçoẽs", new TextProps().fontSize(30)))
                .child(new Text("Dica 1: Pressione CTRL + G para gerar o código de barras",new TextProps().fontSize(24).color("orange")))
                .child(new Text("Dica 2: O sistema não permite gravar produtos diferentes com o mesmo código de barras!",new TextProps().fontSize(24).color("orange")));
    }

    Component Menu(){
        return new Row(new RowProps().spacingOf(20))
                .child(MenuItem("Salvar (CTRL + S)", Entypo.SAVE, "green", ()->IO.println("save")))
                .child(MenuItem("Cancelar (CTRL + C)", Entypo.REPLY, "red", ()->IO.println("cancel")));
    }

    Component MenuItem(String title,Ikon ikon,  String color, Runnable onClick){

       var icon = Component.FromJavaFxNode(FontIcon.of(ikon, 50, Color.web(color)));

        return new Card(new Column(new ColumnProps().centerHorizontally())
                .child(new Clickable(icon).onClick(onClick::run))
                .child(new Text(title, new TextProps().fontSize(19))));
    }

    Component ContainerRight(){

        State<String> imagemState = new State<>("/assets/produto-generico.png");

        return new Card(
                new Column(new ColumnProps().centerHorizontally().spacingOf(15))
                    .child(new Text("Foto do produto",new TextProps().fontSize(20).bold()))
                    .child(new Image(imagemState, new ImageProps().size(120)))
                        .child(new SpacerVertical().fill())
                    .child(new Button("Inserir imagem", new ButtonProps().fontSize(20).bgColor("#A6B1E1"))),
                new CardProps().height(300).padding(20)
        );
    }
     Component ContainerLeft (){
        var rowProps = new RowProps().spacingOf(10);
        return new Column(new ColumnProps().spacingOf(20))
                .child(
                        new Row(rowProps)
                        .child(new Row(new RowProps().bottomVertically())
                                        .child(InputColumn("SKU(Código de barras)", vm.codigoBarras))
                                        .child(new Button("Gerar", new ButtonProps().height(40)))
                        )
                        .child(InputColumn("Descrição curta", vm.descricao))
                                .child(SelectColumn("Unidade", vm.unidades ,vm.unidadeSelected))
                                .child(InputColumn("Marca", vm.marca))
                ).child(new Row(rowProps)
                        .child(InputColumn("Preço de compra", vm.precoCompra, Entypo.CREDIT))
                    .child(InputColumn("Margem %", vm.margem))
                    .child(InputColumn("Lucro", vm.lucro,Entypo.CREDIT))
                        .child(InputColumn("Preço de venda", vm.precoVenda,Entypo.CREDIT))
                ).child(new Row(rowProps)
                        .child(SelectColumn("Categoria",vm.categorias, vm.categoriaSelected))
                        .child(SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected))//fornecedor padrão
                        .child(InputColumn("Garantia", vm.garantia))
                        .child(InputColumn("Validade", vm.validade))
                        .child(InputColumn("Comissão", vm.comissao))
                )
                .child(new Row(rowProps)
                        .child(TextAreaColumn("Observações", vm.observacoes))
                        .child(InputColumn("Estoque", vm.estoque))//fornecedor padrão
                );
    }

    Component SelectColumn(String label,List<String> list, State<String> stateSelected){
        return new Column()
                .child(new Text(label, new TextProps().fontSize(25)))
                .child(new Select<String>(new SelectProps().height(40))
                        .items(list)
                        .value(stateSelected));
    }

    Component TextAreaColumn(String label, State<String> inputState){
        return new Column()
                .child(new Text(label, new TextProps().fontSize(22)))
                .child(new TextAreaInput(inputState,new InputProps().fontSize(20).height(140)));
    }

    Component InputColumn(String label, State<String> inputState){
        return new Column()
                .child(new Text(label, new TextProps().fontSize(22)))
                .child(new Input(inputState,new InputProps().fontSize(20).height(40)));
    }

    private static final NumberFormat BRL =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    Component InputColumn(String label, State<String> inputState, Ikon icon){
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
                    .child(new Text(label, new TextProps().fontSize(22)))
                    .child(input.left(fonticon));
    }
}

package my_app.screens.profileScreen;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static org.kordamp.ikonli.entypo.Entypo.SLIDESHARE;

public class ProdutoScreen {
    Router router;

    public ProdutoScreen(Router router) {
    }

    State<String> codigoBarrasState = new State<>("123456789");
    State<String> descState = new State<>("123456789");

    List<String> unidades = List.of("UN","KG","ml");
    State<String> unidadeSelected = new State<>("UN");

    List<String> categorias = List.of("Padrão");
    State<String> categoriaSelected = new State<>("Padrão");

    List<String> fornecedores = List.of("Fornecedor Padrão");
    State<String> fornecedorSelected = new State<>("Fornecedor Padrão");

    State<String> observações = new State<>("");


    public Component render (){
        return new Column(new ColumnProps().paddingAll(15))
                .child(
                        new Row(new RowProps().paddingAll(20)
                                .spacingOf(20), new RowStyler().borderWidth(1).borderColor("black").borderRadius(1))
                                .child(ContainerLeft())
                                .child(ContainerRight())
                )
                .child(new SpacerVertical(30))
                .child(new Text("Informaçoẽs", new TextProps().fontSize(30)))
                .child(new Text("Dica 1: Pressione CTRL + G para gerar o código de barras",new TextProps().fontSize(24).color("orange")))
                .child(new Text("Dica 2: O sistema não permite gravar produtos diferentes com o mesmo código de barras!",new TextProps().fontSize(24).color("orange")))
                ;
    }


    Component ContainerRight(){

        State<String> imagemState = new State<>("/assets/produto-generico.png");

        return new Column()
                .child(new Text("Foto do produto",new TextProps().fontSize(20).bold()))
                .child(new Image(imagemState, new ImageProps().size(200)))
                .child(new Button("Inserir imagem", new ButtonProps().fontSize(20)));
    }
     Component ContainerLeft (){
        var rowProps = new RowProps().spacingOf(10);
        return new Column(new ColumnProps().spacingOf(20))
                .child(
                        new Row(rowProps)
                        .child(new Row(new RowProps().bottomVertically())
                                        .child(InputColumn("SKU(Código de barras)", codigoBarrasState))
                                        .child(new Button("Gerar", new ButtonProps().height(40)))

                        )
                        .child(InputColumn("Descrição", descState))
                ).child(new Row(rowProps)
                        .child(SelectColumn("Unidade", unidades ,unidadeSelected))
                        .child(InputColumn("Preço de compra", descState, Entypo.CREDIT))
                    .child(InputColumn("Margem %", codigoBarrasState))
                    .child(InputColumn("Lucro", descState,Entypo.CREDIT))
                        .child(InputColumn("Preço de venda", descState,Entypo.CREDIT))
                )
                .child(new Row(rowProps)
                        .child(SelectColumn("Categoria",categorias, categoriaSelected))
                        .child(SelectColumn("Fornecedor", fornecedores, fornecedorSelected))//fornecedor padrão
                        .child(InputColumn("Margem %", codigoBarrasState))
                        .child(InputColumn("Lucro", descState))
                        .child(InputColumn("Preço de venda", descState))
                )
                .child(new Row(rowProps)
                        .child(InputColumn("Garantia", codigoBarrasState))
                        .child(InputColumn("Marca", descState))//fornecedor padrão
                        .child(InputColumn("Validade", codigoBarrasState))
                        .child(InputColumn("Comissão", descState))
                )
                .child(new Row(rowProps)
                        .child(TextAreaColumn("Observações", observações))
                        .child(InputColumn("Estoque", descState))//fornecedor padrão
                )

                ;
    }

    Component SelectColumn(String label,List<String> list, State<String> stateSelected){
        return new Column()
                .child(new Text(label, new TextProps().fontSize(25)))
                .child(new Select<String>(new SelectProps().height(40))
                        .items(list)
                        .value(stateSelected))
                ;
    }

    Component TextAreaColumn(String label, State<String> inputState){
        return new Column()
                .child(new Text(label, new TextProps().fontSize(25)))
                .child(new TextAreaInput(inputState,new InputProps().fontSize(20).height(140)));
    }

    Component InputColumn(String label, State<String> inputState){
        return new Column()
                .child(new Text(label, new TextProps().fontSize(25)))
                .child(new Input(inputState,new InputProps().fontSize(20).height(40)))
                ;
    }

    Component InputColumn(String label, State<String> inputState, Ikon icon){
            var fonticon = FontIcon.of(icon, 15, Color.web("green"));
            return new Column()
                    .child(new Text(label, new TextProps().fontSize(25)))
                    .child(new Input(inputState,new InputProps().fontSize(20).height(40)).left(fonticon));
    }
}

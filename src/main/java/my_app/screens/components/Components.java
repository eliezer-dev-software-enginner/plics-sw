package my_app.screens.components;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.*;
import megalodonte.styles.ColumnStyler;
import megalodonte.styles.TextStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.screens.produtoScreen.ProdutoScreen;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

public class Components {

    static Theme theme = ThemeManager.theme();
    
    public static Component errorText(String message){
        return new Column(new ColumnProps(), new ColumnStyler().bgColor("white")).c_child(new SpacerVertical(5))
                .c_child(new Text(message, new TextProps().variant(TextVariant.SUBTITLE), new TextStyler().color("red")));
    }

    public static Row commonCustomMenus(Runnable onClickNew ,Runnable onEdit, Runnable onDelete){
        return new Row(new RowProps().spacingOf(20))
                .r_child(MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", () -> executar(onClickNew::run)))
                .r_child(MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(onEdit::run)))
                .r_child(MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(onDelete::run)))
                .r_child(new SpacerHorizontal().fill())
                //.r_child(MenuItem("Sair", Entypo.REPLY, "red", () -> router.closeSpawn("cad-produtos/"+id)));
                ;
    }

    public static Component MenuItem(String title, Ikon ikon, String color, Runnable onClick){

        var icon = Component.CreateFromJavaFxNode(FontIcon.of(ikon, 40, Color.web(color)));

        return new Clickable(new Card(
                new Column(new ColumnProps().centerHorizontally())
                        .c_child(icon)
                        .c_child(new SpacerVertical(6))
                        .c_child(new Text(title, new TextProps().variant(TextVariant.BODY)))
        ), onClick
        );
    }


    public static Component searchInput(State<String> stateInput, String placeholder){
        var icon = FontIcon.of(AntDesignIconsOutlined.SEARCH, 20, Color.web(theme.colors().secondary()));
        return new Input(stateInput,
                new InputProps().placeHolder(placeholder)
                        .width(300)
                        .height(35))
                .left(icon);
    }

    /**
     * Exemplo de uso do componente Clickable com efeito TouchableOpacity.
     */
    public static Component clickableButton(String text, Runnable onClick) {
        return new Clickable(
            new Text(text, new TextProps().variant(TextVariant.BODY)),
            onClick,
            new ClickableProps()
                .padding(12)
                .borderRadius(8)
                .backgroundColor(theme.colors().primary())
                .hoverColor("rgba(0,0,0,0.05)")
                .activeColor("rgba(0,0,0,0.1)")
        );
    }

    /**
     * Exemplo de Clickable com ícone e texto.
     */
    public static Component clickableButtonWithIcon(String text, Ikon icon, String color, Runnable onClick) {
        var iconNode = Component.CreateFromJavaFxNode(FontIcon.of(icon, 16, Color.web(color)));
        
        return new Clickable(
            new Row(new RowProps().spacingOf(8))
                .r_child(iconNode)
                .r_child(new Text(text, new TextProps().variant(TextVariant.BODY))),
            onClick,
            new ClickableProps()
                .padding(10)
                .borderRadius(6)
                .backgroundColor("white")
                .hoverColor("rgba(0,0,0,0.03)")
                .activeColor("rgba(0,0,0,0.06)")
        );
    }

    /**
     * Exemplo de formulário com larguras controladas.
     */
    public static Component formRowWithConstraints(String labelText, Component inputComponent) {
        return new Row(new RowProps()
            .spacingOf(10)
            .width(400)
            .paddingAll(5))
            .r_child(new Text(labelText, new TextProps().variant(TextVariant.BODY)))
            .r_child(inputComponent);
    }

    /**
     * Exemplo de coluna com largura e espaçamento controlados.
     */
    public static Component columnWithWidthConstraints() {
        return new Column(new ColumnProps()
            .width(300)
            .maxWidth(500)
            .spacingOf(15)
            .paddingAll(20))
            .c_child(new Text("Título da Coluna", new TextProps().variant(TextVariant.SUBTITLE)))
            .c_child(new SpacerVertical(10))
            .c_child(new Text("Conteúdo da coluna com largura fixa."))
            .c_child(clickableButton("Ação Principal", () -> IO.println("Botão principal clicado")));
    }

    /**
     * Exemplo de alinhamento horizontal centralizado.
     */
    public static Component centeredRow() {
        return new Row(new RowProps()
            .width(400)
            .spacingOf(15)
            .centerHorizontally()
            .paddingAll(10))
            .r_child(new Text("Item 1"))
            .r_child(new Text("Item 2"))
            .r_child(new Text("Item 3"));
    }

    /**
     * Exemplo de alinhamento vertical centralizado.
     */
    public static Component centeredColumn() {
        return new Column(new ColumnProps()
            .width(300)
            .spacingOf(20)
            .centerVertically()
            .paddingAll(15))
            .c_child(new Text("Topo"))
            .c_child(new Text("Meio"))
            .c_child(new Text("Fundo"));
    }

    /**
     * Exemplo de card com alinhamento controlado.
     */
    public static Component alignedCard() {
        return new Card(new Column(new ColumnProps()
            .width(350)
            .spacingOf(10))
            .c_child(new Row(new RowProps()
                .width(320)
                .centerHorizontally())
                .r_child(new Text("Título Alinhado", new TextProps().variant(TextVariant.SUBTITLE))))
            .c_child(new Row(new RowProps()
                .width(320)
                .centerHorizontally())
                .r_child(new Button("Botão Centralizado", new ButtonProps().primary().onClick(() -> IO.println("Botão centralizado clicado"))))));
    }


    /**
     * Exemplo de botões com variantes de tema.
     */
    public static Component themeButtons() {
        return new Column(new ColumnProps()
            .spacingOf(15)
            .width(400))
            .c_child(new Text("Botões com Temas", new TextProps().variant(TextVariant.TITLE)))
            .c_child(new Row(new RowProps()
                .spacingOf(10))
                .r_child(new Button("Primary", new ButtonProps().primary().height(45).onClick(() -> IO.println("Primary clicked"))))
                .r_child(new Button("Secondary", new ButtonProps().secondary().height(45).onClick(() -> IO.println("Secondary clicked"))))
                .r_child(new Button("Success", new ButtonProps().success().height(45).onClick(() -> IO.println("Success clicked")))))
            .c_child(new Button("Warning", new ButtonProps().warning().height(45).onClick(() -> IO.println("Warning clicked"))));
    }



@FunctionalInterface
interface Action {
    void run() throws Exception;
}


private static void executar(Action action) {
        try {
            action.run();
            IO.println("Operation completed successfully");
        } catch (Exception e) {
            IO.println("Error: " + e.getMessage());
        }
    }
}
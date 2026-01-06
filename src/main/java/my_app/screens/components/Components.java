package my_app.screens.components;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.TextProps;
import megalodonte.props.TextVariant;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
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

    public static Component commonCustomMenus(Runnable onAdd ,Runnable onEdit, Runnable onDelete){
        return new Row(new RowProps().spacingOf(20))
                .r_child(MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", () -> executar(onAdd::run)))
                .r_child(MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(onEdit::run)))
                .r_child(MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(onDelete::run)))
                .r_child(new SpacerHorizontal().fill())
                //.r_child(MenuItem("Sair", Entypo.REPLY, "red", () -> router.closeSpawn("cad-produtos/"+id)));
        ;
    }

    private static void executar(Action action) {
        try {
            action.run();
            IO.println("Operação realizada com sucesso");
        } catch (Exception e) {
            IO.println(e.getMessage());
        }
    }

    public static Component MenuItem(String title, Ikon ikon, String color, Runnable onClick){

        var icon = Component.FromJavaFxNode(FontIcon.of(ikon, 40, Color.web(color)));

        return new Card(
                new Column(new ColumnProps().centerHorizontally())
                        .c_child(new Clickable(icon).onClick(onClick::run))
                        .c_child(new SpacerVertical(6))
                        .c_child(new Text(title, new TextProps().variant(TextVariant.BODY))));
    }

    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }



    public static Component searchInput(State<String> stateInput, String placeholder){
        var icon = FontIcon.of(AntDesignIconsOutlined.SEARCH, 20, Color.web(theme.colors().secondary()));
        return new Input(stateInput,
                new InputProps().placeHolder(placeholder)
                        .width(300)
                        .height(35))
                .left(icon);
    }
}

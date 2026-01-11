package my_app.screens.authScreen;


import megalodonte.components.*;
import megalodonte.props.ButtonProps;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.Router;
import megalodonte.styles.RowStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.DBInitializer;

public class AccessScreen {
    Router router;

    public AccessScreen(Router router) {
        this.router = router;
    }

    private Theme theme = ThemeManager.theme();

    public Component render (){
        return new Column(new ColumnProps().centerHorizontally()).c_child(
                new Column(new ColumnProps().centerHorizontally().width(400)
                        .maxWidth(400).paddingTop(100).spacingOf(10))
                        .c_child(new Text("Pliqs", new TextProps().variant(TextVariant.TITLE)))
                        .c_child(new Text("Acesso padrão configurado como", new TextProps().variant(TextVariant.BODY)))
                        .c_child(textRow()))
                .c_child(new SpacerVertical(20))
                .c_child(new Button("Entrar no sistema",
                        new ButtonProps()
                                .bgColor(theme.colors().primary())
                                .textColor("#fff")
                                .fontSize(theme.typography().body())
                                .onClick(()-> handleClick())))
                ;
    }

    private void handleClick(){
        DBInitializer.init();
        router.navigateTo("home");
    }

    public Component textRow(){
        return new Row(new RowProps().width(200).maxWidth(300).centerHorizontally(), new RowStyler().bgColor("yellow"))
                .r_child(new Text("usuário", new TextProps().variant(TextVariant.BODY)))
                .r_child(new Text(" admin", new TextProps().variant(TextVariant.BODY).bold()))
                .r_child(new Text(" e senha", new TextProps().variant(TextVariant.BODY)))
                .r_child(new Text(" 1234", new TextProps().variant(TextVariant.BODY).bold()))

                ;
    }
}

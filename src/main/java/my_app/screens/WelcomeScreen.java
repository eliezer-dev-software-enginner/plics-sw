package my_app.screens;


import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.repositories.PreferenciasRepository;

public class WelcomeScreen {
    private final Router router;

    public WelcomeScreen(Router router) {
        this.router = router;
    }

    private final Theme theme = ThemeManager.theme();

    public Component render (){
        return new Column(new ColumnProps().centerHorizontally()).c_child(
                new Column(new ColumnProps().centerHorizontally().width(400)
                        .maxWidth(400).paddingTop(100).spacingOf(10))
                        .children(
                                new Image("logo_256x256.png", new ImageProps().size(100))
                                        .attachAnimation(it->{
                                            ScaleTransition zoom = new ScaleTransition(Duration.millis(850), it.getNode());
                                            zoom.setFromX(1.0); zoom.setFromY(1.0);  // Tamanho original
                                            zoom.setToX(1.5); zoom.setToY(1.5);      // Aumenta 50%
                                            zoom.setAutoReverse(true);
                                            zoom.setCycleCount(2);
                                            return zoom;
                                        }),
                                new Text("Plics SW", (TextProps) new TextProps().variant(TextVariant.TITLE).bold()),
                                new Text("Plics - Sistema de gestão para pequenos negócios. Controle vendas, compras, estoque e financeiro.",
                                        new TextProps().variant(TextVariant.SUBTITLE)),
                                new LineHorizontal(),
                                new Text("Acesso padrão configurado como", new TextProps().variant(TextVariant.BODY)),
                                textRow(),
                                new SpacerVertical(20),
                                new Button("Entrar no sistema",
                                        new ButtonProps()
                                                .fontSize(theme.typography().body()).textColor("#fff").bgColor(theme.colors().primary()))
                                        .onClick(this::handleClick)
                        )
        );
    }

    private void handleClick(){
//        try {
//            var prefs = new PreferenciasRepository().listar();
//            if (!prefs.isEmpty()) {
//                var pref = prefs.getFirst();
//                pref.primeiroAcesso = 0;
//                new PreferenciasRepository().atualizar(pref);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //router.navigateTo("home");
        router.navigateTo("entrar-com-credenciais");
    }

    public Component textRow(){
        return new Row(new RowProps().width(200).maxWidth(300).centerHorizontally().bgColor("yellow"))
                .children(
                        new Text("usuário", new TextProps().variant(TextVariant.BODY)),
                        new Text(" admin", (TextProps) new TextProps().variant(TextVariant.BODY).bold()),
                        new Text(" e senha", new TextProps().variant(TextVariant.BODY)),
                        new Text(" 1234", (TextProps) new TextProps().variant(TextVariant.BODY).bold())
                );
    }
}

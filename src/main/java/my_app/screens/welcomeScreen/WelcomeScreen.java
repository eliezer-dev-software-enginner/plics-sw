package my_app.screens.welcomeScreen;

import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.components.Ref;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;

public class WelcomeScreen implements ScreenComponent {
    private final ScreenContext ctx;
    private final Ref<Image> logoRef = new Ref<>();

    public WelcomeScreen(ScreenContext ctx) {
        this.ctx = ctx;
    }

    private final ThemeInterface theme = ThemeManager.theme();

    @Override
    public void onMount() {
        UI.runOnUi(() -> {
            ScaleTransition zoom = new ScaleTransition(Duration.millis(850), logoRef.current().getNode());
            zoom.setFromX(1.0);
            zoom.setFromY(1.0);
            zoom.setToX(1.5);
            zoom.setToY(1.5);
            zoom.setAutoReverse(true);
            zoom.setCycleCount(2);
            zoom.play();
        });
    }

    public Component render() {
        return new Container(new ContainerProps().bgImage("/assets/wallpapers/welcome.jpg")).children(
                new Column(new ColumnProps().centerHorizontally()).c_child(
                        new Column(new ColumnProps().centerHorizontally().width(400)
                                .maxWidth(400).paddingTop(100).spacingOf(10))
                                .children(
                                        new Image("assets/app_banner.png", new ImageProps().width(400).height(200).preserveRatio(true))
                                                .ref(logoRef),
                                        //new Text("Plics SW", new TextProps().variant(TextVariant.TITLE).bold().color(ThemeManager.theme().colors().secondary())),
                                        new Text("Plics - Sistema de gestão para pequenos negócios. Controle vendas, compras, estoque e financeiro.",
                                                new TextProps().variant(TextVariant.SUBTITLE).color("#fff")),
                                        new LineHorizontal(),
                                        new Text("Acesso padrão configurado como",
                                                new TextProps().variant(TextVariant.BODY).color("#fff")),
                                        textRow(),
                                        new SpacerVertical(20),
                                        new Button("Entrar no sistema",
                                                new ButtonProps()
                                                        .fontSize(theme.typography().body()).textColor("#fff")
                                                        .bgColor(theme.colors().primary()))
                                                .onClick(this::handleClick)
                                )
                )
        );
    }

    private void handleClick() {
        ctx.navigate("entrar-com-credenciais");
    }

    public Component textRow() {
        return new Row(new RowProps().width(200).maxWidth(300).centerHorizontally().bgColor(ThemeManager.theme().colors().border()))
                .children(
                        new Text("usuário", new TextProps().variant(TextVariant.BODY).color("#fff")),
                        new Text(" admin", new TextProps().variant(TextVariant.BODY).bold().color("#fff")),
                        new Text(" e senha", new TextProps().variant(TextVariant.BODY).color("#fff")),
                        new Text(" 1234", new TextProps().variant(TextVariant.BODY).bold().color("#fff"))
                );
    }
}

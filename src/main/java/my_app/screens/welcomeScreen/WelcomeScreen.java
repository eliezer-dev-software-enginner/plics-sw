package my_app.screens.welcomeScreen;

import disgust.io.ButtonsPack;
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
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.AppRoutes;

public class WelcomeScreen implements ScreenComponent {
    private final ScreenContextInterface ctx;
    private final Ref<Image> logoRef = new Ref<>();

    public WelcomeScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
    }

    private final ThemeInterface theme = ThemeManager.theme();

    @Override
    public void onMount() {
        UI.runOnUi(() -> {
            ScaleTransition zoom = new ScaleTransition(Duration.millis(850), logoRef.current().getJavaFxNode());
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
                new Column(new ColumnProps().centerHorizontally().paddingTop(50).spacingOf(10)).children(
                        new Image("assets/app_banner.png", new ImageProps().width(500).height(400))
                                .ref(logoRef),
                        new TextFlow(new Text("Plics - Sistema de gestão para pequenos e médios negócios. Controle vendas, compras, estoque e financeiro.",
                                new TextProps().fontSize(ThemeManager.theme().typography().subtitle()).textColor("#fff"))
                        ),
                        new LineHorizontal(),
                        new Text("Acesso padrão configurado como",
                                new TextProps().fontSize(ThemeManager.theme().typography().body()).textColor("#fff")),
                        textRow(),
                        new SpacerVertical(20),
                        ButtonsPack.ContainedButton("Entrar no sistema", ButtonVariant.PRIMARY, this::handleClick)
                )
        );
    }

    private void handleClick() {
        ctx.navigate(AppRoutes.Screens.AUTH.name());
    }

    public Component textRow() {
        return new Row(new RowProps().width(200).maxWidth(300).centerHorizontally().bgColor(ThemeManager.theme().colors().border()))
                .children(
                        new Text("usuário", new TextProps().fontSize(ThemeManager.theme().typography().body()).textColor("#fff")),
                        new Text(" admin", new TextProps().fontSize(ThemeManager.theme().typography().body()).bold().textColor("#fff")),
                        new Text(" e senha", new TextProps().fontSize(ThemeManager.theme().typography().body()).textColor("#fff")),
                        new Text(" 1234", new TextProps().fontSize(ThemeManager.theme().typography().body()).bold().textColor("#fff"))
                );
    }
}

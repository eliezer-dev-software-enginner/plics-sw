package my_app.screens.acessoBloqueadoScreen;

import javafx.scene.paint.Color;
import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Button;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ButtonProps;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import my_app.domain.Data;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

public class AcessoBloqueadoScreen implements ScreenComponent {

    @Override
    public Component render() {
        return new Column(new ColumnProps().centerHorizontally().centerVertically().spacingOf(15).paddingAll(30))
                .children(
                        Component.CreateFromJavaFxNode(FontIcon.of(AntDesignIconsOutlined.LOCK, 64, Color.web("#ef4444"))),
                        new Text("Acesso bloqueado", new TextProps().fontSize(ThemeManager.theme().typography().title()).bold()),
                        new Text("Não foi possível confirmar o acesso ao aplicativo no momento.",
                                new TextProps().fontSize(ThemeManager.theme().typography().body())),
                        new SpacerVertical(15),
                        new Button("Suporte via WhatsApp",
                                new ButtonProps().bgColor("#25D366").textColor("black"))
                                .onClick(() -> Redirect.to(Data.linkWhatsappSupport))
                );
    }
}

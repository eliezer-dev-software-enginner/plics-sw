package my_app;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.components.Ref;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Image;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.props.ImageProps;
import megalodonte.props.TextProps;
import megalodonte.utils.related.TextVariant;

public class SplashScreen implements ScreenComponent {

    private final Ref<Text> carregandoRef = new Ref<>();

    @Override
    public void onMount() {
        UI.runOnUi(() -> {
            var pulse = new FadeTransition(Duration.millis(700), carregandoRef.current().getNode());
            pulse.setFromValue(0.4);
            pulse.setToValue(1.0);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(FadeTransition.INDEFINITE);
            pulse.play();
        });
    }

    @Override
    public Component render() {
        return new Column(new ColumnProps().centerHorizontally().centerVertically())
                .children(
                        new Image("logo_256x256.png", new ImageProps().size(96)),
                        new SpacerVertical(16),
                        new Text("Plics SW", new TextProps().variant(TextVariant.TITLE).bold()),
                        new SpacerVertical(8),
                        new Text("Carregando...", new TextProps().variant(TextVariant.SUBTITLE))
                                .ref(carregandoRef)
                );
    }
}
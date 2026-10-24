package my_app;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;

public class SplashScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(SplashScreen.class, "/views/splash.view.jsonc");
    private final UiSession session;
    private FadeTransition pulse;

    public SplashScreen() {
        session = VIEW.open(new megalodonte.jsonc.Actions());
        try {
            session.states().string("versao").set(Main.APP_VERSION);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onMount() {
        Platform.runLater(() -> {
            try {
                var loading = session.component("carregando");
                if (loading != null) {
                    pulse = new FadeTransition(Duration.millis(700), loading.getJavaFxNode());
                    pulse.setFromValue(0.4);
                    pulse.setToValue(1.0);
                    pulse.setAutoReverse(true);
                    pulse.setCycleCount(FadeTransition.INDEFINITE);
                    pulse.play();
                }
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public Component render() {
        return session.render();
    }

    @Override
    public void onDestroy() {
        if (pulse != null) {
            pulse.stop();
        }
        session.close();
    }
}

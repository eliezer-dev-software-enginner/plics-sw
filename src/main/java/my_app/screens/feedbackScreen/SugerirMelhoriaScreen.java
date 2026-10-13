package my_app.screens.feedbackScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;

public final class SugerirMelhoriaScreen implements ScreenComponent {
    private final FeedbackScreen screen;

    public SugerirMelhoriaScreen(ScreenContextInterface context) {
        screen = new FeedbackScreen(context, "/views/sugerir-melhoria.view.jsonc");
    }

    @Override public Component render() { return screen.render(); }

    @Override public void onDestroy() { screen.onDestroy(); }
}

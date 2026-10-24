package my_app.screens.feedbackScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.components.Components;

import java.util.concurrent.atomic.AtomicBoolean;

/** Suggestion screen driven by native components declared in JSONC. */
public final class SugerirMelhoriaScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(SugerirMelhoriaScreen.class, "/views/sugerir-melhoria.view.jsonc");
    private final FeedbackViewModel viewModel = new FeedbackViewModel();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final UiSession session;

    public SugerirMelhoriaScreen(ScreenContextInterface context) {
        session = VIEW.open(new Actions().register("enviar", screen -> {
            viewModel.content.set(screen.states().string("conteudo").get());
            viewModel.send(() -> {
                if (!active.get()) return;
                screen.states().string("conteudo").set("");
                Components.ShowPopup(context, "Enviado com sucesso");
            });
        }));
    }

    @Override
    public Component render() {
        return session.render();
    }

    @Override
    public void onDestroy() {
        active.set(false);
        session.close();
    }
}

package my_app.screens.acessoBloqueadoScreen;

import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;

/** The view tree and presentation live in the JSONC resource. */
public final class AcessoBloqueadoScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(
            AcessoBloqueadoScreen.class, "/views/acesso-bloqueado.view.jsonc");
    private final UiSession session = VIEW.open(new Actions()
            .register("abrirSuporte", ignored -> Redirect.to(Data.linkWhatsappSupport)));

    @Override public Component render() { return session.render(); }

    @Override public void onDestroy() { session.close(); }
}

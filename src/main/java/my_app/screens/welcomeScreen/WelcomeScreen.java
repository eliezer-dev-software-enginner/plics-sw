package my_app.screens.welcomeScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.AppRoutes;

/** Entry screen driven by native components declared in JSONC. */
public final class WelcomeScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(WelcomeScreen.class, "/views/welcome.view.jsonc");
    private final UiSession session;

    public WelcomeScreen(ScreenContextInterface context) {
        session = VIEW.open(new Actions().register("entrar", ignored ->
                context.navigate(AppRoutes.Screens.AUTH.name())));
    }

    @Override public Component render() { return session.render(); }

    @Override public void onDestroy() { session.close(); }
}

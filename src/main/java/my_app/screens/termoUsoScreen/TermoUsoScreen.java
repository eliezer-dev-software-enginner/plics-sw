package my_app.screens.termoUsoScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;

/** The privacy policy content and layout are maintained in the declarative view. */
public final class TermoUsoScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(TermoUsoScreen.class, "/views/termo-uso.view.jsonc");
    private final UiSession session;

    public TermoUsoScreen(ScreenContextInterface context) {
        session = VIEW.open(new Actions());
        session.states().string("site").set(Data.linkWebsiteOfficial);
    }

    @Override public Component render() { return session.render(); }
    @Override public void onDestroy() { session.close(); }
}

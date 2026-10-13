package my_app.screens.welcomeScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WelcomeViewTest {
    @Test void nativeEntryViewDeclaresNavigationAction() {
        var definition = UiDefinition.fromResource(getClass(), "/views/welcome.view.jsonc");
        try (var session = definition.open(new Actions().register("entrar", ignored -> {}))) {
            assertEquals("welcome", session.id());
        }
    }
}

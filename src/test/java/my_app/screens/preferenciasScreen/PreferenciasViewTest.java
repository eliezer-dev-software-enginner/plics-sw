package my_app.screens.preferenciasScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferenciasViewTest {
    @Test void declaresPreferencesAndSignOutConfirmation() {
        var definition = UiDefinition.fromResource(getClass(), "/views/preferencias.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("save", ignored -> {})
                .register("askSignOut", ignored -> {})
                .register("cancelSignOut", ignored -> {})
                .register("signOut", ignored -> {}))) {
            assertEquals("preferences", session.id());
            assertEquals("Não", session.states().string("credentialsEnabled").get());
            assertEquals(false, session.states().state("confirmSignOut", Boolean.class).get());
        }
    }
}

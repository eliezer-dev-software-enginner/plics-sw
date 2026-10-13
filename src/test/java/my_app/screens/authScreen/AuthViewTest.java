package my_app.screens.authScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthViewTest {
    @Test void nativeAuthenticationViewDeclaresItsActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/auth.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("entrar", ignored -> {})
                .register("abrirSuporte", ignored -> {})
                .register("abrirSite", ignored -> {}))) {
            assertEquals("auth", session.id());
            assertEquals(true, session.states().state("carregando", Boolean.class).get());
        }
    }
}

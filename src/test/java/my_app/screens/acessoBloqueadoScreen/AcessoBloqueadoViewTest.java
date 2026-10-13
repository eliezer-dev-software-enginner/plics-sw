package my_app.screens.acessoBloqueadoScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcessoBloqueadoViewTest {
    @Test void resourceIsValidAndActionsAreRegistered() {
        var definition = UiDefinition.fromResource(getClass(), "/views/acesso-bloqueado.view.jsonc");
        assertEquals("acesso_bloqueado", definition.id());
        try (var session = definition.open(new Actions().register("abrirSuporte", ignored -> {}))) {
            assertEquals("acesso_bloqueado", session.id());
        }
    }
}

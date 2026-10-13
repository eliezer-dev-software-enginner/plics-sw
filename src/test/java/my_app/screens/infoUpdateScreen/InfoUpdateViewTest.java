package my_app.screens.infoUpdateScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfoUpdateViewTest {
    @Test void releaseNotesListAndSelectionAreDeclared() {
        var definition = UiDefinition.fromResource(getClass(), "/views/info-update.view.jsonc");
        try (var session = definition.open(new Actions().register("selecionarNota", ignored -> {}))) {
            assertEquals("info_update", session.id());
            assertEquals(-1, session.states().state("selecionada", Integer.class).get());
        }
    }
}

package my_app.screens.ler_planilha_ia;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LerPlanilhaViewTest {
    @Test void spreadsheetImportDeclaresActionAndStatus() {
        var definition = UiDefinition.fromResource(getClass(), "/views/ler-planilha.view.jsonc");
        try (var session = definition.open(new Actions().register("import", ignored -> {}))) {
            assertEquals("spreadsheetImport", session.id());
            assertEquals("", session.states().string("status").get());
        }
    }
}

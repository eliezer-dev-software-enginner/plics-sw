package my_app.screens.relatoriosScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelatoriosViewTest {
    @Test void financialReportDeclaresFiltersChartsAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/relatorios.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("generate", ignored -> {})
                .register("downloadPdf", ignored -> {}))) {
            assertEquals("financialReports", session.id());
            assertEquals("[]", session.states().string("comparativo").get());
        }
    }
}

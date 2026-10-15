package my_app.screens.vendaScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VendaListViewTest {
    @Test void salesViewLoadsWithSelectionAndRegisteredActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/venda-list.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("details", ignored -> {})
                .register("create", ignored -> {})
                .register("edit", ignored -> {})
                .register("delete", ignored -> {})
                .register("clone", ignored -> {})
                .register("exportCsv", ignored -> {})
                .register("exportPdf", ignored -> {}))) {
            assertEquals("salesList", session.id());
            assertEquals("[]", session.states().string("selectedItems").get());
        }
    }
}

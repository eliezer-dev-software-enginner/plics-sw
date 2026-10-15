package my_app.screens.contasAReceberScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContasAReceberListViewTest {
    @Test void receivablesViewLoadsWithSelectionAndRegisteredActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/contas-a-receber-list.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("details", ignored -> {})
                .register("create", ignored -> {})
                .register("edit", ignored -> {})
                .register("delete", ignored -> {})
                .register("clone", ignored -> {})
                .register("exportCsv", ignored -> {})
                .register("exportPdf", ignored -> {}))) {
            assertEquals("receivablesList", session.id());
            assertEquals("[]", session.states().string("selectedItems").get());
        }
    }
}

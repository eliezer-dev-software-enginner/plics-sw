package my_app.screens.clienteScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClienteListViewTest {
    @Test void customerTableDeclaresSelectionDetailsAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/cliente-list.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("details", ignored -> {})
                .register("create", ignored -> {})
                .register("edit", ignored -> {})
                .register("delete", ignored -> {})
                .register("clone", ignored -> {})
                .register("exportCsv", ignored -> {})
                .register("exportPdf", ignored -> {}))) {
            assertEquals("customerList", session.id());
            assertEquals("[]", session.states().string("selectedItems").get());
        }
    }
}

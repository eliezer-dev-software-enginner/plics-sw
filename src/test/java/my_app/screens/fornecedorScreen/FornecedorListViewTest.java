package my_app.screens.fornecedorScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FornecedorListViewTest {
    @Test void supplierTableDeclaresSelectionDetailsAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/fornecedor-list.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("details", ignored -> {})
                .register("create", ignored -> {})
                .register("edit", ignored -> {})
                .register("delete", ignored -> {})
                .register("clone", ignored -> {})
                .register("exportCsv", ignored -> {})
                .register("exportPdf", ignored -> {}))) {
            assertEquals("supplierList", session.id());
            assertEquals("[]", session.states().string("selectedItems").get());
        }
    }
}

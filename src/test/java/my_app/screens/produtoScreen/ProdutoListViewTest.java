package my_app.screens.produtoScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProdutoListViewTest {
    @Test void productTableDeclaresImageSelectionDetailsAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/produto-list.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("details", ignored -> {})
                .register("create", ignored -> {})
                .register("edit", ignored -> {})
                .register("delete", ignored -> {})
                .register("clone", ignored -> {})
                .register("exportCsv", ignored -> {})
                .register("exportPdf", ignored -> {}))) {
            assertEquals("productList", session.id());
            assertEquals("[]", session.states().string("selectedItems").get());
        }
    }
}

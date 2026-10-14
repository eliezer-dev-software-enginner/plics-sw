package my_app.screens.categoriaScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoriaListViewTest {
    @Test void categoryTableDeclaresSelectionAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/categoria-list.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("create", ignored -> {})
                .register("edit", ignored -> {})
                .register("delete", ignored -> {})
                .register("clone", ignored -> {})
                .register("exportCsv", ignored -> {})
                .register("exportPdf", ignored -> {}))) {
            assertEquals("categoryList", session.id());
            assertEquals("[]", session.states().string("selectedItems").get());
        }
    }
}

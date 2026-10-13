package my_app.screens.categoriaScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoriaFormViewTest {
    @Test void formBindsCategoryNameAndSaveAction() {
        var definition = UiDefinition.fromResource(getClass(), "/views/categoria-form.view.jsonc");
        try (var session = definition.open(new Actions().register("salvarCategoria", ignored -> {}))) {
            assertEquals("categoria_form", session.id());
            assertEquals("", session.states().string("nome").get());
        }
    }
}

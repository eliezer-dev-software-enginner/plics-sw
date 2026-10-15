package my_app.screens.produtoScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProdutoFormViewTest {
    @Test void productFormDeclaresImageAndInventoryFields() {
        var actions = new Actions().register("save", ignored -> {})
                .register("generateBarcode", ignored -> {})
                .register("chooseImage", ignored -> {});
        try (var session = UiDefinition.fromResource(getClass(), "/views/produto-form.view.jsonc").open(actions)) {
            assertEquals("productForm", session.id());
            assertEquals("", session.states().string("estoqueMinimo").get());
            assertEquals("", session.states().string("imagem").get());
        }
    }
}

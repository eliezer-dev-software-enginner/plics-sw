package my_app.screens.fornecedorScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FornecedorFormViewTest {
    @Test void supplierFormDeclaresAddressAndDocumentTypes() {
        try (var session = UiDefinition.fromResource(getClass(), "/views/fornecedor-form.view.jsonc")
                .open(new Actions().register("save", ignored -> {}))) {
            assertEquals("fornecedorForm", session.id());
            assertEquals("", session.states().string("documento").get());
        }
    }
}

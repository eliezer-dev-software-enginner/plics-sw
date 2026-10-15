package my_app.screens.vendaScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VendaFormViewTest {
    @Test void saleFormDeclaresProductSearchAndInstallments() {
        var actions = new Actions().register("save", ignored -> {})
                .register("selectProduct", ignored -> {})
                .register("generateInstallments", ignored -> {});
        try (var session = UiDefinition.fromResource(getClass(), "/views/venda-form.view.jsonc").open(actions)) {
            assertEquals("saleForm", session.id());
            assertEquals("[]", session.states().string("suggestions").get());
            assertEquals("[]", session.states().string("installments").get());
        }
    }
}

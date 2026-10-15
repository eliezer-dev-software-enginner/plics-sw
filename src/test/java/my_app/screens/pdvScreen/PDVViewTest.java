package my_app.screens.pdvScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PDVViewTest {
    @Test void pointOfSaleDeclaresCartPaymentsAndActions() {
        var actions = new Actions();
        for (var name : new String[]{"addProduct", "selectCart", "updateQuantity", "removeItem",
                "createCustomer", "finish", "print", "printAlternative"}) {
            actions.register(name, ignored -> {});
        }
        try (var session = UiDefinition.fromResource(getClass(), "/views/pdv.view.jsonc").open(actions)) {
            assertEquals("pointOfSale", session.id());
            assertEquals("[]", session.states().string("cart").get());
        }
    }
}

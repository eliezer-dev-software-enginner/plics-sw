package my_app.screens.pedidosScreen.details;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoDetailsViewTest {
    @Test void exchangeViewDeclaresProductSelectionAndConfirmation() {
        var actions = new Actions();
        for (var name : new String[]{"selectItem", "resetSearch", "replace", "confirm"})
            actions.register(name, ignored -> {});
        try (var session = UiDefinition.fromResource(getClass(), "/views/pedido-troca.view.jsonc").open(actions)) {
            assertEquals("orderExchange", session.id());
            assertEquals("[]", session.states().string("items").get());
        }
    }
}

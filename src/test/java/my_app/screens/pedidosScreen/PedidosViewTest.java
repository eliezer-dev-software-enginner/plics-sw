package my_app.screens.pedidosScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidosViewTest {
    @Test void orderHistoryDeclaresTwoTablesAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/pedidos.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("select", ignored -> {})
                .register("print", ignored -> {})
                .register("delete", ignored -> {})
                .register("return", ignored -> {})
                .register("exchange", ignored -> {}))) {
            assertEquals("ordersHistory", session.id());
            assertEquals("[]", session.states().string("orders").get());
            assertEquals("[]", session.states().string("items").get());
        }
    }
}

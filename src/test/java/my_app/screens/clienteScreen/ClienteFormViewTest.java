package my_app.screens.clienteScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClienteFormViewTest {
    @Test void customerFormDeclaresAddressAndDates() {
        try (var session = UiDefinition.fromResource(getClass(), "/views/cliente-form.view.jsonc")
                .open(new Actions().register("save", ignored -> {}))) {
            assertEquals("clienteForm", session.id());
            assertEquals("", session.states().string("cep").get());
        }
    }
}

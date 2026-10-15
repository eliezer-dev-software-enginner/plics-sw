package my_app.screens.contasAReceberScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContaAReceberFormViewTest {
    @Test void receivableFormDeclaresPaymentFlow() {
        var actions = new Actions()
                .register("save", ignored -> {})
                .register("startReceipt", ignored -> {})
                .register("registerReceipt", ignored -> {})
                .register("cancelReceipt", ignored -> {})
                .register("settle", ignored -> {});
        try (var session = UiDefinition.fromResource(getClass(), "/views/conta-a-receber-form.view.jsonc").open(actions)) {
            assertEquals("receivableForm", session.id());
            assertEquals(false, session.states().state("receiptVisible", Boolean.class).get());
            assertEquals("[]", session.states().string("clientOptions").get());
        }
    }
}

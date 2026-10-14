package my_app.screens.termoUsoScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TermoUsoViewTest {
    @Test void loadsCompletePrivacyPolicyView() {
        var definition = UiDefinition.fromResource(getClass(), "/views/termo-uso.view.jsonc");
        try (var session = definition.open(new Actions())) {
            assertEquals("privacyPolicy", session.id());
            assertEquals("", session.states().string("site").get());
        }
    }
}

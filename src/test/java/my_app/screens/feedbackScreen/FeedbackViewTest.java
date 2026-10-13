package my_app.screens.feedbackScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackViewTest {
    @Test void bothViewsUseTheDeclaredTextAreaAndDomainAction() {
        for (String name : new String[]{"relatar-erro", "sugerir-melhoria"}) {
            var definition = UiDefinition.fromResource(getClass(), "/views/" + name + ".view.jsonc");
            try (var session = definition.open(new Actions().register("enviar", ignored -> {}))) {
                assertEquals(name.replace('-', '_'), session.id());
                assertEquals("", session.states().string("conteudo").get());
            }
        }
    }
}

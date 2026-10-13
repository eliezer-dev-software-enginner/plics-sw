package my_app.screens.logsScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogsViewTest {
    @Test void actionsAndReadOnlyLogViewAreDeclared() {
        var definition = UiDefinition.fromResource(getClass(), "/views/logs.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("atualizar", ignored -> {})
                .register("abrirPasta", ignored -> {}))) {
            assertEquals("logs", session.id());
            assertEquals("Carregando...", session.states().string("conteudoLogs").get());
        }
    }
}

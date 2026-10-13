package my_app.screens.infoUpdateScreen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;

import java.util.List;
import java.util.Map;

/** Transforms release-note domain records into declarative list and detail state. */
public final class InfoUpdateScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(
            InfoUpdateScreen.class, "/views/info-update.view.jsonc");
    private final List<InfoUpdateScreenViewModel.NotaAtualizacao> notes =
            new InfoUpdateScreenViewModel().getNotas();
    private final UiSession session = VIEW.open(new Actions().register("selecionarNota", screen -> {
        int index = screen.states().state("selecionada", Integer.class).get();
        screen.states().string("detalhes").set(index >= 0 && index < notes.size()
                ? String.join("\n", notes.get(index).notes()) : "");
    }));

    public InfoUpdateScreen(ScreenContextInterface context) {
        try {
            var rows = notes.stream().map(note -> Map.of(
                    "label", note.version(),
                    "detail", note.notes().isEmpty() ? "" : note.notes().get(0))).toList();
            session.states().string("notas").set(new ObjectMapper().writeValueAsString(rows));
            if (!notes.isEmpty()) {
                session.states().state("selecionada", Integer.class).set(0);
                session.states().string("detalhes").set(String.join("\n", notes.get(0).notes()));
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not encode update notes", e);
        }
    }

    @Override public Component render() { return session.render(); }

    @Override public void onDestroy() { session.close(); }
}

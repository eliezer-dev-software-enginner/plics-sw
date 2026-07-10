package my_app.screens.infoUpdateScreen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.InputStream;
import java.util.List;

@Getter
public class InfoUpdateScreenViewModel {

    private final List<NotaAtualizacao> notas;

    public record NotaAtualizacao(String version, List<String> notes) {}

    public InfoUpdateScreenViewModel() {
        this.notas = carregarNotas();
    }

    private List<NotaAtualizacao> carregarNotas() {
        try (InputStream in = getClass().getResourceAsStream("/updates.json")) {
            if (in == null) return List.of();
            return new ObjectMapper().readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}

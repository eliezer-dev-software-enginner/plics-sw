package my_app.screens.infoUpdateScreen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

@Getter
public class InfoUpdateScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(InfoUpdateScreenViewModel.class);

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
            log.error("Erro ao carregar notas de atualização (updates.json)", e);
            return List.of();
        }
    }
}

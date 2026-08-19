package my_app.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.Executors;

public class UpdaterService {

    private static final Logger log = LoggerFactory.getLogger(UpdaterService.class);

    private static final String LATEST_RELEASE_URL =
        "https://api.github.com/repos/eliezer-dev-software-enginner/plics-sw/releases/latest";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public UpdaterService() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(Executors.newCachedThreadPool(r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("http-client-daemon");
                    return t;
                }))
                .build();
        this.mapper = new ObjectMapper();
    }

    public String getLatestVersion() throws IOException, InterruptedException {
        var json = fetchLatestRelease();
        var root = mapper.readTree(json);
        var tag = root.get("tag_name").asText("");
        return tag.startsWith("v") ? tag.substring(1) : tag;
    }

    public boolean hasUpdate(String currentVersion) throws IOException, InterruptedException {
        var latest = getLatestVersion();
        return !latest.equals(currentVersion);
    }

    private String fetchLatestRelease() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(LATEST_RELEASE_URL))
            .header("Accept", "application/vnd.github.v3+json")
            .GET()
            .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("GitHub API retornou {} ao buscar última release", response.statusCode());
            throw new IOException("GitHub API retornou " + response.statusCode());
        }

        return response.body();
    }

    /**
     * Remove diretórios temporários deixados pelo fluxo antigo de auto-atualização
     * (baixava o pacote em "plics-update-*" e lançava um updater externo) e por
     * "plics-kill-*" (scripts do ProcessKiller). O download automático foi removido
     * (ver HomeScreenViewModel.verificarAtualizacao — agora só redireciona pro site),
     * mas instalações antigas do app podem ter deixado esses diretórios pra trás, e
     * nenhum dos dois se autolimpava. Chamado no startup da aplicação principal:
     * nesse momento, qualquer diretório desses já é de uma sessão anterior, então é
     * sempre seguro apagar.
     */
    public static void cleanTempDirs() {
        cleanTempDirsWithPrefix("plics-update-");
        cleanTempDirsWithPrefix("plics-kill-");
    }

    private static void cleanTempDirsWithPrefix(String prefix) {
        try {
            var temp = Path.of(System.getProperty("java.io.tmpdir"));
            try (var dirs = Files.list(temp)) {
                dirs.filter(p -> p.getFileName().toString().startsWith(prefix))
                    .forEach(p -> {
                        log.info("Limpando diretório temporário de sessão anterior: {}", p);
                        try (var files = Files.walk(p)) {
                            files.sorted(Comparator.reverseOrder())
                                .forEach(f -> {
                                    try { Files.deleteIfExists(f); } catch (IOException e) {
                                        log.warn("Erro ao remover {}", f, e);
                                    }
                                });
                        } catch (IOException e) {
                            log.warn("Erro ao percorrer {}", p, e);
                        }
                    });
            }
        } catch (IOException e) {
            log.warn("Erro ao listar diretórios temporários com prefixo {}", prefix, e);
        }
    }
}

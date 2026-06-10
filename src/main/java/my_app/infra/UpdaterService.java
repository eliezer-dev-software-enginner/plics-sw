package my_app.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdaterService {

    private static final String LATEST_RELEASE_URL =
        "https://api.github.com/repos/eliezer-dev-software-enginner/plics-sw/releases/latest";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public UpdaterService() {
        this.client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
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
        return compareVersions(latest, currentVersion) > 0;
    }

    public String downloadLatestMsi() throws IOException, InterruptedException {
        var releaseJson = fetchLatestRelease();
        var downloadUrl = findMsiAsset(releaseJson);
        if (downloadUrl == null) {
            throw new IOException("Nenhum asset .msi encontrado na última release");
        }
        return downloadToTemp(downloadUrl);
    }

    static int compareVersions(String a, String b) {
        var pa = Pattern.compile("(\\d+)");
        var ma = pa.matcher(a);
        var mb = pa.matcher(b);
        while (ma.find() && mb.find()) {
            var na = Integer.parseInt(ma.group());
            var nb = Integer.parseInt(mb.group());
            if (na != nb) return na - nb;
        }
        return 0;
    }

    private String fetchLatestRelease() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(LATEST_RELEASE_URL))
            .header("Accept", "application/vnd.github.v3+json")
            .GET()
            .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("GitHub API retornou " + response.statusCode());
        }

        return response.body();
    }

    private String findMsiAsset(String json) throws IOException {
        var root = mapper.readTree(json);
        var assets = root.get("assets");
        if (assets == null || !assets.isArray()) return null;

        for (JsonNode asset : assets) {
            var name = asset.get("name").asText("");
            if (name.endsWith(".msi")) {
                return asset.get("browser_download_url").asText();
            }
        }
        return null;
    }

    private String downloadToTemp(String fileUrl) throws IOException, InterruptedException {
        var tempDir = Files.createTempDirectory("plics-update-");
        var fileName = extractFileName(fileUrl);
        var target = tempDir.resolve(fileName);

        var request = HttpRequest.newBuilder()
            .uri(URI.create(fileUrl))
            .GET()
            .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Download falhou com código " + response.statusCode());
        }

        try (var stream = response.body()) {
            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target.toAbsolutePath().toString();
    }

    private String extractFileName(String url) {
        int slash = url.lastIndexOf('/');
        return slash >= 0 ? url.substring(slash + 1) : "update.msi";
    }

    public static void cleanTempDirs() {
        try {
            var temp = Path.of(System.getProperty("java.io.tmpdir"));
            try (var dirs = Files.list(temp)) {
                dirs.filter(p -> p.getFileName().toString().startsWith("plics-update-"))
                    .forEach(p -> {
                        try (var files = Files.walk(p)) {
                            files.sorted(Comparator.reverseOrder())
                                .forEach(f -> {
                                    try { Files.deleteIfExists(f); } catch (IOException ignored) {}
                                });
                        } catch (IOException ignored) {}
                    });
            }
        } catch (IOException ignored) {}
    }
}

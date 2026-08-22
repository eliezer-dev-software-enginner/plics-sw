package my_app.domain.telegram;

import my_app.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TelegramNotifier {
    private final String botToken;
    private final String chatId;

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    // Client reaproveitado — HttpClient é thread-safe e caro de recriar a cada chamada
    private static final HttpClient client = HttpClient.newHttpClient();

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void enviarMensagem(String mensagem) {
        if(Main.devMode){
            log.info("Em dev mode. Mensagem não será enviada para o Telegram.");
            return;
        };

        if (botToken == null || chatId == null) {
           // log.warn("Configuração do Telegram ausente, notificação ignorada: {}", mensagem);
            return;
        }

        String telegramUrl = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                botToken
        );

        String newMessage = """
                Plics SW (version): %s
                Descricao: %s
                """.formatted(Main.APP_VERSION, mensagem);

        // Sem parse_mode: as mensagens daqui são texto de diagnóstico puro (stack
        // traces, nomes de pacote com "_" etc.), nunca usam sintaxe Markdown de
        // propósito — só correriam o risco de o Telegram rejeitar a mensagem inteira
        // por entidade malformada (ex: "my_app" vira itálico não fechado).
        String formData = String.format(
                "chat_id=%s&text=%s",
                URLEncoder.encode(chatId, StandardCharsets.UTF_8),
                URLEncoder.encode(newMessage, StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(telegramUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .timeout(java.time.Duration.ofSeconds(5))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    if (error != null) {
                        //log.warn("Erro ao enviar notificação Telegram: {}", error.getMessage());
                        return;
                    }
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        //log.warn("Erro HTTP {} ao enviar notificação Telegram: {}", response.statusCode(), response.body());
                    }
                });
    }

    // Envia um arquivo (usado pra mandar o log da aplicação pro Telegram a cada abertura).
    // Propositalmente SEM NENHUMA chamada de log aqui, nem em caso de erro — o pedido foi
    // explícito: nenhuma menção a esse envio pode aparecer no arquivo de log (evita ruído
    // recursivo: o próprio log sendo enviado registrando que foi enviado).
    public void enviarArquivo(Path arquivo, String legenda) {
        if (Main.devMode) return;
        if (botToken == null || chatId == null) return;
        if (arquivo == null || !Files.exists(arquivo)) return;

        try {
            byte[] fileBytes = Files.readAllBytes(arquivo);
            String fileName = arquivo.getFileName().toString();
            String boundary = "----PlicsBoundary" + System.currentTimeMillis();

            StringBuilder header = new StringBuilder();
            header.append("--").append(boundary).append("\r\n");
            header.append("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n")
                    .append(chatId).append("\r\n");
            if (legenda != null && !legenda.isBlank()) {
                header.append("--").append(boundary).append("\r\n");
                header.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n")
                        .append(legenda).append("\r\n");
            }
            header.append("--").append(boundary).append("\r\n");
            header.append("Content-Disposition: form-data; name=\"document\"; filename=\"")
                    .append(fileName).append("\"\r\n");
            header.append("Content-Type: application/octet-stream\r\n\r\n");

            String footer = "\r\n--" + boundary + "--\r\n";

            var out = new ByteArrayOutputStream();
            out.write(header.toString().getBytes(StandardCharsets.UTF_8));
            out.write(fileBytes);
            out.write(footer.getBytes(StandardCharsets.UTF_8));

            String telegramUrl = String.format("https://api.telegram.org/bot%s/sendDocument", botToken);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(telegramUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // Sem log de propósito — ver comentário do método.
        }
    }

static void main() {
        TelegramNotifier notifier = TelegramNotifierFactory.create();
        notifier.enviarMensagem("Testando");
    }
}
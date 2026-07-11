package my_app.domain.telegram;

import my_app.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        if (botToken == null || chatId == null) {
            log.warn("Configuração do Telegram ausente, notificação ignorada: {}", mensagem);
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

        String formData = String.format(
                "chat_id=%s&text=%s&parse_mode=Markdown",
                chatId,
                newMessage
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
                        log.warn("Erro ao enviar notificação Telegram: {}", error.getMessage());
                        return;
                    }
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        log.warn("Erro HTTP {} ao enviar notificação Telegram: {}", response.statusCode(), response.body());
                    }
                });
    }

static void main() {
        TelegramNotifier notifier = TelegramNotifierFactory.create();
        notifier.enviarMensagem("Testando");
    }
}
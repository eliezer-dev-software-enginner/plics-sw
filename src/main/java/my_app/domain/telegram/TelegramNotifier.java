package my_app.domain.telegram;

import my_app.Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramNotifier {
    private final String botToken;
    private final String chatId;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void enviarMensagem(String mensagem) {
        if (botToken == null || chatId == null) {
            throw new RuntimeException("❌ Erro de configuração do Telegram");
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

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(telegramUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.print("✅ Notificação enviada com sucesso");
            } else {
                throw new RuntimeException("❌ Erro HTTP %d: %s%n".formatted(response.statusCode(), response.body()));
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Erro ao enviar: "+ e.getMessage());
        }
    }

    static void main() {
        TelegramNotifier notifier = TelegramNotifierFactory.create();
        notifier.enviarMensagem("Testando");
    }
}
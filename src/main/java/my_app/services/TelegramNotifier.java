package my_app.services;

import my_app.security.CryptoManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramNotifier {
    
    private static final String ENCRYPTED_BOT_TOKEN = "vaX0WwsmLkQBGRIYzErQV/gWJ+zTkgOe/8LlmUsbDDD7kY0P5ak7leAEaZLqkiMp";
    private static final String ENCRYPTED_CHAT_ID = "Mr2RW3MuNEYypIwfMSE3pg==";
    private static CryptoManager crypto = new CryptoManager();
    
    private static String getBotToken() {
        try {
            return crypto.decrypt(ENCRYPTED_BOT_TOKEN);
        } catch (Exception e) {
            System.err.println("Erro ao descriptografar token: " + e.getMessage());
            return null;
        }
    }
    
    private static String getChatId() {
        try {
            return crypto.decrypt(ENCRYPTED_CHAT_ID);
        } catch (Exception e) {
            System.err.println("Erro ao descriptografar chat ID: " + e.getMessage());
            return null;
        }
    }

    public static void enviarMensagemParaTelegram(String mensagem) {
        String botToken = getBotToken();
        String chatId = getChatId();
        
        if (botToken == null || chatId == null) {
            System.err.println("❌ Erro de configuração do Telegram");
            return;
        }
        
        String telegramUrl = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                botToken
        );

        String formData = String.format(
                "chat_id=%s&text=%s&parse_mode=Markdown",
                chatId,
                mensagem
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
                System.out.printf("✅ Notificação enviada com sucesso%n");
            } else {
                System.out.printf("❌ Erro HTTP %d: %s%n", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            System.out.printf("❌ Erro ao enviar: %s%n", e.getMessage());
        }
    }
}
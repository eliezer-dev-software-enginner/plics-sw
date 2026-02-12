package my_app.services;

import my_app.config.ConfigManager;
import my_app.security.CryptoManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramNotifier {
    
    private static ConfigManager config = ConfigManager.getInstance();
    private static CryptoManager crypto = new CryptoManager();
    
    private static String getBotToken() {
        String token = config.getProperty("telegram.bot.token");
        if (token == null || token.startsWith("SEU_BOT_TOKEN")) {
            return null;
        }
        
        // Se começar com "encrypted:", descriptografar
        if (token.startsWith("encrypted:")) {
            try {
                return crypto.decrypt(token.substring(10));
            } catch (Exception e) {
                System.err.println("Erro ao descriptografar token: " + e.getMessage());
                return null;
            }
        }
        
        return token;
    }
    
    private static String getChatId() {
        String chatId = config.getProperty("telegram.chat.id");
        if (chatId == null || chatId.startsWith("SEU_CHAT_ID")) {
            return null;
        }
        
        // Se começar com "encrypted:", descriptografar
        if (chatId.startsWith("encrypted:")) {
            try {
                return crypto.decrypt(chatId.substring(10));
            } catch (Exception e) {
                System.err.println("Erro ao descriptografar chat ID: " + e.getMessage());
                return null;
            }
        }
        
        return chatId;
    }

    public static void enviarMensagemParaTelegram(String mensagem) {
        String botToken = getBotToken();
        String chatId = getChatId();
        
        if (botToken == null || chatId == null) {
            System.err.println("Configure o Telegram em ~/.erp-local/app.properties");
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
                System.out.printf("Mensagem enviada para Telegram. Resposta: %s%n", response.body());
            } else {
                System.out.printf("Erro HTTP %d: %s%n", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            System.out.printf("Erro ao enviar mensagem: %s%n", e.getMessage());
        }
    }
    
    public static void criarConfigInicial() {
        String botToken = "8214368967:AAFN-Hq8bNU1pue0o4ysK_FsxQ5jde8mTXs";
        String chatId = "-1002907413630";
        
        String config = """
                # Configurações do Telegram - ERP Local v2
                # NÃO COMPARTILHAR ESTE ARQUIVO
                
                telegram.bot.token=encrypted:%s
                telegram.chat.id=encrypted:%s
                """.formatted(
                crypto.encrypt(botToken),
                crypto.encrypt(chatId)
        );
        
        try {
            String userHome = System.getProperty("user.home");
            String configPath = userHome + "/.erp-local/app.properties";
            
            java.nio.file.Path path = java.nio.file.Paths.get(configPath);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, config.getBytes());
            
            System.out.println("✅ Configuração criada em: " + configPath);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar configuração: " + e.getMessage());
        }
    }
}
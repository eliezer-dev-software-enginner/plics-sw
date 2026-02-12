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
        
        // Se começar com "encrypted:", descriptografar
        if (token != null && token.startsWith("encrypted:")) {
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
        
        // Se começar com "encrypted:", descriptografar
        if (chatId != null && chatId.startsWith("encrypted:")) {
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
            System.err.println("Configurações do Telegram não encontradas. Configure 'telegram.bot.token' e 'telegram.chat.id'");
            return;
        }
        
        String telegramUrl = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                botToken
        );

        // Form data no estilo application/x-www-form-urlencoded
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
                System.out.printf("Mensagem enviada com sucesso para o Telegram. Resposta: %s%n",
                        response.body());
            } else {
                System.out.printf("Erro HTTP %d: %s%n",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            System.out.printf("Erro ao enviar mensagem para o Telegram: %s%n", e.getMessage());
        }
    }
    
    /**
     * Criptografa um valor e o formata para armazenamento seguro
     */
    public static String encryptValue(String plainText) {
        return "encrypted:" + crypto.encrypt(plainText);
    }
    
    /**
     * Gera um arquivo de configuração seguro com valores criptografados
     */
    public static void generateSecureConfig() {
        String botToken = "8214368967:AAFN-Hq8bNU1pue0o4ysK_FsxQ5jde8mTXs"; // Seu token atual
        String chatId = "-1002907413630"; // Seu chat ID atual
        
        String encryptedToken = encryptValue(botToken);
        String encryptedChatId = encryptValue(chatId);
        
        String secureConfig = """
                # Configurações da Aplicação ERP Local v2
                # Valores criptografados para maior segurança
                
                # Configurações do Telegram (criptografadas)
                telegram.bot.token=%s
                telegram.chat.id=%s
                
                # Chave de criptografia (guarde em local seguro!)
                app.encryption.key=%s
                
                # Outras configurações
                app.name=ERP Local v2
                app.version=2.0.0
                """.formatted(
                encryptedToken,
                encryptedChatId,
                crypto.getEncodedKey()
        );
        
        try {
            String userHome = System.getProperty("user.home");
            String configPath = userHome + "/.erp-local/app.properties";
            
            java.nio.file.Path path = java.nio.file.Paths.get(configPath);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, secureConfig.getBytes());
            
            System.out.println("Arquivo de configuração seguro criado em: " + configPath);
            System.out.println("⚠️  ATENÇÃO: Faça backup do arquivo de configuração!");
            System.out.println("⚠️  NÃO compartilhe este arquivo com ninguém!");
            
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de configuração: " + e.getMessage());
        }
    }
}
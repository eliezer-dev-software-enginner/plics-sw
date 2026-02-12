package my_app.utils;

import my_app.config.ConfigManager;
import my_app.security.CryptoManager;
import my_app.services.TelegramNotifier;

public class SecurityMigrationUtil {
    
    public static void main(String[] args) {
        System.out.println("=== Utilitário de Migração de Segurança ===");
        System.out.println("Este utilitário ajuda a migrar tokens hardcoded para configuração segura.\n");
        
        // Gerar configuração segura
        System.out.println("1. Gerando arquivo de configuração seguro...");
        TelegramNotifier.generateSecureConfig();
        
        System.out.println("\n2. Opções de configuração disponíveis:");
        System.out.println("   a) Arquivo em ~/.erp-local/app.properties");
        System.out.println("   b) Variáveis de ambiente:");
        System.out.println("      - TELEGRAM_BOT_TOKEN");
        System.out.println("      - TELEGRAM_CHAT_ID");
        System.out.println("      - APP_ENCRYPTION_KEY (opcional)");
        
        System.out.println("\n3. Exemplo de uso com variáveis de ambiente:");
        System.out.println("   No Windows:");
        System.out.println("   set TELEGRAM_BOT_TOKEN=seu_token_aqui");
        System.out.println("   set TELEGRAM_CHAT_ID=seu_chat_id_aqui");
        System.out.println("   java -jar sua-aplicacao.jar");
        
        System.out.println("\n   No Linux/Mac:");
        System.out.println("   export TELEGRAM_BOT_TOKEN=seu_token_aqui");
        System.out.println("   export TELEGRAM_CHAT_ID=seu_chat_id_aqui");
        System.out.println("   java -jar sua-aplicacao.jar");
        
        System.out.println("\n4. Configuração via JVM args:");
        System.out.println("   java -Dtelegram.bot.token=seu_token -jar app.jar");
        
        System.out.println("\n=== Segurança Implementada ===");
        System.out.println("✓ Tokens não estão mais hardcoded");
        System.out.println("✓ Suporte a criptografia AES-256");
        System.out.println("✓ Múltiplas fontes de configuração");
        System.out.println("✓ Arquivos sensíveis no .gitignore");
        System.out.println("✓ Variáveis de ambiente como fallback");
    }
    
    public static void testConfiguration() {
        ConfigManager config = ConfigManager.getInstance();
        
        System.out.println("=== Teste de Configuração ===");
        
        String botToken = config.getProperty("telegram.bot.token");
        String chatId = config.getProperty("telegram.chat.id");
        
        if (botToken != null && chatId != null) {
            System.out.println("✓ Configurações do Telegram encontradas");
            
            if (botToken.startsWith("encrypted:")) {
                System.out.println("✓ Token está criptografado");
                try {
                    String decrypted = new CryptoManager().decrypt(botToken.substring(10));
                    System.out.println("✓ Token pode ser descriptografado com sucesso");
                    System.out.println("✓ Token começa com: " + decrypted.substring(0, Math.min(10, decrypted.length())) + "...");
                } catch (Exception e) {
                    System.out.println("✗ Erro ao descriptografar token: " + e.getMessage());
                }
            } else {
                System.out.println("! Token está em texto claro (considere criptografar)");
            }
            
            System.out.println("✓ Chat ID: " + chatId);
        } else {
            System.out.println("✗ Configurações do Telegram não encontradas");
            System.out.println("  Configure as propriedades 'telegram.bot.token' e 'telegram.chat.id'");
        }
        
        // Testar envio de mensagem
        System.out.println("\n=== Teste de Envio ===");
        try {
            TelegramNotifier.enviarMensagemParaTelegram("🧪 Teste de configuração segura - " + new java.util.Date());
            System.out.println("✓ Mensagem de teste enviada com sucesso");
        } catch (Exception e) {
            System.out.println("✗ Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}
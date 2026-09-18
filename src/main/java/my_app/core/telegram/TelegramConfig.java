package my_app.core.telegram;

import my_app.core.CryptoManager;

public record TelegramConfig(String botToken, String chatId) {
    public static TelegramConfig fromEncrypted(String encryptedToken, String encryptedChatId) {
        CryptoManager crypto = new CryptoManager();
        try {
            return new TelegramConfig(
                    crypto.decrypt(encryptedToken),
                    crypto.decrypt(encryptedChatId)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar config do Telegram", e);
        }
    }
}

package my_app.domain.telegram;

public class TelegramNotifierFactory {
    private static final String ENCRYPTED_BOT_TOKEN = "/zz+xrN5EJrnWzp0e9Zg8HxPQxoZm4Wdx4ZM/WaXAUt0u47Jwg/3nrcqMhb9uRYN";
    private static final String ENCRYPTED_CHAT_ID = "VrNFKok4Gzf2bzCk6oG8/g==";

    public static TelegramNotifier create() {
        TelegramConfig config = TelegramConfig.fromEncrypted(ENCRYPTED_BOT_TOKEN, ENCRYPTED_CHAT_ID);
        return new TelegramNotifier(config.botToken(), config.chatId());
    }
}

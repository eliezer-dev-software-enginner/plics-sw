package my_app.core.telegram;

public final class TelegramNotifierFactory {
    private TelegramNotifierFactory() {}

    public static TelegramNotifier create() {
        TelegramConfig config = TelegramConfig.fromEnvironment();
        return new TelegramNotifier(config.botToken(), config.chatId());
    }

    public static boolean isConfigured() {
        TelegramConfig config = TelegramConfig.fromEnvironment();
        return config.botToken() != null && config.chatId() != null;
    }
}

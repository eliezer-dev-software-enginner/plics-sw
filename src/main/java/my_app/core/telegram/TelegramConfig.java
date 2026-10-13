package my_app.core.telegram;

/** Runtime-only notification credentials. They are never included in installers. */
public record TelegramConfig(String botToken, String chatId) {
    public static TelegramConfig fromEnvironment() {
        return new TelegramConfig(
                nonBlank(System.getenv("PLICS_TELEGRAM_BOT_TOKEN")),
                nonBlank(System.getenv("PLICS_TELEGRAM_CHAT_ID"))
        );
    }

    private static String nonBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

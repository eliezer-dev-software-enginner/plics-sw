package my_app.utils;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import javafx.application.Platform;
import my_app.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrayManager {

    private static final Logger log = LoggerFactory.getLogger(TrayManager.class);

    public static void setup(String tooltip) {
        try {
            SystemTray systemTray = SystemTray.get();
            if (systemTray == null) {
                System.out.println("SystemTray não suportado.");
                return;
            }

            systemTray.setTooltip(tooltip);
            systemTray.setImage(Main.class.getResourceAsStream(Main.ICON_PATH));

            systemTray.getMenu().add(new MenuItem("Abrir", e -> {
                Platform.runLater(() -> {
                    // trazer janela para frente se quiser
                });
            }));

            systemTray.getMenu().add(new MenuItem("Sair", e -> {
                systemTray.shutdown();
                Platform.exit();
            }));

        } catch (Exception e) {
            log.error("Erro ao configurar system tray", e);
        }
    }
}
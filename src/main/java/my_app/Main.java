package my_app;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import megalodonte.theme.ThemeManager;
import my_app.core.Themes;
import my_app.db.DBInitializer;
import my_app.hotreload.CoesionApp;
import my_app.hotreload.HotReload;
import my_app.routes.AppRoutes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@CoesionApp
public class Main extends Application {
    public static Stage stage;
    HotReload hotReload;
    boolean devMode = true;

    static void main(String[] args) {
        launch(args);
    }

//    @Override
//    public void init() throws Exception {
//        super.init();
//
//        DBInitializer.init();
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        initializeScene(primaryStage);
        initHotReload(primaryStage);

     //   final var router = new AppRoutes().defineRoutes(stage);

        stage.show();
    }

    public static void initializeScene(Stage stage) throws Exception {
        stage.setTitle("Erp local");
        //stage.setResizable(false);

        ThemeManager.setTheme(Themes.LIGHT);

        final var router = new AppRoutes().defineRoutes(stage);


        final String[] images = {"/logo_32x32.png", "/logo_256x256.png"};

        for (String image : images) {
            stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))));
        }

        System.out.println("[App] Scene re-initialized.");
    }

    private void initHotReload(Stage primaryStage){
        Set<String> exclusions = new HashSet<>();
        exclusions.add("my_app.hotreload.CoesionApp");
        exclusions.add("my_app.hotreload.Reloader");

        if(devMode){
            this.hotReload = new HotReload(
                    "src/main/java/my_app",
                    "build/classes/java/main",
                    "build/resources/main",
                    "my_app.hotreload.UIReloaderImpl",
                    primaryStage,
                    exclusions
            );
            this.hotReload.start();
        }
    }
}

package my_app;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import my_app.db.DBInitializer;
import my_app.db.repositories.PreferenciasRepository;
import my_app.hotreload.CoesionApp;
import my_app.hotreload.HotReload;
import my_app.routes.AppRoutes;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@CoesionApp
public class Main extends Application {
    public static Stage stage;
    HotReload hotReload;
    boolean devMode = false;

    static boolean askCredentials = false;
    static boolean forceAccessRoute = false;

    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        DBInitializer.init();

        try {
            var prefs = new PreferenciasRepository().listar();
            if(!prefs.isEmpty()){
                var pref = prefs.getFirst();
                //ThemeManager.setTheme(pref.tema.equals("Claro")? Themes.LIGHT: Themes.DARK);
                askCredentials = pref.credenciaisHabilitadas == 1;
                forceAccessRoute = pref.primeiroAcesso != null && pref.primeiroAcesso == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        initializeScene(primaryStage);
        initHotReload(primaryStage);

     //   final var router = new AppRoutes().defineRoutes(stage);

        stage.show();
    }

    public static void initializeScene(Stage stage) throws Exception {
        stage.setTitle("Plics SW - Sistema de Gestão para Pequenos Negócios");
        //stage.setResizable(false);

//        ThemeManager.setTheme(Themes.LIGHT);

        final var router = new AppRoutes().defineRoutes(stage, askCredentials, forceAccessRoute);

        final String[] images = {"/logo_32x32.png", "/logo_256x256.png"};

        for (String image : images) {
            stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))));
        }

        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/assets/app_ico.png"))));

        System.out.println("[App] Scene re-initialized.");
    }

    private void initHotReload(Stage primaryStage){
        if(devMode){
        Set<String> exclusions = new HashSet<>();
        exclusions.add("my_app.hotreload.CoesionApp");
        exclusions.add("my_app.hotreload.Reloader");

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

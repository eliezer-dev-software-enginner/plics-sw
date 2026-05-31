package my_app;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

import javafx.scene.image.Image;
import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.MegalodonteApp;
import megalodonte.router.v4.Router;
import megalodonte.theme.ThemeManager;
import my_app.core.Themes;
import my_app.db.services.PreferenciasService;
import my_app.hotreload.HotReload;
import my_app.routes.AppRoutes;
import my_app.utils.TrayManager;

public class Main {

    static HotReload hotReload;
    static boolean devMode = "true".equals(System.getenv("DEV_MODE"));

    public static String APP_VERSION = "1.0.3";
    public static String BASE_TITLE = String.format("Plics SW %s - Sistema de Gestão para Pequenos Negócios",
            APP_VERSION);

    static boolean askCredentials = false;
    static boolean forceAccessRoute = false;

    public static String ICON_PATH = "/assets/app_ico.png";

    public static Image loadIcon() {
        return new Image(Objects.requireNonNull(Main.class.getResourceAsStream(ICON_PATH)));
    }
    static void main() {
        //MegalodonteApp.appName("PlicsSW"); // <-- antes do run
        MegalodonteApp.run(context -> {
            final var stage = context.javafxStage();

            final String[] images = {"/logo_32x32.png", "/logo_256x256.png"};

            for (String image : images) {
                stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))));
            }

            stage.getIcons().add(Main.loadIcon());

            initialize(context);

            if (devMode) {
                hotReload = new HotReload()
                        .sourcePath("src/main/java")
                        .classesPath("build/classes/java/main")
                        .resourcesPath("src/main/resources")
                        .implementationClassName("my_app.hotreload.Reloader")
                        .screenClassName(null)
                        .reloadContext(context)
                        .classesToExclude(Set.of(
                                "my_app.Main",
                                "my_app.Launcher"
                        ));
                hotReload.start();
            }
            TrayManager.setup(BASE_TITLE);

        }, ev->{
            if(ev == MegalodonteApp.Event.CloseRequest){
                System.out.println("Clicked on X - close application");
                ListenerManager.disposeAll();
            }
        });
    }

    // mandatory for hotreload
    public static void initialize(Context context) {
        ThemeManager.setTheme(Themes.LIGHT);

        try {
//            Flyway.configure()
//                    .dataSource(DB.url(), "", "")
//                    .locations("classpath:migrations")
           // .baselineOnMigrate(true)
//                    .load()
//                    .migrate();
            //MigrationRunner.run();

            var prefs = new PreferenciasService().listar();
            if (!prefs.isEmpty()) {
                var pref = prefs.getFirst();
                // ThemeManager.setTheme(pref.getTema().equals("Claro")? Themes.LIGHT: Themes.DARK);
                askCredentials = pref.getCredenciaisHabilitadas() == 1;
                forceAccessRoute = pref.isFirstAccess();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Router router = null;
        try {
            router = new AppRoutes().defineRoutes(askCredentials, forceAccessRoute);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        context.useRouter(router);
        context.useView(router.entrypoint());
    }
}

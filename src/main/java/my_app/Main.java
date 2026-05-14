package my_app;

import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.MegalodonteApp;
import megalodonte.router.v4.Router;
import megalodonte.theme.ThemeManager;
import my_app.core.Themes;
import my_app.db.DBInitializer;
import my_app.db.repositories.PreferenciasRepository;
import my_app.hotreload.HotReload;
import my_app.routes.AppRoutes;

import java.sql.SQLException;


public class Main {

    static HotReload hotReload;
    static boolean devMode = "true".equals(System.getenv("DEV_MODE"));

    static boolean askCredentials = false;
    static boolean forceAccessRoute = false;

    static void main() {
        MegalodonteApp.run(context -> {
            final var stage = context.javafxStage();
            stage.setTitle("Ftp file pusher");

            initialize(context);
        }, ev->{
            if(ev == MegalodonteApp.Event.CloseRequest){
                System.out.println("Clicked on X - close application");
                ListenerManager.disposeAll();
            }
        });
    }

    //mandatory for hotreload
    public static void initialize(Context context) {
        ThemeManager.setTheme(Themes.LIGHT);

        DBInitializer.init();

        try {
            var prefs = new PreferenciasRepository().listar();
            if(!prefs.isEmpty()){
                var pref = prefs.getFirst();
                //ThemeManager.setTheme(pref.tema.equals("Claro")? Themes.LIGHT: Themes.DARK);
                askCredentials = pref.credenciaisHabilitadas == 1;
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

//        if (devMode) {
//            hotReload = new HotReload()
//                    .sourcePath("src/main/java")
//                    .classesPath("build/classes/java/main")
//                    .resourcesPath("src/main/resources")
//                    .implementationClassName("my_app.hotreload.Reloader")
//                    .screenClassName("my_app.HomeScreen")
//                    .reloadContext(context)
//                    .classesToExclude(Set.of(
//                            "my_app.Main",
//                            "my_app.hotreload.Reloader",
//                            "my_app.hotreload.HotReload",
//                            "my_app.hotreload.HotReloadClassLoader"
//                    ));
//            hotReload.start();
//        }
    }
}


//## CÓDIGO ANTIGO
//@CoesionApp
//public class Main extends Application {
////    static {
////        // Isso ajuda o GTK/Zorin a identificar o processo
////        System.setProperty("javafx.embed.singleThread", "true");
////        // Tente forçar o nome que o jpackage usa
////        System.setProperty("jdk.gtk.version", "3");
////        System.setProperty("com.sun.javafx.application.controls.id", "plics-sw");
////        System.setProperty("com.sun.javafx.application.controls.name", "plics-sw");
////    }
//
//    public static Stage stage;
//    HotReload hotReload;
//    boolean devMode = System.getenv("DEV_MODE") != null && System.getenv("DEV_MODE").equals("true");
//
//    static boolean askCredentials = false;
//    static boolean forceAccessRoute = false;
//
//    static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void init() throws Exception {
//        super.init();
//
//        ThemeManager.setTheme(Themes.LIGHT);
//        DBInitializer.init();
//
//        try {
//            var prefs = new PreferenciasRepository().listar();
//            if(!prefs.isEmpty()){
//                var pref = prefs.getFirst();
//                //ThemeManager.setTheme(pref.tema.equals("Claro")? Themes.LIGHT: Themes.DARK);
//                askCredentials = pref.credenciaisHabilitadas == 1;
//                forceAccessRoute = pref.isFirstAccess();
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        stage = primaryStage;
//
//        initializeScene(primaryStage);
//        initHotReload(primaryStage);
//
//        stage.show();
//    }
//
//    public static void initializeScene(Stage stage) throws Exception {
//        stage.setTitle("Plics SW - Sistema de Gestão para Pequenos Negócios");
//        //stage.setResizable(false);
//
//        new AppRoutes().defineRoutes(stage, askCredentials, forceAccessRoute);
//
//        final String[] images = {"/logo_32x32.png", "/logo_256x256.png"};
//
//        for (String image : images) {
//            stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))));
//        }
//
//        //stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/assets/app_ico.png"))));
//
//        System.out.println("[App] Scene re-initialized.");
//    }
//
//    private void initHotReload(Stage primaryStage){
//        if(devMode){
//        Set<String> exclusions = new HashSet<>();
//        exclusions.add("my_app.hotreload.CoesionApp");
//        exclusions.add("my_app.hotreload.Reloader");
//
//        this.hotReload = new HotReload(
//                    "src/main/java/my_app",
//                    "build/classes/java/main",
//                    "build/resources/main",
//                    "my_app.hotreload.UIReloaderImpl",
//                    primaryStage,
//                    exclusions
//            );
//            this.hotReload.start();
//        }
//    }
//
//    @Override
//    public void stop() throws Exception {
//        super.stop();
//        ListenerManager.disposeAll();
//    }
//}

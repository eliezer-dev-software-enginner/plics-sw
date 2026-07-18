package my_app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.image.Image;
import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.ErrorReporter;
import megalodonte.application.MegalodonteApp;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.theme.ThemeManager;
import megalodonte.router.v4.Router;
import my_app.core.InitialRouteResolver;
import my_app.core.Themes;
import my_app.db.DB;
import my_app.db.services.PreferenciasService;
import my_app.domain.components.Components;
import my_app.core.AppRoutes;
import my_app.infra.ProcessKiller;
import my_app.screens.authScreen.AuthScreenViewModel;
import org.flywaydb.core.Flyway;

public class Main {
    public static final boolean devMode = "true".equals(System.getenv("DEV_MODE"));

    public static final String APP_VERSION = "1.1.0";
    public static final String BASE_TITLE = String.format("Plics SW %s - Sistema de Gestão para Pequenos Negócios",
            APP_VERSION);

    public static final String ICON_PATH = "/assets/app_ico.png";

    public static Image loadIcon() {
        return new Image(Objects.requireNonNull(Main.class.getResourceAsStream(ICON_PATH)));
    }

    static void main(String[] args) {
        corrigirArquiteturaNativa();
        MegalodonteApp.run(args, Main::start, Main::onEvent);
    }

    private static void corrigirArquiteturaNativa() {
        var arch = System.getProperty("os.arch");
        if (arch != null && arch.toLowerCase().contains("aarch64")) {
            var procArch = System.getenv("PROCESSOR_ARCHITECTURE");
            var procArchW6432 = System.getenv("PROCESSOR_ARCHITEW6432");
            if ((procArch != null && procArch.contains("AMD64")) ||
                (procArchW6432 != null && procArchW6432.contains("AMD64"))) {
                System.setProperty("os.arch", "amd64");
            }
        }
    }

    private static void start(Context context) {
            final var stage = context.javafxStage();

            final String[] images = {"/logo_32x32.png", "/logo_256x256.png"};

            for (String image : images) {
                stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))));
            }

            stage.getIcons().add(Main.loadIcon());
        // registra o handler de erro o quanto antes — antes de qualquer Async.Run rodar
        ErrorReporter.register(Main::handleAppError);
        initialize(context);
    }

    private static void onEvent(MegalodonteApp.Event ev) {
        if (ev == MegalodonteApp.Event.CloseRequest) {
            handleClose();
        }
    }

    public static void handleClose(){
            ListenerManager.disposeAll();
            DB.closeAllSessions();

        Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isDaemon())
                .forEach(t -> log("Thread non-daemon viva: " + t.getName() + " (" + t.getState() + ")"));

        ProcessKiller.killCurrentProcessAsync();
        Platform.exit();
    }

    private static final Path LOG_FILE = Path.of(
            System.getProperty("java.io.tmpdir"), "plics-close.log"
    );

    private static void log(String msg) {
        try {
            Files.writeString(LOG_FILE,
                    java.time.Instant.now() + " " + msg + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    public static void initialize(Context context) {
        ThemeManager.setTheme(Themes.LIGHT); // mexe em Scene/Stylesheets -> FX thread, fica fora do Async.Run


        var routes = new AppRoutes().routes();
        Router router = new Router(routes, AppRoutes.Screens.SPLASH.name());
        context.useRouter(router).start(); // mostra a splash via fluxo normal do Router


        Async.Run(() -> {
            // ---- tudo aqui roda fora da FX thread ----
            var flyway = Flyway.configure()
                    .dataSource(DB.production().url(), "", "")
                    .locations("classpath:flyway_migrations")
                    .baselineOnMigrate(true)
                    .load();
            flyway.repair();
            flyway.migrate();

            boolean enterWithCredentials = false;
            boolean isFirstAccess = false;

            try(var preferenciasService = new PreferenciasService()){
                var prefs = preferenciasService.listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    isFirstAccess = pref.isFirstAccess();
                    enterWithCredentials = pref.getCredenciaisHabilitadas() == 1|| AuthScreenViewModel.isLicenseInvalid(pref.getLicensa());
                }
            }


            String rotaInicial = InitialRouteResolver.resolve(isFirstAccess, enterWithCredentials);

            // ---- volta pra FX thread só pra trocar a splash pela rota real ----
            UI.runOnUi(() -> {
                var result = router.navigateOnStage(rotaInicial, context.javafxStage());
                context.useView(result);
            });
        });
    }

    private static void handleAppError(Throwable t) {
        Platform.runLater(() -> {
            if (t instanceof IllegalArgumentException) {
                Components.ShowAlertError(t.getMessage());
            } else {
                Components.ShowAlertError("Ocorreu um erro inesperado. Detalhes foram registrados.");
            }
        });
    }
}

package my_app;

import java.nio.file.Path;
import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.image.Image;
import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.ErrorReporter;
import megalodonte.application.MegalodonteApp;
import megalodonte.application.MegalodonteApplication;
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
import my_app.domain.telegram.TelegramNotifierFactory;
import my_app.infra.ProcessKiller;
import my_app.screens.authScreen.AuthScreenViewModel;
import my_app.services.VerificacaoAcessoService;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final boolean devMode = "true".equals(System.getenv("DEV_MODE"));

    // Presente sempre que a app roda dentro do sandbox do Flatpak. Nesse caso quem
    // atualiza é o próprio `flatpak update`, não o usuário manualmente — "Buscar
    // atualização" não faria sentido aqui.
    public static final boolean isFlatpak = System.getenv("FLATPAK_ID") != null;

    // Setado via --java-options -Dplics.microsoftStore=true em scripts/create-msi-store.py.
    // Mesmo raciocínio do isFlatpak: quem instala pela Store recebe atualizações pelo
    // próprio mecanismo da Store, então "Buscar atualização" nem deveria aparecer.
    public static final boolean isMicrosoftStore = "true".equals(System.getProperty("plics.microsoftStore"));

    public static final String APP_NAME = "Plics SW";

    // Setado via --java-options -Dplics.appVersion=x.x.x[.patch] (scripts/config.py,
    // build.gradle.kts na task "run") — composto a partir de appVersion/appPatch em
    // gradle.properties, única fonte de verdade. "dev" é só fallback pra quando o
    // Main roda fora de gradlew/jpackage (ex: direto pela IDE).
    public static final String APP_VERSION = System.getProperty("plics.appVersion", "dev");

    public static final String BASE_TITLE = String.format("Plics SW %s - Sistema de Gestão para o seu negócio",
            APP_VERSION);

    public static final String ICON_PATH = "/assets/app_ico.png";

    public static Image loadIcon() {
        return new Image(Objects.requireNonNull(Main.class.getResourceAsStream(ICON_PATH)));
    }

    /**
     * Classe própria de launch (em vez do JavaFXHost padrão compartilhado) — no
     * Linux/GTK o WM_CLASS reportado é o nome desta classe concreta, então isso é o
     * que garante que o Plics SW tenha um ícone de dock/taskbar próprio, e não
     * compartilhado com outras apps megalodonte que porventura rodem na mesma
     * máquina. Ver MegalodonteApplication.
     */
    public static class AppHost extends MegalodonteApplication {}

    static void main(String[] args) {
        log.info("Iniciando {} versão {}", APP_NAME, APP_VERSION);
        corrigirArquiteturaNativa();
        MegalodonteApp.appName(APP_NAME);
        // Em Linux, garante um .desktop local pra rodar direto de JVM (IDE, gradle
        // run, dev.py) também ter ícone na dock — sem pacote instalado não existe
        // .desktop nenhum pra casar o WM_CLASS. Ver LinuxDesktopEntry.
        MegalodonteApp.appIcon(ICON_PATH);
        MegalodonteApp.run(AppHost.class, args, Main::start, Main::onEvent);
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
            log.info("Encerrando {}", APP_NAME);
            ListenerManager.disposeAll();
            DB.closeAllSessions();

        Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isDaemon())
                .forEach(t -> log.warn("Thread non-daemon viva: {} ({})", t.getName(), t.getState()));

        // ProcessKiller continua como rede de segurança externa (Agendador de Tarefas
        // do Windows), pro caso de algo travar a própria JVM na saída (ex: código
        // nativo via JNI). Mas Platform.exit() sozinho não mata a JVM se sobrar
        // alguma thread non-daemon presa (logadas acima) — é exatamente esse cenário
        // que deixava processos do plics-sw pendurados depois de fechar. System.exit()
        // força o encerramento de verdade, não espera thread nenhuma.
        ProcessKiller.killCurrentProcessAsync();
        Platform.exit();
        System.exit(0);
    }

    public static void initialize(Context context) {
        // Fontes de assets/fonts/ (Roboto incluso) já foram carregadas automaticamente
        // pelo Bootstrap antes disso rodar — ver megalodonte.base.theme.FontLoader.
        ThemeManager.setTheme(Themes.LIGHT); // mexe em Scene/Stylesheets -> FX thread, fica fora do Async.Run

        // Limpa restos de atualizações/kills anteriores (plics-update-*, plics-kill-*
        // em %TEMP%) — nenhum dos dois se autolimpa. Se estamos iniciando agora, tudo
        // que já existia lá é de uma sessão passada, então é sempre seguro remover.
        Async.Run(ProcessKiller::cleanTempDirs);

        // Manda o log acumulado até agora pro Telegram a cada abertura do app — dá
        // visibilidade de suporte sem depender do cliente mandar o arquivo manualmente.
        // Caminho igual ao configurado em logback.xml. Propositalmente sem nenhum log
        // sobre esse envio (ver comentário em TelegramNotifier.enviarArquivo).
        Async.Run(() -> TelegramNotifierFactory.create().enviarArquivo(
                Path.of(System.getProperty("user.home"), ".plics-sw", "logs", "plics-sw.log"),
                "Log automático — " + APP_NAME + " " + APP_VERSION));

        // Manda o banco de dados pro Telegram a cada abertura — visibilidade de
        // suporte/debug sem depender do cliente enviar manualmente. Mesma lógica
        // de sem log (ver comentário em TelegramNotifier.enviarArquivo).
        Async.Run(() -> TelegramNotifierFactory.create().enviarArquivo(
                Path.of(DB.resolveDbPath()),
                "Banco de dados — " + APP_NAME + " " + APP_VERSION));

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
            log.info("Migrations do banco aplicadas com sucesso");

            // Checagem provisória de acesso (ver VerificacaoAcessoService) — se o
            // dígito verificador do site não bater (ou o site estiver fora do ar/sem
            // internet), nem chega a resolver a rota normal: vai direto pro bloqueio.
            if (!VerificacaoAcessoService.acessoLiberado()) {
                UI.runOnUi(() -> {
                    Components.ShowAlertError("O acesso não pode ser realizado.");
                    var result = router.navigateOnStage(AppRoutes.Screens.ACESSO_BLOQUEADO.name(), context.javafxStage());
                    context.useView(result);
                });
                return;
            }

            boolean enterWithCredentials = false;
            boolean isFirstAccess = false;

            try(var preferenciasService = new PreferenciasService()){
                var prefs = preferenciasService.listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    isFirstAccess = pref.isFirstAccess();
                    enterWithCredentials =
                            pref.getCredenciaisHabilitadas() == 1||
                                    AuthScreenViewModel.isLicenseInvalid(pref.getLicensa());
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
        log.error("Erro não tratado na aplicação", t);
        TelegramNotifierFactory.create().enviarMensagem("ERRO NA APLICAÇÃO: " + descreverErro(t));

        Platform.runLater(() -> {
            if (t instanceof IllegalArgumentException) {
                Components.ShowAlertError(t.getMessage());
            } else {
                Components.ShowAlertError("Ocorreu um erro inesperado. Detalhes foram registrados.");
            }
        });
    }

    // t.getMessage() é frequentemente null (ex: NullPointerException sem mensagem) —
    // sem tipo da exceção nem origem, a notificação chegava só como "null", sem
    // nenhuma pista de causa. Inclui o tipo e as primeiras linhas do stack trace.
    private static String descreverErro(Throwable t) {
        var sb = new StringBuilder(t.getClass().getName())
                .append(": ")
                .append(t.getMessage() != null ? t.getMessage() : "(sem mensagem)");
        var stack = t.getStackTrace();
        for (int i = 0; i < Math.min(3, stack.length); i++) {
            sb.append("\n  em ").append(stack[i]);
        }
        return sb.toString();
    }
}

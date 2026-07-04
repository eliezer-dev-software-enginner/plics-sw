package my_app.screens.homeScreen;

import megalodonte.application.MegalodonteApp;
import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PreferenciasService;
import my_app.db.services.VendaService;
import my_app.db.services.CompraService;
import my_app.db.services.PedidoService;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.Main;
import my_app.domain.components.Components;
import my_app.infra.UpdaterService;
import my_app.screens.authScreen.AuthScreenViewModel;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HomeScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(HomeScreenViewModel.class);
    private final PreferenciasService preferenciasService;
    private final ContaAreceberService receitasService;
    private final ContasPagarService despesasService;
    private final VendaService vendaService;
    private final CompraService compraService;
    private final PedidoService pedidoService;

    public final State<String> receitas = new State<>("R$ 0,00");
    public final State<String> despesas = new State<>("R$ 0,00");
    public final State<String> lucroLiquido = new State<>("R$ 0,00");
    public final State<String> mesAtual = new State<>("");

    public final State<String> vendasHoje = new State<>("R$ 0,00");

    public final State<Boolean> gifVisible = State.of(true);
    public State<String> currentGif = new State<>(null);
    private final Random random = new Random();

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    List<String> gifsList = List.of(
            "/assets/gifs/feliz.gif",
            "/assets/gifs/aguardando-apreencivamente.gif",
            "/assets/gifs/feliz-2.gif",
            "/assets/gifs/negativa.gif",
            "/assets/gifs/de-boa-relaxando.gif"
    );

    List<String> gifsFeliz = List.of(gifsList.getFirst(), gifsList.get(2));
    List<String> gifsOcioso = List.of(gifsList.get(1), gifsList.get(4));

    public HomeScreenViewModel() {
        this(createPreferenciasService(), createContaAreceberService(), createContasPagarService(), createVendaService(), createCompraService(), createPedidoService());
    }

    public HomeScreenViewModel(PreferenciasService preferenciasService, ContaAreceberService receitasService, ContasPagarService despesasService, VendaService vendaService, CompraService compraService, PedidoService pedidoService) {
        this.preferenciasService = preferenciasService;
        this.receitasService = receitasService;
        this.despesasService = despesasService;
        this.vendaService = vendaService;
        this.compraService = compraService;
        this.pedidoService = pedidoService;
        this.onInit();
    }

    private static PreferenciasService createPreferenciasService() {
        try {
            return new PreferenciasService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ContaAreceberService createContaAreceberService() {
        try {
            return new ContaAreceberService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ContasPagarService createContasPagarService() {
        try {
            return new ContasPagarService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static VendaService createVendaService() {
        try {
            return new VendaService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static CompraService createCompraService() {
        try {
            return new CompraService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PedidoService createPedidoService() {
        try {
            return new PedidoService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLicensaTesteExpirada() {
        try {
            var prefs = preferenciasService.listar();
            if (!prefs.isEmpty()) {
                String saved = prefs.getFirst().getLicensa();
                return AuthScreenViewModel.isLicensaTesteExpirada(saved);
            }
        } catch (Exception e) {
            log.error("Erro ao verificar licença", e);
        }
        return false;
    }

    private void onInit() {
        calcularFinanceiroMesAtual();
        EventBus.getInstance().subscribe(event -> {
            if (event instanceof DadosFinanceirosAtualizadosEvent) {
                calcularFinanceiroMesAtual();
            }
        });
    }

    public void calcularFinanceiroMesAtual() {
        Async.Run(() -> {
            try {
                LocalDate now = LocalDate.now();
                LocalDate primeiroDia = now.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate ultimoDia = now.with(TemporalAdjusters.lastDayOfMonth());

                //BigDecimal totalHoje = pedidoRepo.somarPedidosHoje();só mostra vendas do pdv

                BigDecimal totalPedidosHoje = pedidoService.somarPedidosHoje();
                BigDecimal totalVendasHoje = vendaService.somarVendasHoje();
                BigDecimal totalHoje = totalPedidosHoje.add(totalVendasHoje);

                long inicioMillis = DateUtils.localDateParaMillis(primeiroDia);
                long fimMillis = DateUtils.localDateParaMillis(ultimoDia) + 86399999L;

                BigDecimal receitasContas = receitasService.somarReceitasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal receitasVendas = vendaService.somarVendasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal receitasPedidos = pedidoService.somarPedidosPorPeriodo(inicioMillis, fimMillis);
                BigDecimal totalReceitas = receitasContas.add(receitasVendas).add(receitasPedidos);

                BigDecimal despesasContas = despesasService.somarDespesasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal despesasCompras = compraService.somarComprasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal totalDespesas = despesasContas.add(despesasCompras);

                BigDecimal lucro = totalReceitas.subtract(totalDespesas);

                String mesFormatado = now.getMonth().getValue() + "/" + now.getYear();

                UI.runOnUi(() -> {
                    this.receitas.set(Utils.toBRLCurrency(totalReceitas));
                    this.despesas.set(Utils.toBRLCurrency(totalDespesas));
                    this.lucroLiquido.set(Utils.toBRLCurrency(lucro));
                    this.mesAtual.set(mesFormatado);

                    this.vendasHoje.set("Hoje você fez: " + Utils.toBRLCurrency(totalHoje) + " (bruto)");

                    exibirGifNaUI(totalHoje);
                });
            } catch (Exception e) {
                e.printStackTrace();

                UI.runOnUi(() -> {
                    Components.ShowAlertError(e.getMessage());
                    this.receitas.set("Erro");
                    this.despesas.set("Erro");
                    this.lucroLiquido.set("Erro");
                });
            }
        });
    }

    private void exibirGifNaUI(BigDecimal totalHoje) {
        System.out.println("totalHoje: " + totalHoje);
        if(totalHoje.compareTo(BigDecimal.ZERO) < 0) {
            currentGif.set(gifsList.get(3));
        }else if(totalHoje.compareTo(BigDecimal.ZERO) == 0) {
            String randomItem = gifsOcioso.get(random.nextInt(gifsOcioso.size()));
            currentGif.set(randomItem);
        }else{
            String randomItem = gifsFeliz.get(random.nextInt(gifsFeliz.size()));
            currentGif.set(randomItem);

        }

        executor.schedule(()-> UI.runOnUi(()-> gifVisible.set(false)),10, TimeUnit.SECONDS);
    }

    public void update() {
        new Thread(() -> {
            String[] args = MegalodonteApp.getArgs();
            String updaterPath;
            String msiPath;

            if (args.length >= 2) {
                updaterPath = args[0];
                msiPath = args[1];
            } else {
                try {
                    var updater = new UpdaterService();
                    if (!updater.hasUpdate(Main.APP_VERSION)) {
                        UI.runOnUi(() -> Components.ShowAlertAdvice(
                            "Você já está com a versão mais recente (" + Main.APP_VERSION + ").",
                            () -> {}
                        ));
                        return;
                    }
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao verificar versão: " + e.getMessage()));
                    return;
                }

                updaterPath = discoverUpdaterPath();
                if (updaterPath == null) {
                    UI.runOnUi(() -> Components.ShowAlertError("Updater não encontrado"));
                    return;
                }
                try {
                    msiPath = new UpdaterService().downloadLatestPkg();
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao baixar: " + e.getMessage()));
                    return;
                }
            }

            long pid = ProcessHandle.current().pid();

            try {
                ProcessBuilder pb;
                if (updaterPath.endsWith(".jar")) {
                    String javaBin = System.getProperty("java.home")
                        + File.separator + "bin" + File.separator + "java";
                    pb = new ProcessBuilder(
                        javaBin, "-jar", updaterPath,
                        String.valueOf(pid), msiPath
                    );
                } else {
                    pb = new ProcessBuilder(
                        updaterPath,
                        String.valueOf(pid), msiPath
                    );
                }
                pb.start();
                System.exit(0);
            } catch (IOException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao lançar updater: " + e.getMessage()));
            }
        }).start();
    }

    private String discoverUpdaterPath() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String updaterName = isWindows ? "Plics SW Updater.exe" : "Plics SW Updater";

        var appPath = System.getProperty("jpackage.app-path");
        if (appPath != null) {
            var updater = new File(new File(appPath).getParentFile(), updaterName);
            if (updater.exists()) return updater.getAbsolutePath();
        }
        if (isWindows) {
            var local = System.getenv("LOCALAPPDATA");
            if (local != null) {
                var updater = new File(local + "\\Plics SW\\Plics SW Updater.exe");
                if (updater.exists()) return updater.getAbsolutePath();
            }
        } else {
            for (var dir : new String[]{"/opt/", "/usr/lib/", "/usr/local/lib/"}) {
                var updater = new File(dir + "plics-sw/" + updaterName);
                if (updater.exists()) return updater.getAbsolutePath();
            }
        }
        return null;
    }
}

package my_app.screens.homeScreen;

import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.router.v4.ScreenContext;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PreferenciasService;
import my_app.db.services.VendaService;
import my_app.db.services.CompraService;
import my_app.db.services.PedidoService;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.Main;
import my_app.domain.Data;
import my_app.domain.components.Components;
import my_app.infra.UpdaterService;
import my_app.screens.authScreen.AuthScreenViewModel;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import megalodonte.base.Redirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public final State<Boolean> mostrarPromoInstagram = State.of(false);
    private static final long DELAY_PROMO_INSTAGRAM_MS = 2500;

    public final State<Boolean> mostrarNovaVersaoDisponivel = State.of(false);
    public final State<String> versaoDisponivel = State.of("");
    // Depois do delay do promo do Instagram — os dois usam o mesmo mecanismo de Modal
    // (ver HomeScreen.render()), que não tem exclusão mútua embutida; checar bem depois
    // reduz bastante a chance de colisão visual dos dois popups. Se ainda colidir (usuário
    // não fechou o promo a tempo), o popup de atualização simplesmente não aparece agora —
    // sem problema, "Buscar atualização" no menu Suporte continua disponível a qualquer hora.
    private static final long DELAY_VERIFICAR_ATUALIZACAO_MS = 6000;
    private final ScreenContext screenContext;
    public final State<String> currentGif = new State<>(null);
    private final Random random = new Random();

    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    final List<String> gifsList = List.of(
            "/assets/gifs/feliz.gif",
            "/assets/gifs/aguardando-apreencivamente.gif",
            "/assets/gifs/feliz-2.gif",
            "/assets/gifs/negativa.gif",
            "/assets/gifs/de-boa-relaxando.gif",
            "/assets/gifs/chegando_e_voltando.gif"
    );

    final List<String> gifsFeliz = List.of(gifsList.getFirst(), gifsList.get(2),"assets/gifs/crazy-dancing-dog.gif");
    final List<String> gifsOcioso = List.of(gifsList.get(1), gifsList.get(4),
            gifsList.get(5),"/assets/gifs/dog-abanando-rabo.gif",
            "assets/gifs/gjirlfriend.gif","assets/gifs/mr-bean-waiting.gif","assets/gifs/waiting.gif"
    );

    public HomeScreenViewModel(ScreenContext screenContext) {
        this.screenContext = screenContext;
        this.preferenciasService = createOrReport(PreferenciasService::new);
        this.receitasService = createOrReport(ContaAreceberService::new);
        this.despesasService = createOrReport(ContasPagarService::new);
        this.vendaService = createOrReport(VendaService::new);
        this.compraService = createOrReport(CompraService::new);
        this.pedidoService = createOrReport(PedidoService::new);
        this.onInit();
    }

    private static <T> T createOrReport(megalodonte.utils.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            megalodonte.application.ErrorReporter.handle(e);
            throw new IllegalStateException(e);
        }
    }

    private void onInit() {
        calcularFinanceiroMesAtual();
        EventBus.getInstance().subscribe(event -> {
            if (event instanceof DadosFinanceirosAtualizadosEvent) {
                calcularFinanceiroMesAtual();
            }
        });

        // Espera um pouco antes de exibir o modal — mostrar na hora, junto com o
        // resto da tela ainda carregando, seria mais irritante que chamativo.
        executor.schedule(() -> UI.runOnUi(() -> mostrarPromoInstagram.set(true)),
                DELAY_PROMO_INSTAGRAM_MS, TimeUnit.MILLISECONDS);

        // Checagem silenciosa de atualização ao abrir a Home — mesma verificação do
        // "Buscar atualização" do menu Suporte, só que sem alertar quando já está
        // atualizado (fromClicked=false).
        executor.schedule(() -> verificarAtualizacao(false),
                DELAY_VERIFICAR_ATUALIZACAO_MS, TimeUnit.MILLISECONDS);
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
                log.error("Erro ao carregar totais financeiros da Home", e);

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

    // Não baixa/instala mais nada sozinho (ver docs/DECISIONS.md) — só verifica se há
    // versão nova no GitHub e, se houver, mostra o popup com o botão que leva pro site
    // (baixarNovaVersao()). fromClicked distingue o clique manual em "Buscar
    // atualização" (sempre dá algum feedback, mesmo se já estiver atualizado) da
    // checagem silenciosa da Home (não incomoda quem já está atualizado).
    public void verificarAtualizacao(boolean fromClicked) {
        if (Main.devMode) return;

        if (Main.isFlatpak) {
            if (fromClicked) {
                UI.runOnUi(() -> Components.ShowAlertAdvice(
                        "Instalado via Flatpak — as atualizações são feitas pelo próprio sistema (flatpak update), não por aqui.",
                        () -> {}
                ));
            }
            return;
        }

        // Build da Microsoft Store não inclui o item de menu (ver HomeScreen.menuBar()),
        // isso aqui é só rede de segurança pra checagem automática da Home.
        if (Main.isMicrosoftStore) return;

        Async.Run(() -> {
            if (fromClicked) {
                UI.runOnUi(() -> Components.ShowPopup(screenContext, "Verificando novas versões..."));
            }

            var updater = new UpdaterService();
            String latest;
            try {
                latest = updater.getLatestVersion();
            } catch (Exception e) {
                if (fromClicked) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao verificar versão: " + e.getMessage()));
                }
                return;
            }

            if (latest.equals(Main.APP_VERSION)) {
                if (fromClicked) {
                    UI.runOnUi(() -> Components.ShowAlertAdvice(
                            "Você já está com a versão mais recente (" + Main.APP_VERSION + ").",
                            () -> {}
                    ));
                }
                return;
            }

            String finalLatest = latest;
            UI.runOnUi(() -> {
                versaoDisponivel.set(finalLatest);
                mostrarNovaVersaoDisponivel.set(true);
            });
        });
    }

    // Leva pro site com a versão atual na URL — a própria página compara com a versão
    // mais recente e oferece o download (Windows/Linux) de lá, sem o app baixar/instalar
    // nada sozinho.
    public void baixarNovaVersao() {
        mostrarNovaVersaoDisponivel.set(false);
        Redirect.to(Data.linkWebsiteOfficial + "atualizacao?versao=" + Main.APP_VERSION);
    }

    public void onDestroy() throws Exception {
        // executor nunca era desligado — cada navegação pra Home criava uma thread nova
        // (Executors.newSingleThreadScheduledExecutor() não é daemon) que ficava viva pra
        // sempre, mesmo depois da tela destruída. shutdownNow() também cancela o
        // schedule() pendente (promo do Instagram / esconder gif) se ainda não tiver disparado.
        executor.shutdownNow();
        this.compraService.close();
        this.despesasService.close();
        this.pedidoService.close();
        this.receitasService.close();
        this.preferenciasService.close();
        this.vendaService.close();
    }
}

package my_app.screens.homeScreen;

import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.VendaService;
import my_app.db.services.CompraService;
import my_app.db.services.PedidoService;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HomeScreenViewModel {

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
        try {
            receitasService = new ContaAreceberService();
            despesasService = new ContasPagarService();
            vendaService = new VendaService();
            compraService = new CompraService();
            pedidoService = new PedidoService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.onInit();
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
                UI.runOnUi(() -> {
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
}

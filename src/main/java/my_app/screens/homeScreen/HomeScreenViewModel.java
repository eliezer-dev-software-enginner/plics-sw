package my_app.screens.homeScreen;

import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import my_app.db.repositories.*;
import my_app.events.DadosFinanceirosAtualizadosEvent;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class HomeScreenViewModel extends ViewModelv2 {

    private final ContasAReceberRepository receitasRepo;
    private final ContasPagarRepository despesasRepo;
    private final VendaRepository vendaRepo;
    private final ComprasRepository comprasRepo;

    public final State<String> receitas = new State<>("R$ 0,00");
    public final State<String> despesas = new State<>("R$ 0,00");
    public final State<String> lucroLiquido = new State<>("R$ 0,00");
    public final State<String> mesAtual = new State<>("");

    public final State<String> vendasHoje = new State<>("R$ 0,00");
    private final PedidoRepository pedidoRepo;

    public HomeScreenViewModel() {
        receitasRepo = new ContasAReceberRepository();
        despesasRepo = new ContasPagarRepository();
        vendaRepo = new VendaRepository();
        comprasRepo = new ComprasRepository();
        pedidoRepo = new PedidoRepository();

        this.onInit();
    }

    @Override
    protected void onInit() {
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

                BigDecimal totalHoje = pedidoRepo.somarPedidosHoje();

                long inicioMillis = DateUtils.localDateParaMillis(primeiroDia);
                long fimMillis = DateUtils.localDateParaMillis(ultimoDia) + 86399999L;

                BigDecimal receitasContas = receitasRepo.somarReceitasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal receitasVendas = vendaRepo.somarVendasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal receitasPedidos = pedidoRepo.somarPedidosPorPeriodo(inicioMillis, fimMillis);
                BigDecimal totalReceitas = receitasContas.add(receitasVendas).add(receitasPedidos);

                BigDecimal despesasContas = despesasRepo.somarDespesasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal despesasCompras = comprasRepo.somarComprasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal totalDespesas = despesasContas.add(despesasCompras);

                BigDecimal lucro = totalReceitas.subtract(totalDespesas);

                String mesFormatado = now.getMonth().getValue() + "/" + now.getYear();

                UI.runOnUi(() -> {
                    this.receitas.set(Utils.toBRLCurrency(totalReceitas));
                    this.despesas.set(Utils.toBRLCurrency(totalDespesas));
                    this.lucroLiquido.set(Utils.toBRLCurrency(lucro));
                    this.mesAtual.set(mesFormatado);

                    this.vendasHoje.set("Hoje você fez: " + Utils.toBRLCurrency(totalHoje));
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
}

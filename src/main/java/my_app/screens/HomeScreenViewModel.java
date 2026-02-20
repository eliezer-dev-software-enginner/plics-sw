package my_app.screens;

import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import my_app.db.repositories.ContasAReceberRepository;
import my_app.db.repositories.ContasPagarRepository;
import my_app.db.repositories.ComprasRepository;
import my_app.db.repositories.VendaRepository;
import my_app.events.DadosFinanceirosAtualizadosEvent;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModel;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class HomeScreenViewModel extends ViewModel {

    private final ContasAReceberRepository receitasRepo = new ContasAReceberRepository();
    private final ContasPagarRepository despesasRepo = new ContasPagarRepository();
    private final VendaRepository vendaRepo = new VendaRepository();
    private final ComprasRepository comprasRepo = new ComprasRepository();

    public final State<String> receitas = new State<>("R$ 0,00");
    public final State<String> despesas = new State<>("R$ 0,00");
    public final State<String> lucroLiquido = new State<>("R$ 0,00");
    public final State<String> mesAtual = new State<>("");

    public HomeScreenViewModel() {
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

                long inicioMillis = DateUtils.localDateParaMillis(primeiroDia);
                long fimMillis = DateUtils.localDateParaMillis(ultimoDia) + 86399999L;

                BigDecimal receitasContas = receitasRepo.somarReceitasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal receitasVendas = vendaRepo.somarVendasPorPeriodo(inicioMillis, fimMillis);
                BigDecimal totalReceitas = receitasContas.add(receitasVendas);

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

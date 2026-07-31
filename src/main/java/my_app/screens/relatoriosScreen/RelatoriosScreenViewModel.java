package my_app.screens.relatoriosScreen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.stage.FileChooser;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import megalodonte.router.v4.ScreenContext;
import my_app.db.services.EmpresaService;
import my_app.domain.components.Components;
import my_app.services.RelatorioDados;
import my_app.services.RelatorioPdfExporter;
import my_app.services.RelatorioService;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class RelatoriosScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(RelatoriosScreenViewModel.class);
    private static final long UM_DIA_MENOS_1MS = 86_399_999L;

    private final ScreenContext ctx;
    private final RelatorioService relatorioService;
    private final EmpresaService empresaService;
    private final RelatorioPdfExporter pdfExporter = new RelatorioPdfExporter();

    final State<LocalDate> dataInicio = State.of(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
    final State<LocalDate> dataFim = State.of(LocalDate.now());

    final State<String> receitasVendas = State.of("R$ 0,00");
    final State<String> receitasPedidosPdv = State.of("R$ 0,00");
    final State<String> receitasContasRecebidas = State.of("R$ 0,00");
    final State<String> despesasCompras = State.of("R$ 0,00");
    final State<String> despesasContasPagas = State.of("R$ 0,00");
    final State<String> totalReceitas = State.of("R$ 0,00");
    final State<String> totalDespesas = State.of("R$ 0,00");
    final State<String> lucroLiquido = State.of("R$ 0,00");
    final State<String> contasReceberEmAberto = State.of("R$ 0,00");
    final State<String> contasPagarEmAberto = State.of("R$ 0,00");

    private RelatorioDados ultimoRelatorio;

    // Dados dos gráficos (javafx.scene.chart, nativo — sem lib nova). O framework de
    // UI da app não tem um jeito genérico de "reconstruir componente quando o State
    // muda" fora de Show.when (que só alterna visibilidade, não reconstrói) — então
    // os gráficos são criados uma vez em RelatoriosScreen e atualizados aqui mutando
    // os mesmos objetos Data/Series, que é como o JavaFX Chart já é reativo por padrão.
    final PieChart.Data receitaVendasSlice = new PieChart.Data("Vendas de Mercadoria", 0);
    final PieChart.Data receitaPdvSlice = new PieChart.Data("Vendas no PDV", 0);
    final PieChart.Data receitaContasSlice = new PieChart.Data("Contas Recebidas", 0);
    final ObservableList<PieChart.Data> receitasPieData =
            FXCollections.observableArrayList(receitaVendasSlice, receitaPdvSlice, receitaContasSlice);

    final PieChart.Data despesaComprasSlice = new PieChart.Data("Compras de Mercadoria", 0);
    final PieChart.Data despesaContasSlice = new PieChart.Data("Contas a Pagar", 0);
    final ObservableList<PieChart.Data> despesasPieData =
            FXCollections.observableArrayList(despesaComprasSlice, despesaContasSlice);

    final XYChart.Data<String, Number> receitasBarra = new XYChart.Data<>("Receitas", 0);
    final XYChart.Data<String, Number> despesasBarra = new XYChart.Data<>("Despesas", 0);
    final XYChart.Data<String, Number> lucroBarra = new XYChart.Data<>("Lucro líquido", 0);
    final XYChart.Series<String, Number> comparativoSeries = new XYChart.Series<>();

    public RelatoriosScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.relatorioService = createOrReport(RelatorioService::new);
        this.empresaService = createOrReport(EmpresaService::new);
        comparativoSeries.setName("Valores (R$)");
        comparativoSeries.getData().addAll(receitasBarra, despesasBarra, lucroBarra);
    }

    private static <T> T createOrReport(megalodonte.utils.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            megalodonte.application.ErrorReporter.handle(e);
            throw new IllegalStateException(e);
        }
    }

    void gerarRelatorio() {
        var inicio = dataInicio.get();
        var fim = dataFim.get();
        if (inicio == null || fim == null) {
            Components.ShowAlertError("Selecione a data de início e a data de fim.");
            return;
        }
        if (inicio.isAfter(fim)) {
            Components.ShowAlertError("Data de início deve ser anterior ou igual à data de fim.");
            return;
        }

        Async.Run(() -> {
            try {
                long inicioMillis = DateUtils.localDateParaMillis(inicio);
                long fimMillis = DateUtils.localDateParaMillis(fim) + UM_DIA_MENOS_1MS;
                var dados = relatorioService.gerar(inicioMillis, fimMillis);
                UI.runOnUi(() -> exibirDados(dados));
            } catch (Exception e) {
                log.error("Erro ao gerar relatório", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao gerar relatório: " + e.getMessage()));
            }
        });
    }

    private void exibirDados(RelatorioDados dados) {
        ultimoRelatorio = dados;
        receitasVendas.set(Utils.toBRLCurrency(dados.receitasVendas()));
        receitasPedidosPdv.set(Utils.toBRLCurrency(dados.receitasPedidosPdv()));
        receitasContasRecebidas.set(Utils.toBRLCurrency(dados.receitasContasRecebidas()));
        despesasCompras.set(Utils.toBRLCurrency(dados.despesasCompras()));
        despesasContasPagas.set(Utils.toBRLCurrency(dados.despesasContasPagas()));
        totalReceitas.set(Utils.toBRLCurrency(dados.totalReceitas()));
        totalDespesas.set(Utils.toBRLCurrency(dados.totalDespesas()));
        lucroLiquido.set(Utils.toBRLCurrency(dados.lucroLiquido()));
        contasReceberEmAberto.set(Utils.toBRLCurrency(dados.contasReceberEmAberto()));
        contasPagarEmAberto.set(Utils.toBRLCurrency(dados.contasPagarEmAberto()));

        receitaVendasSlice.setPieValue(dados.receitasVendas().doubleValue());
        receitaPdvSlice.setPieValue(dados.receitasPedidosPdv().doubleValue());
        receitaContasSlice.setPieValue(dados.receitasContasRecebidas().doubleValue());

        despesaComprasSlice.setPieValue(dados.despesasCompras().doubleValue());
        despesaContasSlice.setPieValue(dados.despesasContasPagas().doubleValue());

        receitasBarra.setYValue(dados.totalReceitas().doubleValue());
        despesasBarra.setYValue(dados.totalDespesas().doubleValue());
        lucroBarra.setYValue(dados.lucroLiquido().doubleValue());
    }

    void baixarPdf() {
        if (ultimoRelatorio == null) {
            Components.ShowAlertError("Gere o relatório antes de baixar o PDF.");
            return;
        }

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar relatório em PDF");
        fileChooser.setInitialFileName("relatorio_" + dataInicio.get() + "_a_" + dataFim.get() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File destino = fileChooser.showSaveDialog(ctx.selfStage());
        if (destino == null) return;

        final var dados = ultimoRelatorio;
        final var inicio = dataInicio.get();
        final var fim = dataFim.get();

        Async.Run(() -> {
            try {
                var empresa = empresaService.buscarUnico();
                pdfExporter.gerar(destino, empresa, inicio, fim, dados);
                UI.runOnUi(() -> Components.ShowPopup(ctx, "PDF salvo em: " + destino.getAbsolutePath()));
            } catch (Exception e) {
                log.error("Erro ao gerar PDF do relatório", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao gerar PDF: " + e.getMessage()));
            }
        });
    }

    public void onDestroy() throws Exception {
        this.relatorioService.close();
        this.empresaService.close();
    }
}

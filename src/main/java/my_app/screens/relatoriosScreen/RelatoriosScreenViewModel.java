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
import my_app.services.ProdutoMaisVendido;
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

    final State<String> produtosMaisVendidos = State.of("");
    final State<String> produtosSemVenda = State.of("");
    final State<String> novosClientes = State.of("");
    final State<String> novosFornecedores = State.of("");

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

    // Quantidade de formas de pagamento varia (depende do que foi usado no período),
    // então, diferente das 2 pizzas acima (fatias fixas), essa lista é reconstruída
    // inteira a cada geração de relatório — o PieChart já reage a isso sozinho.
    final ObservableList<PieChart.Data> formasPagamentoPieData = FXCollections.observableArrayList();

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
        // As 3 barras vêm da mesma Series (única forma de compartilhar o eixo Y
        // sem 3 séries separadas), então o CSS padrão do BarChart pinta todas com
        // a mesma cor (".default-color0"). Coloridas manualmente aqui, uma vez
        // que cada Data ganha seu node, com as mesmas cores dos cards acima.
        colorirBarraQuandoDisponivel(receitasBarra, "#10b981");
        colorirBarraQuandoDisponivel(despesasBarra, "#ef4444");
        colorirBarraQuandoDisponivel(lucroBarra, "#2563eb");
    }

    private void colorirBarraQuandoDisponivel(XYChart.Data<String, Number> data, String cor) {
        data.nodeProperty().addListener((obs, old, node) -> {
            if (node != null) node.setStyle("-fx-bar-fill: " + cor + ";");
        });
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

        var produtos = dados.produtosMaisVendidos();
        produtosMaisVendidos.set(produtos == null || produtos.isEmpty()
                ? "Nenhum produto vendido no período"
                : java.util.stream.IntStream.range(0, produtos.size())
                        .mapToObj(i -> formatarProduto(produtos, i))
                        .collect(java.util.stream.Collectors.joining("\n")));

        var semVenda = dados.produtosSemVenda();
        produtosSemVenda.set(semVenda == null || semVenda.isEmpty()
                ? "Todos os produtos tiveram venda no período"
                : semVenda.stream()
                        .map(p -> "• " + p.descricao() + (p.unidade() != null && !p.unidade().isBlank() ? " (" + p.unidade() + ")" : ""))
                        .collect(java.util.stream.Collectors.joining("\n")));

        var clientesNovos = dados.novosClientes();
        novosClientes.set(clientesNovos == null || clientesNovos.isEmpty()
                ? "Nenhum cliente novo no período"
                : clientesNovos.stream()
                        .map(c -> "• " + c.getNome())
                        .collect(java.util.stream.Collectors.joining("\n")));

        var fornecedoresNovos = dados.novosFornecedores();
        novosFornecedores.set(fornecedoresNovos == null || fornecedoresNovos.isEmpty()
                ? "Nenhum fornecedor novo no período"
                : fornecedoresNovos.stream()
                        .map(f -> "• " + f.getNome())
                        .collect(java.util.stream.Collectors.joining("\n")));

        receitaVendasSlice.setPieValue(dados.receitasVendas().doubleValue());
        receitaPdvSlice.setPieValue(dados.receitasPedidosPdv().doubleValue());
        receitaContasSlice.setPieValue(dados.receitasContasRecebidas().doubleValue());

        despesaComprasSlice.setPieValue(dados.despesasCompras().doubleValue());
        despesaContasSlice.setPieValue(dados.despesasContasPagas().doubleValue());

        receitasBarra.setYValue(dados.totalReceitas().doubleValue());
        despesasBarra.setYValue(dados.totalDespesas().doubleValue());
        lucroBarra.setYValue(dados.lucroLiquido().doubleValue());

        var formasPagamento = dados.formasPagamento();
        formasPagamentoPieData.setAll(formasPagamento == null ? java.util.List.of() :
                formasPagamento.stream()
                        .map(f -> new PieChart.Data(f.forma(), f.valor().doubleValue()))
                        .toList());
    }

    private String formatarProduto(java.util.List<ProdutoMaisVendido> produtos, int indice) {
        if (produtos == null || indice >= produtos.size()) return "";
        var produto = produtos.get(indice);
        String unidade = produto.unidade() != null && !produto.unidade().isBlank() ? produto.unidade() : "un";
        return (indice + 1) + "º " + produto.descricao() + " — " + Utils.quantidadeTratada(produto.quantidade()) + " " + unidade;
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

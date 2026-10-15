package my_app.screens.relatoriosScreen;

import javafx.stage.FileChooser;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.state.State;
import megalodonte.components.BarChartView;
import megalodonte.components.PieChartView;
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.db.services.EmpresaService;
import my_app.core.components.Components;
import my_app.services.*;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class RelatoriosScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(RelatoriosScreenViewModel.class);
    private static final long UM_DIA_MENOS_1MS = 86_399_999L;

    private final ScreenContextInterface ctx;
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
    final State<String> produtosDevolvidos = State.of("");
    final State<String> novosClientes = State.of("");
    final State<String> novosFornecedores = State.of("");

    private RelatorioDados ultimoRelatorio;

    final State<List<PieChartView.Slice>> receitasPieData = State.of(List.of());
    final State<List<PieChartView.Slice>> despesasPieData = State.of(List.of());
    final State<List<PieChartView.Slice>> formasPagamentoPieData = State.of(List.of());
    final State<List<BarChartView.Bar>> comparativoSeries = State.of(List.of());

    public RelatoriosScreenViewModel(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.relatorioService = createOrReport(RelatorioService::new);
        this.empresaService = createOrReport(EmpresaService::new);
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
                long inicioMillis = DatePack.localDateParaMillis(inicio);
                long fimMillis = DatePack.localDateParaMillis(fim) + UM_DIA_MENOS_1MS;
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
        receitasVendas.set(CurrencyPack.toBRLCurrency(dados.receitasVendas()));
        receitasPedidosPdv.set(CurrencyPack.toBRLCurrency(dados.receitasPedidosPdv()));
        receitasContasRecebidas.set(CurrencyPack.toBRLCurrency(dados.receitasContasRecebidas()));
        despesasCompras.set(CurrencyPack.toBRLCurrency(dados.despesasCompras()));
        despesasContasPagas.set(CurrencyPack.toBRLCurrency(dados.despesasContasPagas()));
        totalReceitas.set(CurrencyPack.toBRLCurrency(dados.totalReceitas()));
        totalDespesas.set(CurrencyPack.toBRLCurrency(dados.totalDespesas()));
        lucroLiquido.set(CurrencyPack.toBRLCurrency(dados.lucroLiquido()));
        contasReceberEmAberto.set(CurrencyPack.toBRLCurrency(dados.contasReceberEmAberto()));
        contasPagarEmAberto.set(CurrencyPack.toBRLCurrency(dados.contasPagarEmAberto()));

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

        produtosDevolvidos.set(formatarDevolucoes(dados.devolucoes()));

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

        receitasPieData.set(List.of(
                new PieChartView.Slice("Vendas de Mercadoria", dados.receitasVendas().doubleValue()),
                new PieChartView.Slice("Vendas no PDV", dados.receitasPedidosPdv().doubleValue()),
                new PieChartView.Slice("Contas Recebidas", dados.receitasContasRecebidas().doubleValue())));
        despesasPieData.set(List.of(
                new PieChartView.Slice("Compras de Mercadoria", dados.despesasCompras().doubleValue()),
                new PieChartView.Slice("Contas a Pagar", dados.despesasContasPagas().doubleValue())));
        comparativoSeries.set(List.of(
                new BarChartView.Bar("Receitas", dados.totalReceitas().doubleValue(), "#10b981"),
                new BarChartView.Bar("Despesas", dados.totalDespesas().doubleValue(), "#ef4444"),
                new BarChartView.Bar("Lucro líquido", dados.lucroLiquido().doubleValue(), "#2563eb")));

        var formasPagamento = dados.formasPagamento();
        formasPagamentoPieData.set(formasPagamento == null ? List.of() :
                formasPagamento.stream()
                        .map(f -> new PieChartView.Slice(f.forma(), f.valor().doubleValue()))
                        .toList());
    }

    private String formatarProduto(java.util.List<ProdutoMaisVendido> produtos, int indice) {
        if (produtos == null || indice >= produtos.size()) return "";
        var produto = produtos.get(indice);
        String unidade = produto.unidade() != null && !produto.unidade().isBlank() ? produto.unidade() : "un";
        return (indice + 1) + "º " + produto.descricao() + " — " + Utils.quantidadeTratada(produto.quantidade()) + " " + unidade;
    }

    private String formatarDevolucoes(ResumoDevolucoes resumo) {
        if (resumo == null || resumo.numeroDevolucoes() == 0) {
            return "Nenhum produto devolvido no período";
        }
        var linhas = new java.util.ArrayList<String>();
        linhas.add("Total devolvido: " + Utils.quantidadeTratada(resumo.totalUnidades())
                + " unidade(s) em " + resumo.numeroDevolucoes() + " devolução(ões)");
        for (var produto : resumo.produtos()) {
            String unidade = produto.unidade() != null && !produto.unidade().isBlank()
                    ? produto.unidade() : "un";
            linhas.add("• " + produto.descricao() + " — "
                    + Utils.quantidadeTratada(produto.quantidade()) + " " + unidade);
        }
        return String.join("\n", linhas);
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

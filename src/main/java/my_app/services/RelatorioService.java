package my_app.services;

import my_app.db.DB;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.db.services.ProdutoService;
import my_app.db.services.VendaService;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Orquestra os totais do RelatoriosScreen reaproveitando os mesmos métodos "somarXPorPeriodo"
// já usados pela Home (que só calculam pro mês atual) — aqui o período é escolhido pelo usuário.
public class RelatorioService implements AutoCloseable {

    private static final int TOP_PRODUTOS = 10;

    private final VendaService vendaService;
    private final PedidoService pedidoService;
    private final PedidoItemService pedidoItemService;
    private final ContaAreceberService contaAreceberService;
    private final CompraService compraService;
    private final ContasPagarService contasPagarService;
    private final ProdutoService produtoService;

    public RelatorioService() throws SQLException {
        this(DB.getPersismSession());
    }

    public RelatorioService(Session session) {
        this.vendaService = new VendaService(session);
        this.pedidoService = new PedidoService(session);
        this.pedidoItemService = new PedidoItemService(session);
        this.contaAreceberService = new ContaAreceberService(session);
        this.compraService = new CompraService(session);
        this.contasPagarService = new ContasPagarService(session);
        this.produtoService = new ProdutoService(session);
    }

    public RelatorioDados gerar(long dataInicio, long dataFim) throws SQLException {
        BigDecimal receitasVendas = zeroSeNulo(vendaService.somarVendasPorPeriodo(dataInicio, dataFim));
        BigDecimal receitasPedidosPdv = zeroSeNulo(pedidoService.somarPedidosPorPeriodo(dataInicio, dataFim));
        BigDecimal receitasContasRecebidas = zeroSeNulo(contaAreceberService.somarReceitasPorPeriodo(dataInicio, dataFim));
        BigDecimal despesasCompras = zeroSeNulo(compraService.somarComprasPorPeriodo(dataInicio, dataFim));
        BigDecimal despesasContasPagas = zeroSeNulo(contasPagarService.somarDespesasPorPeriodo(dataInicio, dataFim));

        BigDecimal totalReceitas = receitasVendas.add(receitasPedidosPdv).add(receitasContasRecebidas);
        BigDecimal totalDespesas = despesasCompras.add(despesasContasPagas);
        BigDecimal lucroLiquido = totalReceitas.subtract(totalDespesas);

        BigDecimal contasReceberEmAberto = zeroSeNulo(contaAreceberService.getTotalEmAberto());
        BigDecimal contasPagarEmAberto = zeroSeNulo(contasPagarService.getTotalEmAberto());

        Map<String, BigDecimal> quantidades = quantidadesVendidas(dataInicio, dataFim);

        return new RelatorioDados(
                receitasVendas, receitasPedidosPdv, receitasContasRecebidas,
                despesasCompras, despesasContasPagas,
                totalReceitas, totalDespesas, lucroLiquido,
                contasReceberEmAberto, contasPagarEmAberto,
                topProdutosMaisVendidos(quantidades),
                produtosSemVenda(quantidades)
        );
    }

    public List<ProdutoMaisVendido> produtosMaisVendidos(long dataInicio, long dataFim) throws SQLException {
        return topProdutosMaisVendidos(quantidadesVendidas(dataInicio, dataFim));
    }

    public List<ProdutoMaisVendido> produtosSemVenda(long dataInicio, long dataFim) throws SQLException {
        return produtosSemVenda(quantidadesVendidas(dataInicio, dataFim));
    }

    private Map<String, BigDecimal> quantidadesVendidas(long dataInicio, long dataFim) throws SQLException {
        Map<String, BigDecimal> quantidades = new LinkedHashMap<>();
        for (var item : pedidoItemService.listarPorPeriodo(dataInicio, dataFim)) {
            acumular(quantidades, item);
        }
        for (var venda : vendaService.listarPorPeriodo(dataInicio, dataFim)) {
            acumular(quantidades, venda);
        }
        return quantidades;
    }

    private List<ProdutoMaisVendido> topProdutosMaisVendidos(Map<String, BigDecimal> quantidades) throws SQLException {
        var ordenados = quantidades.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(TOP_PRODUTOS)
                .toList();
        List<ProdutoMaisVendido> resultado = new java.util.ArrayList<>();
        for (var entry : ordenados) {
            resultado.add(toProdutoMaisVendido(entry.getKey(), entry.getValue()));
        }
        return resultado;
    }

    private List<ProdutoMaisVendido> produtosSemVenda(Map<String, BigDecimal> quantidades) throws SQLException {
        Set<String> vendidos = quantidades.keySet();
        return produtoService.listar().stream()
                .filter(produto -> !vendidos.contains(produto.getCodigoBarras()))
                .sorted(Comparator.comparing(
                        ProdutoModel::getDescricao,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(produto -> new ProdutoMaisVendido(
                        produto.getCodigoBarras(),
                        produto.getDescricao() != null ? produto.getDescricao() : produto.getCodigoBarras(),
                        produto.getUnidade(),
                        BigDecimal.ZERO))
                .toList();
    }

    private void acumular(Map<String, BigDecimal> quantidades, PedidoItemModel item) {
        if (item.getQuantidade() != null) {
            quantidades.merge(item.getProdutoCod(), item.getQuantidade(), BigDecimal::add);
        }
    }

    private void acumular(Map<String, BigDecimal> quantidades, VendaModel venda) {
        if (venda.getQuantidade() != null) {
            quantidades.merge(venda.getProdutoCod(), venda.getQuantidade(), BigDecimal::add);
        }
    }

    private ProdutoMaisVendido toProdutoMaisVendido(String codigoBarras, BigDecimal quantidade) throws SQLException {
        var produto = produtoService.buscarPorCodigoBarras(codigoBarras);
        String descricao = produto != null && produto.getDescricao() != null ? produto.getDescricao() : codigoBarras;
        String unidade = produto != null ? produto.getUnidade() : null;
        return new ProdutoMaisVendido(codigoBarras, descricao, unidade, quantidade);
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    @Override
    public void close() throws Exception {
        vendaService.close();
        pedidoService.close();
        pedidoItemService.close();
        contaAreceberService.close();
        compraService.close();
        contasPagarService.close();
        produtoService.close();
    }
}

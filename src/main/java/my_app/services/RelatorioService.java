package my_app.services;

import my_app.db.DB;
import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PedidoService;
import my_app.db.services.VendaService;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;

// Orquestra os totais do RelatoriosScreen reaproveitando os mesmos métodos "somarXPorPeriodo"
// já usados pela Home (que só calculam pro mês atual) — aqui o período é escolhido pelo usuário.
public class RelatorioService implements AutoCloseable {

    private final VendaService vendaService;
    private final PedidoService pedidoService;
    private final ContaAreceberService contaAreceberService;
    private final CompraService compraService;
    private final ContasPagarService contasPagarService;

    public RelatorioService() throws SQLException {
        this(DB.getPersismSession());
    }

    public RelatorioService(Session session) {
        this.vendaService = new VendaService(session);
        this.pedidoService = new PedidoService(session);
        this.contaAreceberService = new ContaAreceberService(session);
        this.compraService = new CompraService(session);
        this.contasPagarService = new ContasPagarService(session);
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

        return new RelatorioDados(
                receitasVendas, receitasPedidosPdv, receitasContasRecebidas,
                despesasCompras, despesasContasPagas,
                totalReceitas, totalDespesas, lucroLiquido,
                contasReceberEmAberto, contasPagarEmAberto
        );
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    @Override
    public void close() throws Exception {
        vendaService.close();
        pedidoService.close();
        contaAreceberService.close();
        compraService.close();
        contasPagarService.close();
    }
}

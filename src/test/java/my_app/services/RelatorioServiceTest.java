package my_app.services;

import my_app.db.dto.CompraDto;
import my_app.db.models.ContaAreceberModel;
import my_app.db.models.ContasPagarModel;
import my_app.db.models.PedidoModel;
import my_app.db.models.VendaModel;
import my_app.db.services.BaseServiceTest;
import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PedidoService;
import my_app.db.services.VendaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioServiceTest extends BaseServiceTest {

    // Janela ampla [0, agora+1min] em vez de "hoje" — mesma técnica já usada em
    // CompraServiceTest.deveSomarComprasPorPeriodo, evita depender de fuso/horário
    // exato de quando dataCriacao é gravado internamente por cada Service.
    private static final long INICIO = 0L;
    private static long fim() {
        return System.currentTimeMillis() + 60_000L;
    }

    private RelatorioService relatorioService;

    @Override
    protected void initService() {
        relatorioService = new RelatorioService(session);
    }

    @Test
    void deveRetornarZerosQuandoNaoHaMovimentacaoNoPeriodo() throws Exception {
        var dados = relatorioService.gerar(INICIO, fim());

        assertBigDecimalEquals(BigDecimal.ZERO, dados.totalReceitas());
        assertBigDecimalEquals(BigDecimal.ZERO, dados.totalDespesas());
        assertBigDecimalEquals(BigDecimal.ZERO, dados.lucroLiquido());
        assertBigDecimalEquals(BigDecimal.ZERO, dados.contasReceberEmAberto());
        assertBigDecimalEquals(BigDecimal.ZERO, dados.contasPagarEmAberto());
    }

    @Test
    void deveSomarReceitasEDespesasDoPeriodo() throws Exception {
        var vendaService = new VendaService(session);
        var venda = new VendaModel();
        venda.setProdutoCod("789");
        venda.setClienteId(1);
        venda.setQuantidade(BigDecimal.ONE);
        venda.setPrecoUnitario(BigDecimal.valueOf(100));
        venda.setTotalLiquido(BigDecimal.valueOf(100));
        venda.setTipoPagamento("DINHEIRO");
        vendaService.salvar(venda, false);

        var pedidoService = new PedidoService(session);
        var pedido = new PedidoModel();
        pedido.setClienteId(1);
        pedido.setFormaPagamento("À VISTA");
        pedido.setTotalLiquido(BigDecimal.TEN);
        pedido.setDesconto(BigDecimal.ZERO);
        pedido.setFiado(0);
        pedidoService.salvar(pedido);

        var contaAreceberService = new ContaAreceberService(session);
        var contaReceber = new ContaAreceberModel();
        contaReceber.setDescricao("Recebimento teste");
        contaReceber.setValorOriginal(BigDecimal.valueOf(30));
        contaReceber.setValorRecebido(BigDecimal.valueOf(30));
        contaReceber.setValorRestante(BigDecimal.ZERO);
        contaReceber.setDataVencimento(System.currentTimeMillis());
        contaReceber.setDataRecebimento(System.currentTimeMillis());
        contaReceber.setStatus("PAGO");
        contaReceber.setClienteId(1);
        contaAreceberService.salvar(contaReceber);

        var compraService = new CompraService(session);
        var compraDto = new CompraDto(
                "789", BigDecimal.valueOf(50), 1, BigDecimal.ONE,
                BigDecimal.ZERO, "DINHEIRO", "obs",
                System.currentTimeMillis(), "NF-1", null, "NAO", BigDecimal.valueOf(50)
        );
        compraService.salvar(compraDto);

        var contasPagarService = new ContasPagarService(session);
        var contaPagar = new ContasPagarModel();
        contaPagar.setDescricao("Pagamento teste");
        contaPagar.setValorOriginal(BigDecimal.valueOf(20));
        contaPagar.setValorPago(BigDecimal.valueOf(20));
        contaPagar.setValorRestante(BigDecimal.ZERO);
        contaPagar.setDataVencimento(System.currentTimeMillis());
        contaPagar.setDataPagamento(System.currentTimeMillis());
        contaPagar.setStatus("PAGO");
        contaPagar.setFornecedorId(1);
        contasPagarService.salvar(contaPagar);

        var dados = relatorioService.gerar(INICIO, fim());

        assertBigDecimalEquals(BigDecimal.valueOf(100), dados.receitasVendas());
        assertBigDecimalEquals(BigDecimal.TEN, dados.receitasPedidosPdv());
        assertBigDecimalEquals(BigDecimal.valueOf(30), dados.receitasContasRecebidas());
        assertBigDecimalEquals(BigDecimal.valueOf(140), dados.totalReceitas());

        assertBigDecimalEquals(BigDecimal.valueOf(50), dados.despesasCompras());
        assertBigDecimalEquals(BigDecimal.valueOf(20), dados.despesasContasPagas());
        assertBigDecimalEquals(BigDecimal.valueOf(70), dados.totalDespesas());

        assertBigDecimalEquals(BigDecimal.valueOf(70), dados.lucroLiquido());

        // as duas contas já estão PAGAS — nada em aberto
        assertBigDecimalEquals(BigDecimal.ZERO, dados.contasReceberEmAberto());
        assertBigDecimalEquals(BigDecimal.ZERO, dados.contasPagarEmAberto());
    }

    @Test
    void deveLancarExcecaoQuandoDataInicioMaiorQueFim() {
        assertThrows(IllegalArgumentException.class, () -> relatorioService.gerar(fim(), INICIO));
    }
}

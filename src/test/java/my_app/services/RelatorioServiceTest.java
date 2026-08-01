package my_app.services;

import my_app.db.dto.CompraDto;
import my_app.db.models.ContaAreceberModel;
import my_app.db.models.ContasPagarModel;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import my_app.db.services.BaseServiceTest;
import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.db.services.ProdutoService;
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
        assertTrue(dados.produtosMaisVendidos().isEmpty());
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

    @Test
    void deveRetornarTop3ProdutosMaisVendidosSomandoPdvEVendas() throws Exception {
        var produtoService = new ProdutoService(session);
        produtoService.salvar(produto("111", "Camiseta", "UN"));
        produtoService.salvar(produto("222", "Calça", "UN"));
        produtoService.salvar(produto("333", "Boné", "UN"));
        produtoService.salvar(produto("444", "Meia", "UN"));

        var vendaService = new VendaService(session);
        vendaService.salvar(venda("111", 2), false);
        vendaService.salvar(venda("222", 5), false);
        vendaService.salvar(venda("333", 1), false);

        var pedidoService = new PedidoService(session);
        var pedido = new PedidoModel();
        pedido.setClienteId(1);
        pedido.setFormaPagamento("À VISTA");
        pedido.setTotalLiquido(BigDecimal.valueOf(50));
        pedido.setDesconto(BigDecimal.ZERO);
        pedido.setFiado(0);
        pedidoService.salvar(pedido);

        var pedidoItemService = new PedidoItemService(session);
        pedidoItemService.salvar(item(pedido.getId(), "222", 3));
        pedidoItemService.salvar(item(pedido.getId(), "444", 4));

        var dados = relatorioService.gerar(INICIO, fim());

        var top = dados.produtosMaisVendidos();
        assertEquals(3, top.size());

        assertEquals("222", top.get(0).codigoBarras());
        assertEquals("Calça", top.get(0).descricao());
        assertEquals("UN", top.get(0).unidade());
        assertBigDecimalEquals(BigDecimal.valueOf(8), top.get(0).quantidade());

        assertEquals("444", top.get(1).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(4), top.get(1).quantidade());

        assertEquals("111", top.get(2).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(2), top.get(2).quantidade());
    }

    @Test
    void deveRetornarMenosDeTresProdutosQuandoPoucosForamVendidos() throws Exception {
        var produtoService = new ProdutoService(session);
        produtoService.salvar(produto("111", "Camiseta", "UN"));

        var vendaService = new VendaService(session);
        vendaService.salvar(venda("111", 1), false);

        var dados = relatorioService.gerar(INICIO, fim());

        assertEquals(1, dados.produtosMaisVendidos().size());
        assertEquals("Camiseta", dados.produtosMaisVendidos().getFirst().descricao());
    }

    @Test
    void deveUsarCodigoDeBarrasQuandoProdutoNaoCadastrado() throws Exception {
        var vendaService = new VendaService(session);
        vendaService.salvar(venda("999", 2), false);

        var dados = relatorioService.gerar(INICIO, fim());

        assertEquals(1, dados.produtosMaisVendidos().size());
        assertEquals("999", dados.produtosMaisVendidos().getFirst().descricao());
    }

    private ProdutoModel produto(String codigo, String descricao, String unidade) {
        var produto = new ProdutoModel();
        produto.setCodigoBarras(codigo);
        produto.setDescricao(descricao);
        produto.setUnidade(unidade);
        produto.setFornecedorId(1);
        produto.setPrecoVenda(BigDecimal.TEN);
        produto.setTotalLiquido(BigDecimal.TEN);
        produto.setEstoque(BigDecimal.ZERO);
        return produto;
    }

    private VendaModel venda(String codigo, int quantidade) {
        var venda = new VendaModel();
        venda.setProdutoCod(codigo);
        venda.setClienteId(1);
        venda.setQuantidade(BigDecimal.valueOf(quantidade));
        venda.setPrecoUnitario(BigDecimal.TEN);
        venda.setTotalLiquido(BigDecimal.TEN.multiply(BigDecimal.valueOf(quantidade)));
        venda.setDesconto(BigDecimal.ZERO);
        venda.setTipoPagamento("DINHEIRO");
        return venda;
    }

    private PedidoItemModel item(Integer pedidoId, String codigo, int quantidade) {
        var item = new PedidoItemModel();
        item.setPedidoId(pedidoId);
        item.setProdutoCod(codigo);
        item.setQuantidade(BigDecimal.valueOf(quantidade));
        item.setPrecoUnitario(BigDecimal.TEN);
        item.setDesconto(BigDecimal.ZERO);
        item.setTotalItem(BigDecimal.TEN.multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }
}

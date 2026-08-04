package my_app.services;

import my_app.db.dto.CompraDto;
import my_app.db.models.ClienteModel;
import my_app.db.models.ContaAreceberModel;
import my_app.db.models.ContasPagarModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import my_app.db.services.BaseServiceTest;
import my_app.db.services.ClienteService;
import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.FornecedorService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.db.services.ProdutoService;
import my_app.db.services.VendaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        assertTrue(dados.produtosSemVenda().isEmpty());
        assertTrue(dados.novosClientes().isEmpty());
        assertTrue(dados.novosFornecedores().isEmpty());
        assertTrue(dados.formasPagamento().isEmpty());
    }

    @Test
    void deveRetornarNovosClientesEFornecedoresDoPeriodo() throws Exception {
        var clienteService = new ClienteService(session);
        var cliente = new ClienteModel();
        cliente.setNome("Maria Nova");
        cliente.setEmail("");
        cliente.setCelular("");
        clienteService.salvar(cliente);

        var fornecedorService = new FornecedorService(session);
        var fornecedor = new FornecedorModel();
        fornecedor.setNome("Distribuidora Nova");
        fornecedorService.salvar(fornecedor);

        var dados = relatorioService.gerar(INICIO, fim());

        assertEquals(1, dados.novosClientes().size());
        assertEquals("Maria Nova", dados.novosClientes().getFirst().getNome());

        assertEquals(1, dados.novosFornecedores().size());
        assertEquals("Distribuidora Nova", dados.novosFornecedores().getFirst().getNome());
    }

    @Test
    void deveSomarFormasDePagamentoDeVendasEPedidosExcluindoAPrazo() throws Exception {
        var vendaService = new VendaService(session);

        var vendaPix = new VendaModel();
        vendaPix.setProdutoCod("111");
        vendaPix.setClienteId(1);
        vendaPix.setQuantidade(BigDecimal.ONE);
        vendaPix.setPrecoUnitario(BigDecimal.valueOf(70));
        vendaPix.setTotalLiquido(BigDecimal.valueOf(70));
        vendaPix.setTipoPagamento("PIX");
        vendaService.salvar(vendaPix, false);

        var vendaAPrazo = new VendaModel();
        vendaAPrazo.setProdutoCod("222");
        vendaAPrazo.setClienteId(1);
        vendaAPrazo.setQuantidade(BigDecimal.ONE);
        vendaAPrazo.setPrecoUnitario(BigDecimal.valueOf(999));
        vendaAPrazo.setTotalLiquido(BigDecimal.valueOf(999));
        vendaAPrazo.setTipoPagamento("A PRAZO");
        vendaService.salvar(vendaAPrazo, false);

        var pedidoService = new PedidoService(session);
        var pedidoPix = new PedidoModel();
        pedidoPix.setClienteId(1);
        pedidoPix.setFormaPagamento("PIX");
        pedidoPix.setTotalLiquido(BigDecimal.valueOf(30));
        pedidoPix.setDesconto(BigDecimal.ZERO);
        pedidoPix.setFiado(0);
        pedidoService.salvar(pedidoPix);

        var pedidoDebito = new PedidoModel();
        pedidoDebito.setClienteId(1);
        pedidoDebito.setFormaPagamento("DÉBITO");
        pedidoDebito.setTotalLiquido(BigDecimal.valueOf(20));
        pedidoDebito.setDesconto(BigDecimal.ZERO);
        pedidoDebito.setFiado(0);
        pedidoService.salvar(pedidoDebito);

        var dados = relatorioService.gerar(INICIO, fim());

        var formas = dados.formasPagamento();
        assertEquals(2, formas.size(), "A PRAZO não deve virar uma forma de pagamento no relatório");

        assertEquals("PIX", formas.get(0).forma());
        assertBigDecimalEquals(BigDecimal.valueOf(100), formas.get(0).valor());

        assertEquals("DÉBITO", formas.get(1).forma());
        assertBigDecimalEquals(BigDecimal.valueOf(20), formas.get(1).valor());
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
    void deveRetornarProdutosMaisVendidosOrdenadosSomandoPdvEVendas() throws Exception {
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

        // Só 4 produtos venderam nesse período — todos abaixo do limite de top 10,
        // então devem aparecer todos, ordenados por quantidade decrescente.
        var top = dados.produtosMaisVendidos();
        assertEquals(4, top.size());

        assertEquals("222", top.get(0).codigoBarras());
        assertEquals("Calça", top.get(0).descricao());
        assertEquals("UN", top.get(0).unidade());
        assertBigDecimalEquals(BigDecimal.valueOf(8), top.get(0).quantidade());

        assertEquals("444", top.get(1).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(4), top.get(1).quantidade());

        assertEquals("111", top.get(2).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(2), top.get(2).quantidade());

        assertEquals("333", top.get(3).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(1), top.get(3).quantidade());

        assertTrue(dados.produtosSemVenda().isEmpty());
    }

    @Test
    void deveLimitarATop10ProdutosMaisVendidosQuandoMaisDeDezForamVendidos() throws Exception {
        var produtoService = new ProdutoService(session);
        var vendaService = new VendaService(session);

        // 12 produtos vendidos, quantidades decrescentes e distintas — só os
        // 10 primeiros (quantidades 12 a 3) devem aparecer no top.
        for (int i = 1; i <= 12; i++) {
            String codigo = "p" + i;
            int quantidade = 13 - i;
            produtoService.salvar(produto(codigo, "Produto " + i, "UN"));
            vendaService.salvar(venda(codigo, quantidade), false);
        }

        var dados = relatorioService.gerar(INICIO, fim());

        var top = dados.produtosMaisVendidos();
        assertEquals(10, top.size());
        assertEquals("p1", top.get(0).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(12), top.get(0).quantidade());
        assertEquals("p10", top.get(9).codigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(3), top.get(9).quantidade());

        // Todos os 12 produtos cadastrados nesse teste tiveram venda — produtosSemVenda
        // só lista os que ficaram de fora do top, não os que nunca venderam.
        assertTrue(dados.produtosSemVenda().isEmpty());
    }

    @Test
    void deveRetornarMenosDeDezProdutosQuandoPoucosForamVendidos() throws Exception {
        var produtoService = new ProdutoService(session);
        produtoService.salvar(produto("111", "Camiseta", "UN"));

        var vendaService = new VendaService(session);
        vendaService.salvar(venda("111", 1), false);

        var dados = relatorioService.gerar(INICIO, fim());

        assertEquals(1, dados.produtosMaisVendidos().size());
        assertEquals("Camiseta", dados.produtosMaisVendidos().getFirst().descricao());
        assertTrue(dados.produtosSemVenda().isEmpty());
    }

    @Test
    void deveUsarCodigoDeBarrasQuandoProdutoNaoCadastrado() throws Exception {
        var vendaService = new VendaService(session);
        vendaService.salvar(venda("999", 2), false);

        var dados = relatorioService.gerar(INICIO, fim());

        assertEquals(1, dados.produtosMaisVendidos().size());
        assertEquals("999", dados.produtosMaisVendidos().getFirst().descricao());
        assertTrue(dados.produtosSemVenda().isEmpty());
    }

    @Test
    void deveRetornarProdutosSemVendaNoPeriodo() throws Exception {
        var produtoService = new ProdutoService(session);
        produtoService.salvar(produto("111", "Arroz", "KG"));
        produtoService.salvar(produto("222", "Feijão", "KG"));
        produtoService.salvar(produto("333", "Óleo", "UN"));

        var vendaService = new VendaService(session);
        vendaService.salvar(venda("111", 2), false);

        var dados = relatorioService.gerar(INICIO, fim());

        var semVenda = dados.produtosSemVenda();
        assertEquals(2, semVenda.size());
        assertEquals("Feijão", semVenda.get(0).descricao());
        assertEquals("KG", semVenda.get(0).unidade());
        assertBigDecimalEquals(BigDecimal.ZERO, semVenda.get(0).quantidade());
        assertEquals("Óleo", semVenda.get(1).descricao());
    }

    @Test
    void deveListarProdutoComoSemVendaQuandoVendaForaDoPeriodo() throws Exception {
        var produtoService = new ProdutoService(session);
        produtoService.salvar(produto("111", "Arroz", "KG"));

        var vendaService = new VendaService(session);
        vendaService.salvar(venda("111", 1), false);

        var venda = vendaService.listar().stream()
                .filter(v -> v.getProdutoCod().equals("111"))
                .findFirst()
                .orElseThrow();
        venda.setDataCriacao(LocalDateTime.now().plusDays(5));
        vendaService.atualizar(venda);

        var dados = relatorioService.gerar(INICIO, fim());

        assertEquals(1, dados.produtosSemVenda().size());
        assertEquals("Arroz", dados.produtosSemVenda().getFirst().descricao());
        assertTrue(dados.produtosMaisVendidos().isEmpty());
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

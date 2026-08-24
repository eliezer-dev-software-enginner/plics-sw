package my_app.services;

import my_app.db.models.ProdutoModel;
import my_app.db.services.ProdutoService;
import my_app.screens.pdvScreen.ItemVenda;
import my_app.db.services.BaseServiceTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDVServiceTest extends BaseServiceTest {

    private PDVService pdvService;
    private ProdutoService produtoService;

    @Override
    protected void initService() {
        pdvService = new PDVService(session);
        produtoService = new ProdutoService(session);
    }

    private ProdutoModel criarProduto() throws Exception {
        var p = novoProduto("789", "Produto Teste");
        p.setAceitaDevolucao(true);
        return produtoService.salvar(p);
    }

    private ProdutoModel novoProduto(String codigoBarras, String descricao) {
        var p = new ProdutoModel();
        p.setCodigoBarras(codigoBarras);
        p.setDescricao(descricao);
        p.setUnidade("UN");
        p.setPrecoVenda(BigDecimal.TEN);
        p.setTotalLiquido(BigDecimal.TEN);
        p.setFornecedorId(1);
        p.setEstoque(BigDecimal.TEN);
        return p;
    }

    private List<ItemVenda> itensValidos() throws Exception {
        var produto = criarProduto();
        var item = new ItemVenda(produto);
        item.quantidade = BigDecimal.ONE;
        return List.of(item);
    }

    private int contarLinhas(String tabela, String where, Object param) throws Exception {
        var sql = "SELECT COUNT(*) FROM " + tabela + " WHERE " + where;
        try (var stmt = rawConnection.prepareStatement(sql)) {
            if (param instanceof Integer i) stmt.setInt(1, i);
            else if (param instanceof Long l) stmt.setLong(1, l);
            try (var rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Test
    void deveFinalizarVendaComClientePadrao() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", 1, false, 1);

        assertNotNull(pedido.getId());
        assertEquals(1, pedido.getClienteId());
        assertNotNull(pedido.getDataCriacao());
        assertBigDecimalEquals(BigDecimal.TEN, pedido.getTotalLiquido());

        assertEquals(1, contarLinhas("pedido_itens", "pedido_id = ?", pedido.getId()));
    }

    @Test
    void deveFinalizarVendaSemCliente() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", null, false, 1);

        assertNotNull(pedido.getId());
        assertNull(pedido.getClienteId());
    }

    @Test
    void deveFinalizarVendaFiadaComCliente() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "CREDIARIO", 1, true, 3);

        assertNotNull(pedido.getId());
        assertEquals(1, pedido.getFiado());

        assertEquals(3, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));
    }

    @Test
    void naoDeveGerarContasQuandoNaoFiado() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", 1, false, 1);

        assertEquals(0, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));
    }

    @Test
    void deveAplicarDescontoNoTotalLiquido() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", 1, false, 1, BigDecimal.valueOf(3));

        assertBigDecimalEquals(BigDecimal.valueOf(3), pedido.getDesconto());
        assertBigDecimalEquals(BigDecimal.valueOf(7), pedido.getTotalLiquido());
    }

    @Test
    void naoDevePermitirTotalLiquidoNegativoComDescontoMaiorQueOTotal() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", 1, false, 1, BigDecimal.valueOf(999));

        assertBigDecimalEquals(BigDecimal.ZERO, pedido.getTotalLiquido());
    }

    @Test
    void deveGerarParcelasComValorLiquidoDescontado() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "CREDIARIO", 1, true, 2, BigDecimal.valueOf(2));

        // total bruto 10, desconto 2 -> liquido 8, dividido em 2 parcelas de 4
        assertEquals(2, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));
        try (var stmt = rawConnection.prepareStatement(
                "SELECT SUM(valor_original) FROM contas_a_receber WHERE venda_id = ?")) {
            stmt.setLong(1, pedido.getId());
            try (var rs = stmt.executeQuery()) {
                rs.next();
                assertBigDecimalEquals(BigDecimal.valueOf(8), BigDecimal.valueOf(rs.getDouble(1)));
            }
        }
    }

    @Test
    void deveExcluirVendaERestaurarEstoque() throws Exception {
        var itens = itensValidos();
        var codigoBarras = itens.getFirst().produto.getCodigoBarras();
        var pedido = pdvService.finalizarVenda(itens, "À VISTA", 1, false, 1);

        assertBigDecimalEquals(BigDecimal.valueOf(9), produtoService.buscarPorCodigoBarras(codigoBarras).getEstoque());

        pdvService.excluirVenda(pedido.getId());

        assertBigDecimalEquals(BigDecimal.TEN, produtoService.buscarPorCodigoBarras(codigoBarras).getEstoque());
        assertEquals(0, contarLinhas("pedido_itens", "pedido_id = ?", pedido.getId()));
        assertEquals(0, contarLinhas("pedidos", "id = ?", pedido.getId()));
    }

    @Test
    void deveExcluirContasAReceberAoExcluirVendaFiada() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "CREDIARIO", 1, true, 3);
        assertEquals(3, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));

        pdvService.excluirVenda(pedido.getId());

        assertEquals(0, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));
    }

    @Test
    void naoDeveMexerEmContasDeOutraVendaAoExcluir() throws Exception {
        var pedidoParaExcluir = pdvService.finalizarVenda(itensValidos(), "CREDIARIO", 1, true, 2);

        var produto2 = new ProdutoModel();
        produto2.setCodigoBarras("790");
        produto2.setDescricao("Produto Teste 2");
        produto2.setUnidade("UN");
        produto2.setPrecoVenda(BigDecimal.TEN);
        produto2.setTotalLiquido(BigDecimal.TEN);
        produto2.setFornecedorId(1);
        produto2.setEstoque(BigDecimal.TEN);
        produtoService.salvar(produto2);
        var item2 = new ItemVenda(produto2);
        item2.quantidade = BigDecimal.ONE;
        var pedidoParaManter = pdvService.finalizarVenda(List.of(item2), "CREDIARIO", 1, true, 2);

        pdvService.excluirVenda(pedidoParaExcluir.getId());

        assertEquals(0, contarLinhas("contas_a_receber", "venda_id = ?", pedidoParaExcluir.getId()));
        assertEquals(2, contarLinhas("contas_a_receber", "venda_id = ?", pedidoParaManter.getId()));
    }

    @Test
    void deveLancarExcecaoAoExcluirVendaInexistente() {
        // withTransaction() envolve a IllegalArgumentException original numa
        // PersismException, mas preserva a mensagem — é isso que a ViewModel exibe.
        var erro = assertThrows(Exception.class, () -> pdvService.excluirVenda(9999));
        assertEquals("Venda não encontrada", erro.getMessage());
    }

    @Test
    void deveSomarFreteNoTotalLiquido() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", 1, false, 1,
                BigDecimal.valueOf(2), BigDecimal.valueOf(5));

        assertBigDecimalEquals(BigDecimal.valueOf(5), pedido.getFrete());
        // total bruto 10 - desconto 2 + frete 5 = 13
        assertBigDecimalEquals(BigDecimal.valueOf(13), pedido.getTotalLiquido());
    }

    @Test
    void deveDevolverVendaERestaurarEstoqueSemApagarRegistro() throws Exception {
        var itens = itensValidos();
        var codigoBarras = itens.getFirst().produto.getCodigoBarras();
        var pedido = pdvService.finalizarVenda(itens, "À VISTA", 1, false, 1);

        assertBigDecimalEquals(BigDecimal.valueOf(9), produtoService.buscarPorCodigoBarras(codigoBarras).getEstoque());

        pdvService.devolverVenda(pedido.getId());

        assertBigDecimalEquals(BigDecimal.TEN, produtoService.buscarPorCodigoBarras(codigoBarras).getEstoque());
        assertEquals(1, contarLinhas("pedidos", "id = ?", pedido.getId()));
    }

    @Test
    void deveCancelarEExcluirContasAoDevolverVendaFiada() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "CREDIARIO", 1, true, 1);
        assertEquals(1, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));

        pdvService.devolverVenda(pedido.getId());

        assertEquals(0, contarLinhas("contas_a_receber", "venda_id = ?", pedido.getId()));
    }

    @Test
    void naoDevePermitirDevolverVendaComProdutoQueNaoAceitaDevolucao() throws Exception {
        var produto = novoProduto("791", "Produto Sem Devolução");
        produto.setAceitaDevolucao(false);
        produtoService.salvar(produto);
        var item = new ItemVenda(produto);
        item.quantidade = BigDecimal.ONE;
        var pedido = pdvService.finalizarVenda(List.of(item), "À VISTA", 1, false, 1);

        var erro = assertThrows(Exception.class, () -> pdvService.devolverVenda(pedido.getId()));
        assertTrue(erro.getMessage().contains("Devolução bloqueada"));
        assertTrue(erro.getMessage().contains("Produto Sem Devolução"));

        // Nada mudou: estoque segue decrescido e o pedido não ganhou marca de devolvida.
        assertBigDecimalEquals(BigDecimal.valueOf(9),
                produtoService.buscarPorCodigoBarras(produto.getCodigoBarras()).getEstoque());
        assertEquals(1, contarLinhas("pedidos", "id = ?", pedido.getId()));
        assertEquals(0, contarLinhas("pedidos", "id = ? AND devolvida = 1", pedido.getId()));
    }

    @Test
    void deveExcluirVendaMesmoComProdutoQueNaoAceitaDevolucao() throws Exception {
        // Exclusão é correção administrativa — a política de devolução não se aplica.
        var produto = novoProduto("792", "Produto Sem Devolução 2");
        produto.setAceitaDevolucao(false);
        produtoService.salvar(produto);
        var item = new ItemVenda(produto);
        item.quantidade = BigDecimal.ONE;
        var pedido = pdvService.finalizarVenda(List.of(item), "À VISTA", 1, false, 1);

        pdvService.excluirVenda(pedido.getId());

        assertEquals(0, contarLinhas("pedidos", "id = ?", pedido.getId()));
        assertBigDecimalEquals(BigDecimal.TEN,
                produtoService.buscarPorCodigoBarras(produto.getCodigoBarras()).getEstoque());
    }

    @Test
    void naoDevePermitirDevolverVendaJaDevolvida() throws Exception {
        var pedido = pdvService.finalizarVenda(itensValidos(), "À VISTA", 1, false, 1);
        pdvService.devolverVenda(pedido.getId());

        var erro = assertThrows(Exception.class, () -> pdvService.devolverVenda(pedido.getId()));
        assertEquals("Esta venda já foi devolvida", erro.getMessage());
    }

    @Test
    void deveLancarExcecaoAoDevolverVendaInexistente() {
        var erro = assertThrows(Exception.class, () -> pdvService.devolverVenda(9999));
        assertEquals("Venda não encontrada", erro.getMessage());
    }
}

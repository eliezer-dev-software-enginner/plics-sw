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
        var p = new ProdutoModel();
        p.setCodigoBarras("789");
        p.setDescricao("Produto Teste");
        p.setUnidade("UN");
        p.setPrecoVenda(BigDecimal.TEN);
        p.setTotalLiquido(BigDecimal.TEN);
        p.setFornecedorId(1);
        p.setEstoque(BigDecimal.TEN);
        return produtoService.salvar(p);
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
}

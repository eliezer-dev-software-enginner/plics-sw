package my_app.db.services;

import my_app.db.models.ProdutoModel;
import my_app.db.models.VendaModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VendaServiceTest extends BaseServiceTest {

    private VendaService vendaService;
    private ProdutoService produtoService;

    @Override
    protected void initService() {
        vendaService = new VendaService(session);
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
        return produtoService.salvar(p);
    }

    private VendaModel criarVendaValida(String produtoCod) {
        var v = new VendaModel();
        v.setProdutoCod(produtoCod);
        v.setClienteId(1);
        v.setQuantidade(BigDecimal.ONE);
        v.setPrecoUnitario(BigDecimal.TEN);
        v.setTotalLiquido(BigDecimal.TEN);
        v.setTipoPagamento("DINHEIRO");
        return v;
    }

    @Test
    void deveLancarExcecaoQuandoProdutoVazio() {
        var v = criarVendaValida("789");
        v.setProdutoCod("");
        assertThrows(IllegalArgumentException.class, () -> vendaService.salvar(v, false));
    }

    @Test
    void deveLancarExcecaoQuandoClienteNull() {
        var v = criarVendaValida("789");
        v.setClienteId(null);
        assertThrows(IllegalArgumentException.class, () -> vendaService.salvar(v, false));
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeMenorOuIgualZero() {
        var v = criarVendaValida("789");
        v.setQuantidade(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> vendaService.salvar(v, false));
    }

    @Test
    void deveLancarExcecaoQuandoPrecoUnitarioMenorOuIgualZero() {
        var v = criarVendaValida("789");
        v.setPrecoUnitario(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> vendaService.salvar(v, false));
    }

    @Test
    void deveSalvarVendaSemAtualizarEstoque() throws Exception {
        criarProduto();
        var v = criarVendaValida("789");
        var salvo = vendaService.salvar(v, false);
        assertNotNull(salvo.getId());
    }

    @Test
    void deveSalvarVendaEAtualizarEstoque() throws Exception {
        var p = criarProduto();
        produtoService.definirEstoque(p.getCodigoBarras(), BigDecimal.TEN);
        var v = criarVendaValida(p.getCodigoBarras());
        v.setQuantidade(BigDecimal.valueOf(3));
        vendaService.salvar(v, true);
        var atualizado = produtoService.buscarPorCodigoBarras(p.getCodigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(7), atualizado.getEstoque());
    }

    @Test
    void deveExcluirVendaSemDevolverEstoque() throws Exception {
        var p = criarProduto();
        var v = criarVendaValida(p.getCodigoBarras());
        var salvo = vendaService.salvar(v, false);
        vendaService.excluir(salvo.getId(), false);
        assertNull(vendaService.buscarById(salvo.getId()));
    }

    @Test
    void deveExcluirVendaEDevolverEstoque() throws Exception {
        var p = criarProduto();
        produtoService.definirEstoque(p.getCodigoBarras(), BigDecimal.TEN);
        var v = criarVendaValida(p.getCodigoBarras());
        var salvo = vendaService.salvar(v, true);
        vendaService.excluir(salvo.getId(), true);
        var atualizado = produtoService.buscarPorCodigoBarras(p.getCodigoBarras());
        assertBigDecimalEquals(BigDecimal.TEN, atualizado.getEstoque());
    }

    @Test
    void deveLancarExcecaoAoSomarPorPeriodoComDataInicioMaiorQueFim() {
        assertThrows(IllegalArgumentException.class, () -> vendaService.somarVendasPorPeriodo(100L, 50L));
    }

    @Test
    void deveBuscarPorCliente() throws Exception {
        criarProduto();
        var v = criarVendaValida("789");
        vendaService.salvar(v, false);
        List<VendaModel> vendas = vendaService.buscarPorCliente(1);
        assertEquals(1, vendas.size());
    }
}

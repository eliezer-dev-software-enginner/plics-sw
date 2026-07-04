package my_app.db.services;

import my_app.db.models.ProdutoModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoServiceTest extends BaseServiceTest {

    private ProdutoService produtoService;

    @Override
    protected void initService() {
        produtoService = new ProdutoService(session);
    }

    private ProdutoModel produtoValido() {
        var p = new ProdutoModel();
        p.setCodigoBarras("789");
        p.setDescricao("Produto Teste");
        p.setUnidade("UN");
        p.setPrecoVenda(BigDecimal.TEN);
        p.setTotalLiquido(BigDecimal.TEN);
        p.setFornecedorId(1);
        p.setCor("Azul");
        p.setTamanho("M");
        p.setModelo("Esportivo");
        return p;
    }

    @Test
    void deveLancarExcecaoQuandoCodigoBarrasVazio() {
        var p = produtoValido();
        p.setCodigoBarras("");
        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(p));
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoVazia() {
        var p = produtoValido();
        p.setDescricao(" ");
        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(p));
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeVazia() {
        var p = produtoValido();
        p.setUnidade(" ");
        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(p));
    }

    @Test
    void deveLancarExcecaoQuandoFornecedorIdNull() {
        var p = produtoValido();
        p.setFornecedorId(null);
        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(p));
    }

    @Test
    void deveSalvarProduto() throws Exception {
        var salvo = produtoService.salvar(produtoValido());
        assertNotNull(salvo.getId());
        assertNotNull(salvo.getDataCriacao());
    }

    @Test
    void deveSalvarProdutoComPropriedades() throws Exception {
        var salvo = produtoService.salvar(produtoValido());
        assertNotNull(salvo.getId());
        assertEquals("Azul", salvo.getCor());
        assertEquals("M", salvo.getTamanho());
        assertEquals("Esportivo", salvo.getModelo());
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        var salvo = produtoService.salvar(produtoValido());
        salvo.setDescricao("Atualizado");
        produtoService.atualizar(salvo);
        var buscado = produtoService.buscarById(salvo.getId());
        assertEquals("Atualizado", buscado.getDescricao());
    }

    @Test
    void deveBuscarPorCodigoBarras() throws Exception {
        produtoService.salvar(produtoValido());
        var encontrado = produtoService.buscarPorCodigoBarras("789");
        assertNotNull(encontrado);
        assertEquals("Produto Teste", encontrado.getDescricao());
    }

    @Test
    void deveIncrementarEstoque() throws Exception {
        var p = produtoService.salvar(produtoValido());
        produtoService.definirEstoque(p.getCodigoBarras(), BigDecimal.ZERO);
        produtoService.incrementarEstoque(p.getCodigoBarras(), BigDecimal.TEN);
        var atualizado = produtoService.buscarPorCodigoBarras(p.getCodigoBarras());
        assertBigDecimalEquals(BigDecimal.TEN, atualizado.getEstoque());
    }

    @Test
    void deveDecrementarEstoque() throws Exception {
        var p = produtoService.salvar(produtoValido());
        produtoService.definirEstoque(p.getCodigoBarras(), BigDecimal.TEN);
        produtoService.decrementarEstoque(p.getCodigoBarras(), BigDecimal.ONE);
        var atualizado = produtoService.buscarPorCodigoBarras(p.getCodigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(9), atualizado.getEstoque());
    }

    @Test
    void deveDefinirEstoque() throws Exception {
        var p = produtoService.salvar(produtoValido());
        produtoService.definirEstoque(p.getCodigoBarras(), BigDecimal.valueOf(50));
        var atualizado = produtoService.buscarPorCodigoBarras(p.getCodigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(50), atualizado.getEstoque());
    }

    @Test
    void deveAtualizarEstoque() throws Exception {
        var p = produtoService.salvar(produtoValido());
        produtoService.definirEstoque(p.getCodigoBarras(), BigDecimal.ZERO);
        produtoService.atualizarEstoque(p.getCodigoBarras(), BigDecimal.valueOf(20));
        var atualizado = produtoService.buscarPorCodigoBarras(p.getCodigoBarras());
        assertBigDecimalEquals(BigDecimal.valueOf(20), atualizado.getEstoque());
    }
}

package my_app.screens;

import my_app.db.models.ProdutoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a funcionalidade de controle de estoque na ComprasScreen
 */
class ComprasScreenTest {

    private ComprasScreen comprasScreen;

    @BeforeEach
    void setUp() {
        // Router será null, mas não é usado nos testes de estoque
        comprasScreen = new ComprasScreen(null);
    }

    @Test
    void testAtualizarEstoqueVisualComControleAtivado() {
        // Arrange
        ProdutoModel produto = createProdutoFake("123456789", new BigDecimal("50"));
        comprasScreen.produtoEncontrado.set(produto);
        comprasScreen.qtd.set("10");
        comprasScreen.opcaoDeControleDeEstoqueSelected.set("Sim");
        
        // Act
        comprasScreen.atualizarEstoqueVisual();
        
        // Assert
        assertEquals("50", comprasScreen.estoqueAnterior.get());
        assertEquals("60", comprasScreen.estoqueAtual.get()); // 50 + 10
    }

    @Test
    void testAtualizarEstoqueVisualComControleDesativado() {
        // Arrange
        ProdutoModel produto = createProdutoFake("123456789", new BigDecimal("50"));
        comprasScreen.produtoEncontrado.set(produto);
        comprasScreen.qtd.set("10");
        comprasScreen.opcaoDeControleDeEstoqueSelected.set("Não");
        
        // Act
        comprasScreen.atualizarEstoqueVisual();
        
        // Assert
        assertEquals("50", comprasScreen.estoqueAnterior.get());
        assertEquals("50", comprasScreen.estoqueAtual.get()); // Não altera
    }

    @Test
    void testAtualizarEstoqueVisualSemProduto() {
        // Arrange
        comprasScreen.produtoEncontrado.set(null);
        comprasScreen.qtd.set("10");
        comprasScreen.opcaoDeControleDeEstoqueSelected.set("Sim");
        
        // Act
        comprasScreen.atualizarEstoqueVisual();
        
        // Assert
        assertEquals("0", comprasScreen.estoqueAnterior.get());
        assertEquals("0", comprasScreen.estoqueAtual.get());
    }

    @Test
    void testClearFormResetaOpcaoControleEstoque() {
        // Arrange
        comprasScreen.opcaoDeControleDeEstoqueSelected.set("Sim");
        
        // Act
        comprasScreen.clearForm();
        
        // Assert
        assertEquals("Não", comprasScreen.opcaoDeControleDeEstoqueSelected.get());
        assertEquals("0", comprasScreen.estoqueAnterior.get());
        assertEquals("0", comprasScreen.estoqueAtual.get());
    }

    @Test
    void testOpcoesDeControleDeEstoqueInicializadas() {
        // Assert - Verifica se as opções estão configuradas corretamente
        assertNotNull(comprasScreen.opcoesDeControleDeEstoque);
        assertEquals(2, comprasScreen.opcoesDeControleDeEstoque.get().size());
        assertTrue(comprasScreen.opcoesDeControleDeEstoque.get().contains("Sim"));
        assertTrue(comprasScreen.opcoesDeControleDeEstoque.get().contains("Não"));
        // Verifica se o valor inicial é "Não" ou "Sim" (depende da implementação)
        String valorInicial = comprasScreen.opcaoDeControleDeEstoqueSelected.get();
        assertTrue(valorInicial.equals("Sim") || valorInicial.equals("Não"), 
                  "Valor inicial deve ser 'Sim' ou 'Não', mas foi: " + valorInicial);
    }

    @Test
    void testAtualizarEstoqueVisualQuantidadeInvalida() {
        // Arrange
        ProdutoModel produto = createProdutoFake("123456789", new BigDecimal("50"));
        comprasScreen.produtoEncontrado.set(produto);
        comprasScreen.qtd.set(""); // Quantidade vazia
        comprasScreen.opcaoDeControleDeEstoqueSelected.set("Sim");
        
        // Act
        comprasScreen.atualizarEstoqueVisual();
        
        // Assert
        assertEquals("50", comprasScreen.estoqueAnterior.get());
        assertEquals("50", comprasScreen.estoqueAtual.get()); // Não deve alterar
    }

    @Test
    void testAtualizarEstoqueVisualComEstoqueNulo() {
        // Arrange
        ProdutoModel produto = createProdutoFake("123456789", null);
        comprasScreen.produtoEncontrado.set(produto);
        comprasScreen.qtd.set("10");
        comprasScreen.opcaoDeControleDeEstoqueSelected.set("Sim");
        
        // Act
        comprasScreen.atualizarEstoqueVisual();
        
        // Assert
        assertEquals("0", comprasScreen.estoqueAnterior.get()); // Deve tratar null como 0
        assertEquals("10", comprasScreen.estoqueAtual.get()); // 0 + 10
    }

    // Método helper para criar produtos fake
    private ProdutoModel createProdutoFake(String codigoBarras, BigDecimal estoque) {
        ProdutoModel produto = new ProdutoModel();
        produto.id = 1L;
        produto.codigoBarras = codigoBarras;
        produto.descricao = "Produto Teste";
        produto.precoCompra = new BigDecimal("10.00");
        produto.precoVenda = new BigDecimal("15.00");
        produto.estoque = estoque;
        produto.categoriaId = 1L;
        produto.fornecedorId = 1L;
        return produto;
    }
}
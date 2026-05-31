package my_app.db.repositories;

import my_app.db.models.ProdutoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(ProdutoRepositoryTest.class);

    ProdutoRepository repository;

    @Override
    protected void initRepository() {
        repository = new ProdutoRepository(session);
    }

    @BeforeEach
    void cleanProdutos() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM produtos");
        }
    }

    private ProdutoModel novoProduto(String codigoBarras) {
        var model = new ProdutoModel();
        model.setCodigoBarras(codigoBarras);
        model.setDescricao("Produto Teste");
        model.setPrecoCompra(BigDecimal.valueOf(10.00));
        model.setPrecoVenda(BigDecimal.valueOf(25.50));
        model.setTotalLiquido(BigDecimal.valueOf(15.50));
        model.setUnidade("UN");
        model.setMarca("Marca Teste");
        model.setCategoriaId(1);
        model.setFornecedorId(1);
        model.setEstoque(BigDecimal.valueOf(100));
        model.setObservacoes("Observacao");
        model.setImagem("/assets/produto-generico.png");
        model.setComissao("5");
        model.setGarantia("1 ano");
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        ProdutoModel salvo = repository.salvar(novoProduto("1234567890123"));

        log.info("Produto salvo com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("1234567890123", salvo.getCodigoBarras());
        assertEquals("Produto Teste", salvo.getDescricao());
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novoProduto("1234567890123"));

        var lista = repository.listar();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void atualizar() throws SQLException {
        ProdutoModel salvo = repository.salvar(novoProduto("1234567890123"));

        salvo.setDescricao("Descricao Atualizada");
        salvo.setPrecoVenda(BigDecimal.valueOf(30.00));
        repository.atualizar(salvo);

        ProdutoModel atualizado = repository.buscarById(salvo.getId());

        log.info("Produto atualizado: descricao={}, precoVenda={}",
                atualizado.getDescricao(), atualizado.getPrecoVenda());

        assertNotNull(atualizado);
        assertEquals("Descricao Atualizada", atualizado.getDescricao());
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(atualizado.getPrecoVenda()));
    }

    @Test
    void excluirById() throws SQLException {
        ProdutoModel salvo = repository.salvar(novoProduto("1234567890123"));

        repository.excluirById(salvo.getId());

        ProdutoModel deletado = repository.buscarById(salvo.getId());

        log.info("Produto removido id={}", salvo.getId());

        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        ProdutoModel salvo = repository.salvar(novoProduto("1234567890123"));

        ProdutoModel encontrado = repository.buscarById(salvo.getId());

        log.info("Produto encontrado: id={}, descricao={}",
                encontrado.getId(), encontrado.getDescricao());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("1234567890123", encontrado.getCodigoBarras());
    }

    @Test
    void buscarPorCodigoBarras() throws SQLException {
        repository.salvar(novoProduto("1234567890123"));

        ProdutoModel encontrado = repository.buscarPorCodigoBarras("1234567890123");

        assertNotNull(encontrado);
        assertEquals("1234567890123", encontrado.getCodigoBarras());
    }

    @Test
    void buscarPorCodigoBarrasInexistente() throws SQLException {
        ProdutoModel encontrado = repository.buscarPorCodigoBarras("0000000000000");

        assertNull(encontrado);
    }
}

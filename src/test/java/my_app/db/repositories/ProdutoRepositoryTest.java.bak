package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.DBInitializer;
import my_app.db.models.ProdutoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoRepositoryTest {

    private ProdutoRepository repo;

    @BeforeEach
    void setup() throws Exception {
        DB.reset();
        DB.getInstance("jdbc:sqlite::memory:");
        //DB.getInstance();
        DBInitializer.init();
        repo = new ProdutoRepository();
    }

    @Test
    void salvar() throws SQLException {
        ProdutoModel p = produtoFake();
        repo.salvar(p);

        ProdutoModel encontrado = repo.buscarPorCodigoBarras(p.codigoBarras);

        assertNotNull(encontrado);
        assertEquals("Arroz", encontrado.descricao);
    }

    @Test
    void buscarPorCodigoBarras() throws SQLException {
        ProdutoModel p = produtoFake();
        repo.salvar(p);

        var encontrado = repo.buscarPorCodigoBarras("123");
        var inexistente = repo.buscarPorCodigoBarras("999");

        assertNotNull(encontrado);
        assertEquals("Arroz", encontrado.descricao);

        assertNull(inexistente);
    }


    @Test
    void listar() throws SQLException {
        assertTrue(repo.listar().isEmpty());

        ProdutoModel p1 = produtoFake();
        ProdutoModel p2 = produtoFake();
        p2.codigoBarras = "456";
        p2.descricao = "Feijão";

        repo.salvar(p1);
        repo.salvar(p2);

        var lista = repo.listar();

        assertEquals(2, lista.size());
        assertTrue(
                lista.stream().anyMatch(p -> p.descricao.equals("Arroz"))
        );
        assertTrue(
                lista.stream().anyMatch(p -> p.descricao.equals("Feijão"))
        );
    }


    @Test
    void atualizar() throws SQLException {
        ProdutoModel p = produtoFake();
        repo.salvar(p);

        p.descricao = "Arroz Integral";
        repo.atualizar(p);

        ProdutoModel atualizado = repo.buscarPorCodigoBarras(p.codigoBarras);
        assertEquals("Arroz Integral", atualizado.descricao);
    }

    @Test
    void excluir() throws SQLException {
        ProdutoModel p = produtoFake();
        repo.salvar(p);

        repo.excluir(p.codigoBarras);

        assertNull(repo.buscarPorCodigoBarras(p.codigoBarras));
    }

    private ProdutoModel produtoFake() {
        var p = new ProdutoModel();
        p.codigoBarras = "123";
        p.descricao = "Arroz";
        p.precoCompra = new BigDecimal("10");
        p.precoVenda = new BigDecimal("15");
        p.unidade = "UN";
        p.categoriaId = 1L;
        p.fornecedorId = 1L;
        p.estoque = 5;
        p.observacoes = "";
        p.imagem = "";
        return p;
    }
}
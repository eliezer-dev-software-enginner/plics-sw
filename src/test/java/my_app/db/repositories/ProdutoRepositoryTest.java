package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.DBInitializer;
import my_app.db.models.Models;
import my_app.db.models.Models.Produto;
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
        DBInitializer.init();
        repo = new ProdutoRepository();
    }

    @Test
    void salvar() throws SQLException {
        Produto p = produtoFake();
        repo.salvar(p);

        Produto encontrado = repo.buscarPorCodigoBarras(p.codigoBarras);

        assertNotNull(encontrado);
        assertEquals("Arroz", encontrado.descricao);
    }

    @Test
    void buscarPorCodigoBarras() {
    }

    @Test
    void listar() {
    }

    @Test
    void atualizar() throws SQLException {
        Produto p = produtoFake();
        repo.salvar(p);

        p.descricao = "Arroz Integral";
        repo.atualizar(p);

        Produto atualizado = repo.buscarPorCodigoBarras(p.codigoBarras);
        assertEquals("Arroz Integral", atualizado.descricao);
    }

    @Test
    void excluir() throws SQLException {
        Produto p = produtoFake();
        repo.salvar(p);

        repo.excluir(p.codigoBarras);

        assertNull(repo.buscarPorCodigoBarras(p.codigoBarras));
    }

    private Produto produtoFake() {
        Produto p = new Produto();
        p.codigoBarras = "123";
        p.descricao = "Arroz";
        p.precoCompra = new BigDecimal("10");
        p.precoVenda = new BigDecimal("15");
        p.unidade = "UN";
        p.categoria = "Padrão";
        p.fornecedor = "Fornecedor Padrão";
        p.estoque = 5;
        p.observacoes = "";
        p.imagem = "";
        return p;
    }
}
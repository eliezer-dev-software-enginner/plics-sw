package my_app.db.repositories;

import my_app.db.models.PedidoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoRepositoryTest.class);

    PedidoRepository repository;

    @Override
    protected void initRepository() {
        repository = new PedidoRepository(session);
    }

    @BeforeEach
    void cleanPedidos() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM pedidos");
        }
    }

    private PedidoModel novoPedido() {
        var model = new PedidoModel();
        model.setFormaPagamento("DÉBITO");
        model.setTotalLiquido(BigDecimal.valueOf(150.00));
        model.setDesconto(BigDecimal.ZERO);
        model.setFiado(0);
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        PedidoModel salvo = repository.salvar(novoPedido());

        log.info("Pedido salvo com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("DÉBITO", salvo.getFormaPagamento());
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(salvo.getTotalLiquido()));
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novoPedido());

        var lista = repository.listar();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void buscarById() throws SQLException {
        PedidoModel salvo = repository.salvar(novoPedido());

        PedidoModel encontrado = repository.buscarById(salvo.getId());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
    }

    @Test
    void atualizar() throws SQLException {
        PedidoModel salvo = repository.salvar(novoPedido());

        salvo.setFormaPagamento("CRÉDITO");
        repository.atualizar(salvo);

        PedidoModel atualizado = repository.buscarById(salvo.getId());

        assertNotNull(atualizado);
        assertEquals("CRÉDITO", atualizado.getFormaPagamento());
    }

    @Test
    void excluirById() throws SQLException {
        PedidoModel salvo = repository.salvar(novoPedido());

        repository.excluirById(salvo.getId());

        PedidoModel deletado = repository.buscarById(salvo.getId());

        assertNull(deletado);
    }
}

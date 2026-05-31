package my_app.db.repositories;

import my_app.db.models.PedidoItemModel;
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

class PedidoItemRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoItemRepositoryTest.class);

    PedidoItemRepository repository;
    PedidoRepository pedidoRepository;

    @Override
    protected void initRepository() {
        repository = new PedidoItemRepository(session);
        pedidoRepository = new PedidoRepository(session);
    }

    @BeforeEach
    void cleanTables() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM pedido_itens");
            stmt.execute("DELETE FROM pedidos");
        }
    }

    private PedidoModel salvarPedido() throws SQLException {
        var pedido = new PedidoModel();
        pedido.setFormaPagamento("PIX");
        pedido.setTotalLiquido(BigDecimal.valueOf(100.00));
        pedido.setDesconto(BigDecimal.ZERO);
        pedido.setFiado(0);
        pedido.setDataCriacao(LocalDateTime.now());
        return pedidoRepository.salvar(pedido);
    }

    private PedidoItemModel novoItem(Integer pedidoId) {
        var model = new PedidoItemModel();
        model.setPedidoId(pedidoId);
        model.setProdutoCod("1234567890123");
        model.setQuantidade(BigDecimal.valueOf(2));
        model.setPrecoUnitario(BigDecimal.valueOf(50.00));
        model.setDesconto(BigDecimal.ZERO);
        model.setTotalItem(BigDecimal.valueOf(100.00));
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        var pedido = salvarPedido();
        PedidoItemModel salvo = repository.salvar(novoItem(pedido.getId()));

        log.info("Item salvo com id={}, pedidoId={}", salvo.getId(), salvo.getPedidoId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals(pedido.getId(), salvo.getPedidoId());
        assertEquals("1234567890123", salvo.getProdutoCod());
    }

    @Test
    void listarPorPedido() throws SQLException {
        var pedido = salvarPedido();
        repository.salvar(novoItem(pedido.getId()));
        repository.salvar(novoItem(pedido.getId()));

        var itens = repository.listarPorPedido(pedido.getId());

        log.info("Itens encontrados: {}", itens.size());

        assertNotNull(itens);
        assertEquals(2, itens.size());
    }

    @Test
    void listarPorPedidoVazio() throws SQLException {
        var itens = repository.listarPorPedido(999);

        assertNotNull(itens);
        assertTrue(itens.isEmpty());
    }
}

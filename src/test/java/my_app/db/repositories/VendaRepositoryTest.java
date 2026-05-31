package my_app.db.repositories;

import my_app.db.models.VendaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VendaRepositoryTest extends BaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(VendaRepositoryTest.class);

    VendaRepository repository;

    @Override
    protected void initRepository() {
        repository = new VendaRepository(session);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM vendas");
        }
    }

    private VendaModel novaVenda(String produtoCod, BigDecimal quantidade, BigDecimal totalLiquido) {
        var model = new VendaModel();
        model.setProdutoCod(produtoCod);
        model.setClienteId(1);
        model.setQuantidade(quantidade);
        model.setPrecoUnitario(new BigDecimal("10.00"));
        model.setTotalLiquido(totalLiquido);
        model.setDesconto(BigDecimal.ZERO);
        model.setTipoPagamento("A VISTA");
        model.setObservacao("teste");
        model.setDataCriacao(LocalDateTime.now());
        model.setDataVenda(System.currentTimeMillis());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        var model = novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00"));

        var salvo = repository.salvar(model);

        log.info("Venda salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("COD001", salvo.getProdutoCod());
        assertEquals(0, new BigDecimal("20.00").compareTo(salvo.getTotalLiquido()));
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00")));
        repository.salvar(novaVenda("COD002", new BigDecimal("3"), new BigDecimal("30.00")));

        var lista = repository.listar();

        assertNotNull(lista);
        assertEquals(2, lista.size());
    }

    @Test
    void atualizar() throws SQLException {
        var salvo = repository.salvar(novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00")));

        salvo.setQuantidade(new BigDecimal("5"));
        salvo.setTotalLiquido(new BigDecimal("50.00"));
        repository.atualizar(salvo);

        var atualizado = repository.buscarById(salvo.getId());

        assertNotNull(atualizado);
        assertEquals(0, new BigDecimal("5").compareTo(atualizado.getQuantidade()));
        assertEquals(0, new BigDecimal("50.00").compareTo(atualizado.getTotalLiquido()));
    }

    @Test
    void excluirById() throws SQLException {
        var salvo = repository.salvar(novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00")));

        repository.excluirById(salvo.getId());

        var deletado = repository.buscarById(salvo.getId());
        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        var salvo = repository.salvar(novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00")));

        var encontrado = repository.buscarById(salvo.getId());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("COD001", encontrado.getProdutoCod());
    }

    @Test
    void buscarPorCliente() throws SQLException {
        var v1 = novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00"));
        v1.setClienteId(1);
        repository.salvar(v1);

        var v2 = novaVenda("COD002", new BigDecimal("3"), new BigDecimal("30.00"));
        v2.setClienteId(2);
        repository.salvar(v2);

        var resultado = repository.buscarPorCliente(1);

        assertEquals(1, resultado.size());
        assertEquals("COD001", resultado.getFirst().getProdutoCod());
    }

    @Test
    void somarVendasPorPeriodo() throws SQLException {
        var agora = System.currentTimeMillis();

        var v1 = novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00"));
        v1.setTipoPagamento("A VISTA");
        repository.salvar(v1);

        var v2 = novaVenda("COD002", new BigDecimal("3"), new BigDecimal("30.00"));
        v2.setTipoPagamento("A VISTA");
        repository.salvar(v2);

        var total = repository.somarVendasPorPeriodo(agora - 86400000L, agora + 86400000L);

        assertEquals(0, new BigDecimal("50.00").compareTo(total));
    }

    @Test
    void somarVendasHoje() throws SQLException {
        repository.salvar(novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00")));
        repository.salvar(novaVenda("COD002", new BigDecimal("3"), new BigDecimal("30.00")));

        var total = repository.somarVendasHoje();

        assertEquals(0, new BigDecimal("50.00").compareTo(total));
    }

    @Test
    void somarVendasPorPeriodoIgnoraAPrazo() throws SQLException {
        var agora = System.currentTimeMillis();

        var v1 = novaVenda("COD001", new BigDecimal("2"), new BigDecimal("20.00"));
        v1.setTipoPagamento("A VISTA");
        repository.salvar(v1);

        var v2 = novaVenda("COD002", new BigDecimal("3"), new BigDecimal("30.00"));
        v2.setTipoPagamento("A PRAZO");
        repository.salvar(v2);

        var total = repository.somarVendasPorPeriodo(agora - 86400000L, agora + 86400000L);

        assertEquals(0, new BigDecimal("20.00").compareTo(total));
    }
}

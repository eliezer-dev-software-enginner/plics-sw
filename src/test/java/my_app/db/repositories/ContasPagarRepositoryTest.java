package my_app.db.repositories;

import my_app.db.models.ContasPagarModel;
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

class ContasPagarRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(ContasPagarRepositoryTest.class);

    ContasPagarRepository repository;

    @Override
    protected void initRepository() {
        repository = new ContasPagarRepository(session);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM contas_pagar");
        }
    }

    private ContasPagarModel novaConta(String descricao, BigDecimal valor, Long dataVencimento, String status) {
        var model = new ContasPagarModel();
        model.setDescricao(descricao);
        model.setValorOriginal(valor);
        model.setValorPago(BigDecimal.ZERO);
        model.setValorRestante(valor);
        model.setDataVencimento(dataVencimento);
        model.setStatus(status);
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        var model = novaConta("Conta de teste", new BigDecimal("150.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");

        ContasPagarModel salvo = repository.salvar(model);

        log.info("Conta salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("Conta de teste", salvo.getDescricao());
        assertEquals(0, new BigDecimal("150.00").compareTo(salvo.getValorOriginal()));
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novaConta("Conta 1", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE"));
        repository.salvar(novaConta("Conta 2", new BigDecimal("200.00"),
                System.currentTimeMillis() + 172800000L, "PENDENTE"));

        List<ContasPagarModel> lista = repository.listar();

        assertNotNull(lista);
        assertEquals(2, lista.size());
    }

    @Test
    void atualizar() throws SQLException {
        var salvo = repository.salvar(novaConta("Original", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE"));

        salvo.setDescricao("Atualizada");
        salvo.setValorOriginal(new BigDecimal("150.00"));
        repository.atualizar(salvo);

        var atualizado = repository.buscarById(salvo.getId());

        assertNotNull(atualizado);
        assertEquals("Atualizada", atualizado.getDescricao());
        assertEquals(0, new BigDecimal("150.00").compareTo(atualizado.getValorOriginal()));
    }

    @Test
    void excluirById() throws SQLException {
        var salvo = repository.salvar(novaConta("Para excluir", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE"));

        repository.excluirById(salvo.getId());

        var deletado = repository.buscarById(salvo.getId());
        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        var salvo = repository.salvar(novaConta("Busca por ID", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE"));

        var encontrado = repository.buscarById(salvo.getId());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Busca por ID", encontrado.getDescricao());
    }

    @Test
    void buscarPorStatus() throws SQLException {
        repository.salvar(novaConta("Paga", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PAGO"));
        repository.salvar(novaConta("Pendente", new BigDecimal("200.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE"));

        var pendentes = repository.buscarPorStatus("PENDENTE");

        assertEquals(1, pendentes.size());
        assertEquals("Pendente", pendentes.getFirst().getDescricao());
    }

    @Test
    void buscarVencidas() throws SQLException {
        repository.salvar(novaConta("Vencida", new BigDecimal("100.00"),
                System.currentTimeMillis() - 86400000L, "PENDENTE"));
        repository.salvar(novaConta("Futura", new BigDecimal("200.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE"));

        var vencidas = repository.buscarVencidas();

        assertEquals(1, vencidas.size());
        assertEquals("Vencida", vencidas.getFirst().getDescricao());
    }

    @Test
    void buscarPorFornecedor() throws SQLException {
        var c1 = novaConta("Fornec 1", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        c1.setFornecedorId(1);
        repository.salvar(c1);

        var c2 = novaConta("Fornec 2", new BigDecimal("200.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        c2.setFornecedorId(2);
        repository.salvar(c2);

        var resultado = repository.buscarPorFornecedor(1);

        assertEquals(1, resultado.size());
        assertEquals("Fornec 1", resultado.getFirst().getDescricao());
    }

    @Test
    void buscarPorPeriodo() throws SQLException {
        var agora = System.currentTimeMillis();
        repository.salvar(novaConta("Antiga", new BigDecimal("100.00"),
                agora - 86400000L, "PENDENTE"));
        repository.salvar(novaConta("Recente", new BigDecimal("200.00"),
                agora + 86400000L, "PENDENTE"));

        var resultado = repository.buscarPorPeriodo(agora - 172800000L, agora);

        assertEquals(1, resultado.size());
        assertEquals("Antiga", resultado.getFirst().getDescricao());
    }

    @Test
    void buscarPorCompra() throws SQLException {
        var c1 = novaConta("Compra 1", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        c1.setCompraId(10);
        repository.salvar(c1);

        var c2 = novaConta("Sem compra", new BigDecimal("200.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        repository.salvar(c2);

        var resultado = repository.buscarPorCompra(10);

        assertEquals(1, resultado.size());
        assertEquals("Compra 1", resultado.getFirst().getDescricao());
    }

    @Test
    void somarDespesasPorPeriodo() throws SQLException {
        var agora = System.currentTimeMillis();

        var c1 = novaConta("Paga 1", new BigDecimal("100.00"),
                agora - 86400000L, "PAGO");
        c1.setDataPagamento(agora);
        c1.setValorPago(new BigDecimal("100.00"));
        c1.setValorRestante(BigDecimal.ZERO);
        repository.salvar(c1);

        var c2 = novaConta("Paga 2", new BigDecimal("200.00"),
                agora - 86400000L, "PAGO");
        c2.setDataPagamento(agora);
        c2.setValorPago(new BigDecimal("200.00"));
        c2.setValorRestante(BigDecimal.ZERO);
        repository.salvar(c2);

        var total = repository.somarDespesasPorPeriodo(agora - 86400000L, agora + 86400000L);

        assertEquals(0, new BigDecimal("300.00").compareTo(total));
    }
}

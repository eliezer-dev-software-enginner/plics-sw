package my_app.db.repositories;

import my_app.db.models.ContaAreceberModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContaAreceberRepositoryTest extends BaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(ContaAreceberRepositoryTest.class);

    ContasAReceberRepository repository;

    @Override
    protected void initRepository() {
        repository = new ContasAReceberRepository(session);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM contas_a_receber");
        }
    }

    private ContaAreceberModel novaConta(String descricao, BigDecimal valor, Long dataVencimento, String status) {
        var model = new ContaAreceberModel();
        model.setDescricao(descricao);
        model.setValorOriginal(valor);
        model.setValorRecebido(BigDecimal.ZERO);
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

        var salvo = repository.salvar(model);

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

        var lista = repository.listar();

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
    void buscarPorCliente() throws SQLException {
        var c1 = novaConta("Cliente 1", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        c1.setClienteId(1);
        repository.salvar(c1);

        var c2 = novaConta("Cliente 2", new BigDecimal("200.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        c2.setClienteId(2);
        repository.salvar(c2);

        var resultado = repository.buscarPorCliente(1);

        assertEquals(1, resultado.size());
        assertEquals("Cliente 1", resultado.getFirst().getDescricao());
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
    void buscarPorVenda() throws SQLException {
        var c1 = novaConta("Venda 1", new BigDecimal("100.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        c1.setVendaId(10);
        repository.salvar(c1);

        var c2 = novaConta("Sem venda", new BigDecimal("200.00"),
                System.currentTimeMillis() + 86400000L, "PENDENTE");
        repository.salvar(c2);

        var resultado = repository.buscarPorVenda(10);

        assertEquals(1, resultado.size());
        assertEquals("Venda 1", resultado.getFirst().getDescricao());
    }

    @Test
    void somarReceitasPorPeriodo() throws SQLException {
        var agora = System.currentTimeMillis();

        var c1 = novaConta("Recebida 1", new BigDecimal("100.00"),
                agora - 86400000L, "PAGO");
        c1.setDataRecebimento(agora);
        c1.setValorRecebido(new BigDecimal("100.00"));
        c1.setValorRestante(BigDecimal.ZERO);
        repository.salvar(c1);

        var c2 = novaConta("Recebida 2", new BigDecimal("200.00"),
                agora - 86400000L, "PAGO");
        c2.setDataRecebimento(agora);
        c2.setValorRecebido(new BigDecimal("200.00"));
        c2.setValorRestante(BigDecimal.ZERO);
        repository.salvar(c2);

        var total = repository.somarReceitasPorPeriodo(agora - 86400000L, agora + 86400000L);

        assertEquals(0, new BigDecimal("300.00").compareTo(total));
    }
}

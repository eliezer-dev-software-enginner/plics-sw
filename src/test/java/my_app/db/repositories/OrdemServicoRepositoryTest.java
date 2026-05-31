package my_app.db.repositories;

import my_app.db.models.OrdemServicoModel;
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

class OrdemServicoRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(OrdemServicoRepositoryTest.class);

    OrdemServicoRepository repository;
    private long nextNumeroOs = 1001;

    @Override
    protected void initRepository() {
        repository = new OrdemServicoRepository(session);
    }

    @BeforeEach
    void cleanTable() throws Exception {
        nextNumeroOs = 1001;
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM ordens_de_servico");
        }
    }

    private OrdemServicoModel novaOS(String descricao, BigDecimal valor, String status) {
        var model = new OrdemServicoModel();
        model.setClienteId(1);
        model.setTecnicoId(1);
        model.setNumeroOs(nextNumeroOs++);
        model.setEquipamento(descricao);
        model.setMaoDeObraValor(valor);
        model.setPecasValor(BigDecimal.ZERO);
        model.setTipoPagamento("A VISTA");
        model.setStatus(status);
        model.setChecklistRelatorio("Checklist " + descricao);
        model.setDataEscolhida(System.currentTimeMillis() + 86400000L);
        model.setTotalLiquido(valor);
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        var model = novaOS("Notebook Dell", new BigDecimal("150.00"), "Orçamento");
        OrdemServicoModel salvo = repository.salvar(model);

        log.info("OS salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("Notebook Dell", salvo.getEquipamento());
        assertEquals(0, new BigDecimal("150.00").compareTo(salvo.getMaoDeObraValor()));
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novaOS("OS 1", new BigDecimal("100.00"), "Aberto"));
        repository.salvar(novaOS("OS 2", new BigDecimal("200.00"), "Aberto"));

        List<OrdemServicoModel> lista = repository.listar();

        assertNotNull(lista);
        assertEquals(2, lista.size());
    }

    @Test
    void atualizar() throws SQLException {
        var salvo = repository.salvar(novaOS("Original", new BigDecimal("100.00"), "Aberto"));

        salvo.setEquipamento("Atualizado");
        salvo.setMaoDeObraValor(new BigDecimal("150.00"));
        repository.atualizar(salvo);

        var atualizado = repository.buscarById(salvo.getId());

        assertNotNull(atualizado);
        assertEquals("Atualizado", atualizado.getEquipamento());
        assertEquals(0, new BigDecimal("150.00").compareTo(atualizado.getMaoDeObraValor()));
    }

    @Test
    void excluirById() throws SQLException {
        var salvo = repository.salvar(novaOS("Para excluir", new BigDecimal("100.00"), "Aberto"));

        repository.excluirById(salvo.getId());

        var deletado = repository.buscarById(salvo.getId());
        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        var salvo = repository.salvar(novaOS("Busca", new BigDecimal("100.00"), "Aberto"));

        var encontrado = repository.buscarById(salvo.getId());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Busca", encontrado.getEquipamento());
    }

    @Test
    void gerarProximoNumeroOS() throws SQLException {
        var os1 = repository.salvar(novaOS("OS 1", new BigDecimal("100.00"), "Aberto"));
        os1.setNumeroOs(1001L);
        repository.atualizar(os1);

        long proximo = repository.gerarProximoNumeroOS();
        assertEquals(1002L, proximo);
    }

    @Test
    void buscarPorStatus() throws SQLException {
        repository.salvar(novaOS("OS Aberta", new BigDecimal("100.00"), "Aberto"));
        repository.salvar(novaOS("OS Fechada", new BigDecimal("200.00"), "Finalizado"));

        var abertas = repository.buscarPorStatus("Aberto");

        assertEquals(1, abertas.size());
        assertEquals("OS Aberta", abertas.getFirst().getEquipamento());
    }

    @Test
    void buscarPorCliente() throws SQLException {
        var os1 = novaOS("Cliente 1", new BigDecimal("100.00"), "Aberto");
        os1.setClienteId(1);
        repository.salvar(os1);

        var os2 = novaOS("Cliente 2", new BigDecimal("200.00"), "Aberto");
        os2.setClienteId(2);
        repository.salvar(os2);

        var resultado = repository.buscarPorCliente(1);

        assertEquals(1, resultado.size());
        assertEquals("Cliente 1", resultado.getFirst().getEquipamento());
    }

    @Test
    void buscarPorPeriodo() throws SQLException {
        var agora = System.currentTimeMillis();
        var os1 = novaOS("Antiga", new BigDecimal("100.00"), "Aberto");
        os1.setDataEscolhida(agora - 86400000L);
        repository.salvar(os1);

        var os2 = novaOS("Recente", new BigDecimal("200.00"), "Aberto");
        os2.setDataEscolhida(agora + 86400000L);
        repository.salvar(os2);

        var resultado = repository.buscarPorPeriodo(agora - 172800000L, agora);

        assertEquals(1, resultado.size());
        assertEquals("Antiga", resultado.getFirst().getEquipamento());
    }
}

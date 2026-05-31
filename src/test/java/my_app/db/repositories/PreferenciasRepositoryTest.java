package my_app.db.repositories;

import my_app.db.models.PreferenciasModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PreferenciasRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(PreferenciasRepositoryTest.class);

    PreferenciasRepository repository;

    @Override
    protected void initRepository() {
        repository = new PreferenciasRepository(session);
    }

    @BeforeEach
    void cleanPreferencias() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM preferencias");
        }
    }

    private PreferenciasModel novaPreferencia(String tema, int credenciaisHabilitadas) {
        var model = new PreferenciasModel();
        model.setTema(tema);
        model.setCredenciaisHabilitadas(credenciaisHabilitadas);
        model.setLogin("admin");
        model.setSenha("1234");
        model.setPrimeiroAcesso(1);
        model.setDataCriacaoMillis(System.currentTimeMillis());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        PreferenciasModel salvo = repository.salvar(novaPreferencia("Claro", 0));

        log.info("Preferencia salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("Claro", salvo.getTema());
        assertEquals(0, salvo.getCredenciaisHabilitadas());
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novaPreferencia("Escuro", 1));

        var lista = repository.listar();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void atualizar() throws SQLException {
        PreferenciasModel salvo = repository.salvar(novaPreferencia("Claro", 0));

        salvo.setTema("Escuro");
        salvo.setCredenciaisHabilitadas(1);
        salvo.setLogin("user");
        repository.atualizar(salvo);

        PreferenciasModel atualizado = repository.buscarById(salvo.getId());

        log.info("Preferencia atualizada: tema={}, credenciaisHabilitadas={}",
                atualizado.getTema(), atualizado.getCredenciaisHabilitadas());

        assertNotNull(atualizado);
        assertEquals("Escuro", atualizado.getTema());
        assertEquals(1, atualizado.getCredenciaisHabilitadas());
        assertEquals("user", atualizado.getLogin());
    }

    @Test
    void excluirById() throws SQLException {
        PreferenciasModel salvo = repository.salvar(novaPreferencia("Claro", 0));

        repository.excluirById(salvo.getId());

        PreferenciasModel deletado = repository.buscarById(salvo.getId());

        log.info("Preferencia removida id={}", salvo.getId());

        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        PreferenciasModel salvo = repository.salvar(novaPreferencia("Escuro", 1));

        PreferenciasModel encontrado = repository.buscarById(salvo.getId());

        log.info("Preferencia encontrada: id={}, tema={}",
                encontrado.getId(), encontrado.getTema());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Escuro", encontrado.getTema());
    }
}

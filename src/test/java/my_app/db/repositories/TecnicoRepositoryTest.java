package my_app.db.repositories;

import my_app.db.models.TecnicoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TecnicoRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(TecnicoRepositoryTest.class);

    TecnicoRepository repository;

    @Override
    protected void initRepository() {
        repository = new TecnicoRepository(session);
    }

    @BeforeEach
    void cleanTecnicos() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM tecnicos");
        }
    }

    private TecnicoModel novoTecnico(String nome) {
        var model = new TecnicoModel();
        model.setNome(nome);
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        TecnicoModel salvo = repository.salvar(novoTecnico("João"));

        log.info("Técnico salvo com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("João", salvo.getNome());
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novoTecnico("Maria"));

        var lista = repository.listar();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void atualizar() throws SQLException {
        TecnicoModel salvo = repository.salvar(novoTecnico("Carlos"));

        salvo.setNome("Carlos Atualizado");
        repository.atualizar(salvo);

        TecnicoModel atualizado = repository.buscarById(salvo.getId());

        log.info("Técnico atualizado: nome={}", atualizado.getNome());

        assertNotNull(atualizado);
        assertEquals("Carlos Atualizado", atualizado.getNome());
    }

    @Test
    void excluirById() throws SQLException {
        TecnicoModel salvo = repository.salvar(novoTecnico("Ana"));

        repository.excluirById(salvo.getId());

        TecnicoModel deletado = repository.buscarById(salvo.getId());

        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        TecnicoModel salvo = repository.salvar(novoTecnico("Pedro"));

        TecnicoModel encontrado = repository.buscarById(salvo.getId());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Pedro", encontrado.getNome());
    }
}

package my_app.db.repositories;

import my_app.db.models.CategoriaModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(CategoriaRepositoryTest.class);

    CategoriaRepository repository;

    @Override
    protected void initRepository() {
        repository = new CategoriaRepository(session);
    }

    @Test
    void salvar() throws SQLException {

        CategoriaModel model = new CategoriaModel();
        model.setNome("Categoria Teste");
        model.setDataCriacao(LocalDateTime.now());

        CategoriaModel salvo = repository.salvar(model);

        log.info("Categoria salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("Categoria Teste", salvo.getNome());
    }

    @Test
    void listar() throws SQLException {

        CategoriaModel model = new CategoriaModel();
        model.setNome("Listagem");
        model.setDataCriacao(LocalDateTime.now());

        repository.salvar(model);

        List<CategoriaModel> lista = repository.listar();

        lista.forEach(it ->
                log.info("Categoria encontrada: {}", it.getNome())
        );

        assertNotNull(lista);
        assertFalse(lista.isEmpty());

        boolean encontrou = lista.stream()
                .anyMatch(it -> it.getNome().equals("Listagem"));

        assertTrue(encontrou);
    }

    @Test
    void atualizar() throws SQLException {

        CategoriaModel model = new CategoriaModel();
        model.setNome("Original");
        model.setDataCriacao(LocalDateTime.now());

        CategoriaModel salvo = repository.salvar(model);

        salvo.setNome("Atualizado");

        repository.atualizar(salvo);

        CategoriaModel atualizado =
                repository.buscarById(salvo.getId());

        log.info("Categoria atualizada para: {}",
                atualizado.getNome());

        assertNotNull(atualizado);

        assertEquals("Atualizado",
                atualizado.getNome());
    }

    @Test
    void excluirById() throws SQLException {

        CategoriaModel model = new CategoriaModel();
        model.setNome("Excluir");
        model.setDataCriacao(LocalDateTime.now());

        CategoriaModel salvo = repository.salvar(model);

        repository.excluirById(salvo.getId());

        CategoriaModel deleted =
                repository.buscarById(salvo.getId());

        log.info("Categoria removida id={}", salvo.getId());

        assertNull(deleted);
    }

    @Test
    void buscarById() throws SQLException {

        CategoriaModel model = new CategoriaModel();
        model.setNome("Busca");
        model.setDataCriacao(LocalDateTime.now());

        CategoriaModel salvo = repository.salvar(model);

        CategoriaModel encontrado =
                repository.buscarById(salvo.getId());

        log.info("Categoria encontrada: {}",
                encontrado.getNome());

        assertNotNull(encontrado);

        assertEquals(salvo.getId(),
                encontrado.getId());

        assertEquals("Busca",
                encontrado.getNome());
    }
}
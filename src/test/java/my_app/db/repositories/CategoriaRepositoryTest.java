package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.DBInitializer;
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaRepositoryTest {
    private CategoriaRepository repo;

    @BeforeEach
    void setup() throws Exception {
        DB.reset();
        DB.getInstance("jdbc:sqlite::memory:");
        DBInitializer.init();
        repo = new CategoriaRepository();
    }


    @Test
    void salvar() throws SQLException {
        var dto = new CategoriaDto("cat1", System.currentTimeMillis());
        var salvo = repo.salvar(dto);

        var encontrado = repo.buscarPorId(salvo.id);

        assertNotNull(encontrado);
        assertEquals("cat1", encontrado.nome);
        assertNotNull(salvo.id);
    }

    @Test
    void listar() throws SQLException {
        {
            // Verifica se existe a categoria padrão "Geral"
            var listaInicial = repo.listar();
            assertTrue(
                    listaInicial.stream().anyMatch(p -> p.nome.equals("Geral"))
            );

            var dto1 = new CategoriaDto("categ1", System.currentTimeMillis());
            var dto2 = new CategoriaDto("categ2", System.currentTimeMillis());

            repo.salvar(dto1);
            repo.salvar(dto2);

            var lista = repo.listar();

            // Deve ter a categoria "Geral" mais as 2 novas categorias
            assertEquals(listaInicial.size() + 2, lista.size());
            assertTrue(
                    lista.stream().anyMatch(p -> p.nome.equals("Geral"))
            );
            assertTrue(
                    lista.stream().anyMatch(p -> p.nome.equals("categ1"))
            );
            assertTrue(
                    lista.stream().anyMatch(p -> p.nome.equals("categ2"))
            );
        }

    }

    @Test
    void atualizar() throws SQLException {
        var dto = categoriaDtoFake();
        var salvo = repo.salvar(dto);

        salvo.nome = "cat2";
        repo.atualizar(salvo);

        var atualizado = repo.buscarPorId(salvo.id);
        assertEquals("cat2", atualizado.nome);
    }

    @Test
    void excluir() throws SQLException {
        var dto = categoriaDtoFake();
        var salvo = repo.salvar(dto);

        repo.excluir(salvo.id);

        assertNull(repo.buscarPorId(salvo.id));
    }


    private CategoriaDto categoriaDtoFake() {
        return new CategoriaDto("cat1", System.currentTimeMillis());
    }

    private CategoriaModel categoriaFake() {
        var model = new CategoriaModel();
        model.id = null; // Deixar o banco definir o ID autoincrement
        model.nome = "cat1";
        model.dataCriacao = System.currentTimeMillis();
        return model;
    }


}
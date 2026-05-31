package my_app.db.repositories;

import my_app.db.models.FornecedorModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FornecedorRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(FornecedorRepositoryTest.class);

    FornecedorRepository repository;

    @Override
    protected void initRepository() {
        repository = new FornecedorRepository(session);
    }

    @BeforeEach
    void cleanFornecedores() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM fornecedores");
        }
    }

    private FornecedorModel novoFornecedor(String nome) {
        var model = new FornecedorModel();
        model.setNome(nome);
        model.setCpfCnpj("11.222.333/0001-44");
        model.setCelular("(31) 99999-0000");
        model.setEmail("fornecedor@teste.com");
        model.setInscricaoEstadual("123.456.789");
        model.setUfSelected("MG-Belo Horizonte");
        model.setCidade("Belo Horizonte");
        model.setBairro("Centro");
        model.setRua("Rua Principal");
        model.setNumero("123");
        model.setObservacao("Observacao teste");
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        FornecedorModel salvo = repository.salvar(novoFornecedor("Fornecedor Teste"));

        log.info("Fornecedor salvo com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("Fornecedor Teste", salvo.getNome());
        assertEquals("11.222.333/0001-44", salvo.getCpfCnpj());
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novoFornecedor("Fornecedor A"));

        var lista = repository.listar();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
    }

    @Test
    void atualizar() throws SQLException {
        FornecedorModel salvo = repository.salvar(novoFornecedor("Original"));

        salvo.setNome("Atualizado");
        salvo.setCelular("(31) 98888-0000");
        repository.atualizar(salvo);

        FornecedorModel atualizado = repository.buscarById(salvo.getId());

        log.info("Fornecedor atualizado: nome={}, celular={}",
                atualizado.getNome(), atualizado.getCelular());

        assertNotNull(atualizado);
        assertEquals("Atualizado", atualizado.getNome());
        assertEquals("(31) 98888-0000", atualizado.getCelular());
    }

    @Test
    void excluirById() throws SQLException {
        FornecedorModel salvo = repository.salvar(novoFornecedor("Excluir"));

        repository.excluirById(salvo.getId());

        FornecedorModel deletado = repository.buscarById(salvo.getId());

        log.info("Fornecedor removido id={}", salvo.getId());

        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        FornecedorModel salvo = repository.salvar(novoFornecedor("Busca"));

        FornecedorModel encontrado = repository.buscarById(salvo.getId());

        log.info("Fornecedor encontrado: id={}, nome={}",
                encontrado.getId(), encontrado.getNome());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Busca", encontrado.getNome());
    }
}

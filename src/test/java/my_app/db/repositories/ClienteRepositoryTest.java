package my_app.db.repositories;

import my_app.db.models.ClienteModel;
import net.sf.persism.Session;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(ClienteRepositoryTest.class);

    Session session;
    ClienteRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        String testUrl = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";
        Connection connection = DriverManager.getConnection(testUrl);

        Flyway.configure()
                .dataSource(testUrl, "", "")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();

        session = new Session(connection);
        repository = new ClienteRepository(session);
        log.info("Banco de teste inicializado");
    }

    @AfterEach
    void tearDown() throws Exception {
        session.close();
    }

    private ClienteModel novoCliente(String nome) {
        var cliente = new ClienteModel();
        cliente.setNome(nome);
        cliente.setCpfCnpj("123.456.789-00");
        cliente.setCelular("(31) 99999-0000");
        cliente.setEmail("teste@email.com");
        cliente.setIsPessoaFisica(true);
        cliente.setDataCriacao(LocalDateTime.now());
        return cliente;
    }

    @Test
    void salvar() throws SQLException {
        ClienteModel salvo = repository.salvar(novoCliente("João Silva"));

        log.info("Cliente salvo com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("João Silva", salvo.getNome());
        assertEquals("123.456.789-00", salvo.getCpfCnpj());
        assertTrue(salvo.getIsPessoaFisica());
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novoCliente("Maria Souza"));

        List<ClienteModel> lista = repository.listar();

        lista.forEach(it ->
                log.info("Cliente encontrado: {}", it.getNome())
        );

        assertNotNull(lista);
        assertFalse(lista.isEmpty());

        boolean encontrou = lista.stream()
                .anyMatch(it -> it.getNome().equals("Maria Souza"));

        assertTrue(encontrou);
    }

    @Test
    void atualizar() throws SQLException {
        ClienteModel salvo = repository.salvar(novoCliente("Carlos Original"));

        salvo.setNome("Carlos Atualizado");
        salvo.setEmail("novo@email.com");

        repository.atualizar(salvo);

        ClienteModel atualizado = repository.buscarById(salvo.getId());

        log.info("Cliente atualizado: nome={}, email={}",
                atualizado.getNome(), atualizado.getEmail());

        assertNotNull(atualizado);
        assertEquals("Carlos Atualizado", atualizado.getNome());
        assertEquals("novo@email.com", atualizado.getEmail());
    }

    @Test
    void excluirById() throws SQLException {
        ClienteModel salvo = repository.salvar(novoCliente("Excluir Cliente"));

        repository.excluirById(salvo.getId());

        ClienteModel deletado = repository.buscarById(salvo.getId());

        log.info("Cliente removido id={}", salvo.getId());

        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        ClienteModel salvo = repository.salvar(novoCliente("Busca Cliente"));

        ClienteModel encontrado = repository.buscarById(salvo.getId());

        log.info("Cliente encontrado: {}", encontrado.getNome());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Busca Cliente", encontrado.getNome());
    }
}
package my_app.db.repositories;

import my_app.db.models.EmpresaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaRepositoryTest extends BaseRepositoryTest {

    private static final Logger log =
            LoggerFactory.getLogger(EmpresaRepositoryTest.class);

    EmpresaRepository repository;

    @Override
    protected void initRepository() {
        repository = new EmpresaRepository(session);
    }

    @BeforeEach
    void cleanEmpresas() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM empresas");
        }
    }

    private EmpresaModel novaEmpresa(String nome) {
        var model = new EmpresaModel();
        model.setNome(nome);
        model.setCpfCnpj("11.222.333/0001-44");
        model.setTelefone("(31) 99999-0000");
        model.setCep("30140-071");
        model.setCidade("Belo Horizonte");
        model.setBairro("Centro");
        model.setRua("Rua Principal");
        model.setLocalPagamento("Na entrega");
        model.setTextoResponsabilidade("Texto de responsabilidade");
        model.setTermoServico("Termo de serviço");
        model.setLogoMarca("/logo.png");
        model.setDataCriacao(LocalDateTime.now());
        return model;
    }

    @Test
    void salvar() throws SQLException {
        EmpresaModel salvo = repository.salvar(novaEmpresa("Empresa Teste"));

        log.info("Empresa salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("Empresa Teste", salvo.getNome());
        assertEquals("11.222.333/0001-44", salvo.getCpfCnpj());
    }

    @Test
    void buscarUnico() throws SQLException {
        EmpresaModel salvo = repository.salvar(novaEmpresa("Empresa Unica"));

        EmpresaModel encontrado = repository.buscarUnico();

        log.info("Empresa encontrada: {}", encontrado.getNome());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Empresa Unica", encontrado.getNome());
    }

    @Test
    void atualizar() throws SQLException {
        EmpresaModel salvo = repository.salvar(novaEmpresa("Original"));

        salvo.setNome("Atualizada");
        salvo.setTelefone("(31) 98888-0000");

        repository.atualizar(salvo);

        EmpresaModel atualizado = repository.buscarById(salvo.getId());

        log.info("Empresa atualizada: nome={}, telefone={}",
                atualizado.getNome(), atualizado.getTelefone());

        assertNotNull(atualizado);
        assertEquals("Atualizada", atualizado.getNome());
        assertEquals("(31) 98888-0000", atualizado.getTelefone());
    }

    @Test
    void excluirById() throws SQLException {
        EmpresaModel salvo = repository.salvar(novaEmpresa("Excluir"));

        repository.excluirById(salvo.getId());

        EmpresaModel deletado = repository.buscarById(salvo.getId());

        log.info("Empresa removida id={}", salvo.getId());

        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        EmpresaModel salvo = repository.salvar(novaEmpresa("Busca"));

        EmpresaModel encontrado = repository.buscarById(salvo.getId());

        log.info("Empresa encontrada: {}", encontrado.getNome());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("Busca", encontrado.getNome());
    }
}

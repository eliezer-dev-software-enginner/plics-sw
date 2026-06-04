package my_app.db.services;

import my_app.db.models.EmpresaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaServiceTest extends BaseServiceTest {

    private EmpresaService empresaService;

    @Override
    protected void initService() {
        empresaService = new EmpresaService(session);
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        var e = new EmpresaModel();
        e.setNome("");
        assertThrows(IllegalArgumentException.class, () -> empresaService.salvarOuAtualizar(e));
    }

    @Test
    void deveSalvarPrimeiraEmpresa() throws Exception {
        var e = new EmpresaModel();
        e.setNome("Minha Empresa");
        var salvo = empresaService.salvarOuAtualizar(e);
        assertNotNull(salvo.getId());
        assertEquals("Minha Empresa", salvo.getNome());
    }

    @Test
    void deveAtualizarEmpresaExistente() throws Exception {
        var e = new EmpresaModel();
        e.setNome("Empresa Original");
        empresaService.salvarOuAtualizar(e);
        var e2 = new EmpresaModel();
        e2.setNome("Empresa Atualizada");
        var atualizado = empresaService.salvarOuAtualizar(e2);
        assertEquals("Empresa Atualizada", atualizado.getNome());
    }

    @Test
    void deveBuscarUnico() throws Exception {
        var e = new EmpresaModel();
        e.setNome("Minha Empresa");
        empresaService.salvarOuAtualizar(e);
        var encontrado = empresaService.buscarUnico();
        assertNotNull(encontrado);
        assertEquals("Minha Empresa", encontrado.getNome());
    }

    @Test
    void deveRetornarNullQuandoNaoHaEmpresa() throws Exception {
        assertNull(empresaService.buscarUnico());
    }
}

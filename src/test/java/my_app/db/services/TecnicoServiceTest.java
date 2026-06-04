package my_app.db.services;

import my_app.db.models.TecnicoModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TecnicoServiceTest extends BaseServiceTest {

    private TecnicoService tecnicoService;

    @Override
    protected void initService() {
        tecnicoService = new TecnicoService(session);
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        var t = new TecnicoModel();
        t.setNome("");
        assertThrows(IllegalArgumentException.class, () -> tecnicoService.salvar(t));
    }

    @Test
    void deveSalvarTecnico() throws Exception {
        var t = new TecnicoModel();
        t.setNome("João Técnico");
        var salvo = tecnicoService.salvar(t);
        assertNotNull(salvo.getId());
        assertNotNull(salvo.getDataCriacao());
    }

    @Test
    void deveAtualizarTecnico() throws Exception {
        var t = new TecnicoModel();
        t.setNome("João");
        var salvo = tecnicoService.salvar(t);
        salvo.setNome("João Atualizado");
        tecnicoService.atualizar(salvo);
        var buscado = tecnicoService.buscarById(salvo.getId());
        assertEquals("João Atualizado", buscado.getNome());
    }
}

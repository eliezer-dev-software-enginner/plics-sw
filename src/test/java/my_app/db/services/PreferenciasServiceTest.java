package my_app.db.services;

import my_app.db.models.PreferenciasModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreferenciasServiceTest extends BaseServiceTest {

    private PreferenciasService preferenciasService;

    @Override
    protected void initService() {
        preferenciasService = new PreferenciasService(session);
    }

    private PreferenciasModel modelBase() {
        var model = new PreferenciasModel();
        model.setTema("claro");
        model.setDataCriacaoMillis(System.currentTimeMillis());
        model.setCredenciaisHabilitadas(0);
        return model;
    }

    @Test
    void deveSalvarComCredenciaisDesabilitadas() throws Exception {
        var model = modelBase();
        var salvo = preferenciasService.salvar(model);
        assertNotNull(salvo.getId());
    }

    @Test
    void deveLancarExcecaoAoHabilitarCredenciaisSemLogin() {
        var model = modelBase();
        model.setCredenciaisHabilitadas(1);
        model.setSenha("admin123");

        var erro = assertThrows(IllegalArgumentException.class, () -> preferenciasService.salvar(model));
        assertEquals("Login é obrigatório", erro.getMessage());
    }

    @Test
    void deveLancarExcecaoAoHabilitarCredenciaisSemSenha() {
        var model = modelBase();
        model.setCredenciaisHabilitadas(1);
        model.setLogin("admin");

        var erro = assertThrows(IllegalArgumentException.class, () -> preferenciasService.salvar(model));
        assertEquals("Senha é obrigatória", erro.getMessage());
    }

    @Test
    void deveSalvarComCredenciaisHabilitadasELoginSenhaPreenchidos() throws Exception {
        var model = modelBase();
        model.setCredenciaisHabilitadas(1);
        model.setLogin("admin");
        model.setSenha("admin123");

        var salvo = preferenciasService.salvar(model);
        assertNotNull(salvo.getId());
    }

    @Test
    void deveLancarExcecaoAoAtualizarHabilitandoCredenciaisSemLogin() throws Exception {
        var salvo = preferenciasService.salvar(modelBase());
        salvo.setCredenciaisHabilitadas(1);
        salvo.setLogin("");
        salvo.setSenha("admin123");

        var erro = assertThrows(IllegalArgumentException.class, () -> preferenciasService.atualizar(salvo));
        assertEquals("Login é obrigatório", erro.getMessage());
    }
}

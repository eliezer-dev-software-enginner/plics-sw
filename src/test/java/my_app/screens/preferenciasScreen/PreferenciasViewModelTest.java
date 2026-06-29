package my_app.screens.preferenciasScreen;

import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreferenciasViewModelTest extends BaseViewModelTest {

    private PreferenciasViewModel vm;
    private PreferenciasService preferenciasService;

    @Override
    protected void initService() {
        preferenciasService = new PreferenciasService(session);
        vm = new PreferenciasViewModel(null, preferenciasService);
    }

    @Test
    void deveSalvarPreferencias() throws Exception {
        var pref = new PreferenciasModel();
        pref.setDataCriacaoMillis(System.currentTimeMillis());
        pref.setTema("padrão");
        pref.setLogin("admin");
        pref.setSenha("123");
        pref.setCredenciaisHabilitadas(0);
        pref.setPrimeiroAcesso(0);
        preferenciasService.salvar(pref);

        vm.load();
        waitForAsync();

        vm.loginState.set("novo_admin");
        vm.passwordState.set("nova_senha");
        vm.habilitarCredenciaisSelected.set("Sim");

        vm.salvar();
        waitForAsync();

        var list = preferenciasService.listar();
        assertFalse(list.isEmpty());
        var atualizada = list.getFirst();
        assertEquals("novo_admin", atualizada.getLogin());
    }

    @Test
    void validarDeveRetornarNullQuandoCredenciaisDesabilitadas() {
        vm.habilitarCredenciaisSelected.set("Não");
        vm.loginState.set("");
        vm.passwordState.set("");
        assertNull(vm.validar());
    }

    @Test
    void validarDeveRetornarErroQuandoLoginVazio() {
        vm.habilitarCredenciaisSelected.set("Sim");
        vm.loginState.set("");
        vm.passwordState.set("123");
        assertEquals("Login é obrigatório", vm.validar());
    }

    @Test
    void validarDeveRetornarErroQuandoSenhaVazia() {
        vm.habilitarCredenciaisSelected.set("Sim");
        vm.loginState.set("admin");
        vm.passwordState.set("");
        assertEquals("Senha é obrigatória", vm.validar());
    }

    @Test
    void validarDeveRetornarNullQuandoAmbosPreenchidos() {
        vm.habilitarCredenciaisSelected.set("Sim");
        vm.loginState.set("admin");
        vm.passwordState.set("123");
        assertNull(vm.validar());
    }

}

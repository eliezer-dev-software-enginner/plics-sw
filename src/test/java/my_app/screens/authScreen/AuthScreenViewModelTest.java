package my_app.screens.authScreen;

import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthScreenViewModelTest extends BaseViewModelTest {

    private AuthScreenViewModel vm;
    private PreferenciasService preferenciasService;

    @Override
    protected void initService() {
        preferenciasService = new PreferenciasService(session);
        vm = new AuthScreenViewModel(preferenciasService);
    }

    @Test
    void deveCarregarPreferencias() throws Exception {
        var pref = new PreferenciasModel();
        pref.setDataCriacaoMillis(System.currentTimeMillis());
        pref.setTema("padrão");
        pref.setLogin("admin");
        pref.setSenha("123");
        pref.setCredenciaisHabilitadas(0);
        pref.setPrimeiroAcesso(1);
        preferenciasService.salvar(pref);

        vm.load();
        waitForAsync();

        var list = preferenciasService.listar();
        assertFalse(list.isEmpty());
        assertEquals("admin", list.getFirst().getLogin());
    }

    @Test
    void deveReconhecerLicencaProducaoComoValida() {
        for (var licenca : AuthScreenViewModel.LICENCAS_PRODUCAO) {
            assertFalse(vm.isLicensaTesteExpirada(licenca));
        }
    }

    @Test
    void deveReconhecerLicensaNulaComoNaoExpirada() {
        assertFalse(vm.isLicensaTesteExpirada(null));
    }

    @Test
    void deveReconhecerLicencaVaziaComoNaoExpirada() {
        assertFalse(vm.isLicensaTesteExpirada(""));
    }
}

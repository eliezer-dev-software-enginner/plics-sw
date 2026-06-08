package my_app.screens.homeScreen;

import my_app.db.models.PreferenciasModel;
import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PedidoService;
import my_app.db.services.PreferenciasService;
import my_app.db.services.VendaService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomeScreenViewModelTest extends BaseViewModelTest {

    private HomeScreenViewModel vm;
    private PreferenciasService preferenciasService;

    @Override
    protected void initService() {
        preferenciasService = new PreferenciasService(session);
        var receitasService = new ContaAreceberService(session);
        var despesasService = new ContasPagarService(session);
        var vendaService = new VendaService(session);
        var compraService = new CompraService(session);
        var pedidoService = new PedidoService(session);
        vm = new HomeScreenViewModel(preferenciasService, receitasService, despesasService, vendaService, compraService, pedidoService);
    }

    @Test
    void deveCalcularFinanceiro() throws Exception {
        vm.calcularFinanceiroMesAtual();
        waitForAsync();

        assertNotNull(vm.receitas.get());
        assertNotNull(vm.despesas.get());
        assertNotNull(vm.lucroLiquido.get());
    }

    @Test
    void deveIniciarComValoresPadrao() {
        assertEquals("R$ 0,00", vm.receitas.get());
        assertEquals("R$ 0,00", vm.despesas.get());
        assertEquals("R$ 0,00", vm.lucroLiquido.get());
    }

    @Test
    void deveRetornarFalsoQuandoNaoHaPreferencias() {
        assertFalse(vm.isLicensaTesteExpirada());
    }

    @Test
    void deveRetornarFalsoParaLicencaProducao() throws Exception {
        for (var licenca : my_app.screens.authScreen.AuthScreenViewModel.LICENCAS_PRODUCAO) {
            limparTabelaPreferencias();
            var pref = new PreferenciasModel();
            pref.setDataCriacaoMillis(System.currentTimeMillis());
            pref.setTema("padrão");
            pref.setLogin("admin");
            pref.setSenha("123");
            pref.setCredenciaisHabilitadas(0);
            pref.setPrimeiroAcesso(0);
            pref.setLicensa(licenca);
            preferenciasService.salvar(pref);

            assertFalse(vm.isLicensaTesteExpirada(), "Falhou para licenca: " + licenca);
        }
    }

    private void limparTabelaPreferencias() throws Exception {
        var stmt = rawConnection.createStatement();
        stmt.execute("DELETE FROM preferencias");
    }

    @Test
    void deveRetornarFalsoParaLicencaTesteValida() throws Exception {
        var pref = new PreferenciasModel();
        pref.setDataCriacaoMillis(System.currentTimeMillis());
        pref.setTema("padrão");
        pref.setLogin("admin");
        pref.setSenha("123");
        pref.setCredenciaisHabilitadas(0);
        pref.setPrimeiroAcesso(0);
        pref.setLicensa("QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ");
        preferenciasService.salvar(pref);

        boolean expired = vm.isLicensaTesteExpirada();
        int today = java.time.LocalDate.now().getDayOfMonth();
        if (today > 11) {
            assertTrue(expired);
        } else {
            assertFalse(expired);
        }
    }
}

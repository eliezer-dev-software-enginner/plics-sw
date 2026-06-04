package my_app.screens.homeScreen;

import my_app.db.services.CompraService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.ContasPagarService;
import my_app.db.services.PedidoService;
import my_app.db.services.VendaService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomeScreenViewModelTest extends BaseViewModelTest {

    private HomeScreenViewModel vm;

    @Override
    protected void initService() {
        var receitasService = new ContaAreceberService(session);
        var despesasService = new ContasPagarService(session);
        var vendaService = new VendaService(session);
        var compraService = new CompraService(session);
        var pedidoService = new PedidoService(session);
        vm = new HomeScreenViewModel(receitasService, despesasService, vendaService, compraService, pedidoService);
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
}

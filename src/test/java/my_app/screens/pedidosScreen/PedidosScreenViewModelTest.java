package my_app.screens.pedidosScreen;

import my_app.db.models.PedidoModel;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidosScreenViewModelTest extends BaseViewModelTest {

    private PedidosScreenViewModel vm;
    private PedidoService pedidoService;

    @Override
    protected void initService() {
        pedidoService = new PedidoService(session);
        var pedidoItemService = new PedidoItemService(session);
        vm = new PedidosScreenViewModel(null, pedidoService, pedidoItemService);
    }

    @Test
    void deveCarregarPedidos() throws Exception {
        vm.loadPedidos();
        waitForAsync();

        assertNotNull(vm.pedidos.get());
    }

    @Test
    void handleAddOrUpdateNaoLancaExcecao() {
        assertDoesNotThrow(() -> vm.handleAddOrUpdate());
    }
}

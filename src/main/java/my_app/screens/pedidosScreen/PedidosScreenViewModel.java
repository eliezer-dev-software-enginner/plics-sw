package my_app.screens.pedidosScreen;

import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class PedidosScreenViewModel extends ViewModelScreenContract {

    private static final Logger log = LoggerFactory.getLogger(PedidosScreenViewModel.class);

    private final PedidoService pedidoService;
    private final PedidoItemService pedidoItemService;

    final ListState<PedidoModel> pedidos = ListState.ofEmpty();
    final ListState<PedidoItemModel> itensDoPedidoSelecionado = ListState.ofEmpty();
    final State<PedidoModel> pedidoSelecionado = State.of(null);

    public PedidosScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.pedidoService = createOrReport(PedidoService::new);
        this.pedidoItemService = createOrReport(PedidoItemService::new);
        onInit();
    }

    @Override
    protected void onInit() {
        pedidoSelecionado.subscribe(pedido -> {
            if (pedido == null) {
                itensDoPedidoSelecionado.clear();
                return;
            }
            loadItensDoPedido(pedido.getId());
        });
    }

    @Override
    public void populateFromModel() {
    }

    @Override
    public void clearForm() {
    }

    @Override
    public void handleAddOrUpdate() {
    }

    @Override
    public void handleClickMenuDelete() {
    }

    void loadPedidos() {
        Async.Run(() -> {
            try {
                var list = pedidoService.listar();
                UI.runOnUi(() -> pedidos.set(list));
            } catch (Exception e) {
                log.error("Erro ao carregar pedidos", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar pedidos: " + e.getMessage()));
            }
        });
    }

    private void loadItensDoPedido(Integer pedidoId) {
        Async.Run(() -> {
            try {
                var itens = pedidoItemService.listarPorPedido(pedidoId);
                UI.runOnUi(() -> itensDoPedidoSelecionado.set(itens));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar itens: " + e.getMessage()));
            }
        });
    }

    @Override
    public void onDestroy() throws Exception {
        this.pedidoItemService.close();
        this.pedidoService.close();
    }
}

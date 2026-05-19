package my_app.screens.pedidosScreen;

import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.repositories.PedidoItemRepository;
import my_app.db.repositories.PedidoRepository;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;

public class PedidosScreenViewModel extends ViewModelv2 {

    private final ScreenContext ctx;
    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;

    final ListState<PedidoModel> pedidos = ListState.ofEmpty();
    final ListState<PedidoItemModel> itensDoPedidoSelecionado = ListState.ofEmpty();
    final State<PedidoModel> pedidoSelecionado = State.of(null);

    public PedidosScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.pedidoRepository = new PedidoRepository();
        this.pedidoItemRepository = new PedidoItemRepository();
        this.onInit();
    }

    @Override
    protected void onInit() {
        pedidoSelecionado.subscribe(pedido -> {
            if (pedido == null) {
                itensDoPedidoSelecionado.clear();
                return;
            }
            loadItensDoPedido(pedido.id);
        });
    }

    void loadPedidos() {
        Async.Run(() -> {
            try {
                var list = pedidoRepository.listar();
                UI.runOnUi(() -> pedidos.set(list));
            } catch (Exception e) {
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar pedidos: " + e.getMessage()));
            }
        });
    }

    private void loadItensDoPedido(Long pedidoId) {
        Async.Run(() -> {
            try {
                var itens = pedidoItemRepository.listarPorPedido(pedidoId);
                UI.runOnUi(() -> itensDoPedidoSelecionado.set(itens));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar itens: " + e.getMessage()));
            }
        });
    }
}
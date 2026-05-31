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
import my_app.db.services.PedidoService;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.domain.components.Components;
import net.sf.persism.Session;

import java.sql.SQLException;

public class PedidosScreenViewModel extends ViewModelScreenContract {

    private final PedidoService pedidoService;
    private final PedidoItemRepository pedidoItemRepository;

    final ListState<PedidoModel> pedidos = ListState.ofEmpty();
    final ListState<PedidoItemModel> itensDoPedidoSelecionado = ListState.ofEmpty();
    final State<PedidoModel> pedidoSelecionado = State.of(null);

    public PedidosScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            Session session = getPersismSession();
            this.pedidoService = new PedidoService(session);
            this.pedidoItemRepository = new PedidoItemRepository(session);
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
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
                e.printStackTrace();
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar pedidos: " + e.getMessage()));
            }
        });
    }

    private void loadItensDoPedido(Integer pedidoId) {
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

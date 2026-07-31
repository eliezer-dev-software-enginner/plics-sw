package my_app.screens.pedidosScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.services.PDVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PedidosScreenViewModel extends ViewModelScreenContract<PedidoModel> {

    private static final Logger log = LoggerFactory.getLogger(PedidosScreenViewModel.class);

    private final PedidoService pedidoService;
    private final PedidoItemService pedidoItemService;
    private final ClienteService clienteService;
    private final PDVService pdvService;

    final megalodonte.v2.ListState<PedidoItemModel> itensDoPedidoSelecionado = megalodonte.v2.ListState.ofEmpty();
    final State<PedidoModel> pedidoSelecionado = State.of(null);
    final ComputedState<Boolean> temPedidoSelecionado = ComputedState.of(
            () -> pedidoSelecionado.get() != null, pedidoSelecionado);

    // Cache id -> nome, só pra exibir na tabela (evita N chamadas ao clicar em cada linha)
    private final Map<Integer, String> nomesClientes = new HashMap<>();

    public PedidosScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.pedidoService = createOrReport(PedidoService::new);
        this.pedidoItemService = createOrReport(PedidoItemService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.pdvService = createOrReport(PDVService::new);
        onInit();
    }

    @Override
    protected boolean matchesSearch(PedidoModel model, String query) {
        return contains(nomeClienteDoPedido(model), query)
                || contains(model.getFormaPagamento(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    String nomeClienteDoPedido(PedidoModel pedido) {
        if (pedido.getClienteId() == null) return "Consumidor";
        return nomesClientes.getOrDefault(pedido.getClienteId(), "Cliente #" + pedido.getClienteId());
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
    public void populateFieldsFromModel() {
    }

    // Tela somente-leitura, sem formulário de CRUD.
    @Override
    public PedidoModel populateModelFromFields() {
        return null;
    }

    @Override
    public void clearForm() {
    }

    @Override
    public void handleAddOrUpdate() {
    }

    @Override
    public void handleClickMenuDelete() {
        var pedido = pedidoSelecionado.get();
        if (pedido == null) return;

        var mensagem = "Deseja excluir a venda #" + pedido.getId() + " (" + nomeClienteDoPedido(pedido) + ")? "
                + "O estoque dos produtos será devolvido"
                + (pedido.getFiado() != null && pedido.getFiado() == 1 ? " e as contas a receber vinculadas serão apagadas." : ".");

        Components.ShowAlertAdvice(mensagem, () -> Async.Run(() -> {
            try {
                pdvService.excluirVenda(pedido.getId());
                UI.runOnUi(() -> {
                    allDataList.removeIf(it -> it.getId().equals(pedido.getId()));
                    pedidoSelecionado.set(null);
                    itensDoPedidoSelecionado.clear();
                    Components.ShowPopup(ctx, "Venda excluída com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                log.error("Erro ao excluir venda", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir venda: " + e.getMessage()));
            }
        }));
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var pedidos = pedidoService.listar();
                var clientesList = clienteService.listar();
                UI.runOnUi(() -> {
                    nomesClientes.clear();
                    clientesList.forEach(c -> nomesClientes.put(c.getId(), c.getNome()));
                    allDataList.set(pedidos);
                });
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
        this.clienteService.close();
    }
}

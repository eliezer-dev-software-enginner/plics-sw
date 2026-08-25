package my_app.screens.pedidosScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.db.models.ClienteModel;
import my_app.db.models.PedidoItemModel;
import my_app.db.models.PedidoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.EmpresaService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.db.services.PreferenciasService;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.services.EscPosPrinter;
import my_app.services.PDVService;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidosScreenViewModel extends ViewModelScreenContract<PedidoModel> {

    private static final Logger log = LoggerFactory.getLogger(PedidosScreenViewModel.class);

    private final PedidoService pedidoService;
    private final PedidoItemService pedidoItemService;
    private final ClienteService clienteService;
    private final PDVService pdvService;
    private final EmpresaService empresaService;
    private final EscPosPrinter escPosPrinter;

    final megalodonte.v2.ListState<PedidoItemModel> itensDoPedidoSelecionado = megalodonte.v2.ListState.ofEmpty();
    final State<PedidoModel> pedidoSelecionado = State.of(null);
    final ComputedState<Boolean> temPedidoSelecionado = ComputedState.of(
            () -> pedidoSelecionado.get() != null, pedidoSelecionado);
    final ComputedState<Boolean> podeDevolver = ComputedState.of(
            () -> pedidoSelecionado.get() != null && !Boolean.TRUE.equals(pedidoSelecionado.get().getDevolvida()),
            pedidoSelecionado);

    // Cache id -> nome, só pra exibir na tabela (evita N chamadas ao clicar em cada linha)
    private final Map<Integer, String> nomesClientes = new HashMap<>();

    public PedidosScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.pedidoService = createOrReport(PedidoService::new);
        this.pedidoItemService = createOrReport(PedidoItemService::new);
        this.clienteService = createOrReport(ClienteService::new);
        this.pdvService = createOrReport(PDVService::new);
        this.empresaService = createOrReport(EmpresaService::new);
        var porta = carregarPortaImpressora();
        this.escPosPrinter = porta != null ? new EscPosPrinter(empresaService, porta) : new EscPosPrinter(empresaService);
        onInit();
    }

    private String carregarPortaImpressora() {
        try (var prefsService = createOrReport(PreferenciasService::new)) {
            var prefs = prefsService.listar();
            if (!prefs.isEmpty()) {
                var port = prefs.getFirst().getPortaImpressora();
                if (port != null && !port.isBlank()) return port;
            }
        } catch (Exception e) {
            log.warn("Não foi possível carregar porta da impressora", e);
        }
        return null;
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

    public void handleClickMenuDevolucaoVenda() {
        var pedido = pedidoSelecionado.get();
        if (pedido == null || Boolean.TRUE.equals(pedido.getDevolvida())) return;

        var mensagem = "Confirma a devolução da venda #" + pedido.getId() + " (" + nomeClienteDoPedido(pedido) + ")? "
                + "O estoque dos produtos será restituído"
                + (pedido.getFiado() != null && pedido.getFiado() == 1 ? " e o pagamento vinculado será estornado." : ".");

        Components.ShowAlertAdvice(mensagem, () -> Async.Run(() -> {
            try {
                pdvService.devolverVenda(pedido.getId());
                var atualizado = pedidoService.buscarById(pedido.getId());
                UI.runOnUi(() -> {
                    allDataList.updateIf(it -> it.getId().equals(pedido.getId()), it -> atualizado);
                    pedidoSelecionado.set(atualizado);
                    Components.ShowPopup(ctx, "Venda devolvida com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                });
            } catch (Exception e) {
                log.error("Erro ao devolver venda", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao devolver venda: " + e.getMessage()));
            }
        }));
    }

    // Mesmo fluxo de impressão do PDVScreenViewModel.imprimirNota(), mas operando
    // sobre a venda selecionada no histórico em vez da última venda finalizada.
    void imprimirVendaSelecionada() {
        var pedido = pedidoSelecionado.get();
        if (pedido == null) {
            Components.ShowAlertError("Selecione uma venda para imprimir.");
            return;
        }

        Async.Run(() -> {
            try {
                var dados = carregarDadosNotaPedido(pedido);
                try {
                    escPosPrinter.imprimirNotaVenda(pedido, dados.itens(), dados.cliente(), dados.empresa(), dados.parcelas());
                    UI.runOnUi(() -> Components.ShowPopup(ctx, "Nota enviada para impressão!"));
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao imprimir: " + e.getMessage()));
                }
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar dados para impressão: " + e.getMessage()));
            }
        });
    }

    private record DadosNotaPedido(
            java.util.List<PedidoItemModel> itens,
            ClienteModel cliente,
            my_app.db.models.EmpresaModel empresa,
            java.util.List<my_app.db.models.ContaAreceberModel> parcelas
    ) {}

    private DadosNotaPedido carregarDadosNotaPedido(PedidoModel pedido) throws Exception {
        var itens = pedidoItemService.listarPorPedido(pedido.getId());
        var empresa = empresaService.buscarUnico();
        var clienteId = pedido.getClienteId();
        final ClienteModel cliente;
        if (clienteId != null) {
            cliente = clienteService.listar().stream()
                    .filter(c -> c.getId().equals(clienteId))
                    .findFirst()
                    .orElse(null);
        } else {
            cliente = null;
        }

        java.util.List<my_app.db.models.ContaAreceberModel> parcelas = null;
        if (pedido.getFiado() != null && pedido.getFiado() == 1) {
            try (var contaService = new my_app.db.services.ContaAreceberService()) {
                parcelas = contaService.buscarPorVenda(pedido.getId());
            }
        }

        return new DadosNotaPedido(itens, cliente, empresa, parcelas);
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
        this.empresaService.close();
    }
}

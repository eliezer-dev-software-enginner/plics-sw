package my_app.screens.pdvScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.v2.ListState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.Main;
import my_app.db.models.ClienteModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.EmpresaService;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PreferenciasService;
import my_app.db.services.ProdutoService;
import my_app.core.events.EntityEvent;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.domain.components.Components;
import my_app.services.EscPosPrinter;
import my_app.services.PDVService;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PDVScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(PDVScreenViewModel.class);
    private final ScreenContext ctx;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final PDVService pdvService;

    // Cache de todos os produtos (lookup por código)
    private final Map<String, ProdutoModel> produtosCache = new HashMap<>();
    final ListState<ProdutoModel> sugestoesProduto = ListState.ofEmpty();

    final ComputedState<Boolean> sugestoesProdutoVisible = ComputedState.of(
            () -> !sugestoesProduto.get().isEmpty(),
            sugestoesProduto
    );

    final State<ProdutoModel> produtoEncontrado = State.of(null);
    final ListState<ClienteModel> clientes = ListState.ofEmpty();

    final State<ClienteModel> clienteSelected = State.of(null);

    // Estado reativo: itens no carrinho
    final ListState<ItemVenda> itensCarrinho = ListState.ofEmpty();

    final State<Boolean> isVendaFiada = State.of(false);

    // Estado do campo de busca
    final State<String> codigoBarrasInput = State.of("");

    //item atual buscado
    final State<String> quantidadeInput = State.of("1");

    // Subtotal derivado — recalculado sempre que itensCarrinho mudar
    final State<String> subtotal = State.of("0");

    final State<String> totalRecebido = State.of("0");
    final State<String> troco         = State.of("0");

    final State<Boolean> isPrintNotaVendaVisible = State.of(false);

    private my_app.db.models.PedidoModel lastPedido;
    private final PedidoItemService pedidoItemService;
    private final EmpresaService empresaService;
    private final EscPosPrinter escPosPrinter;

    public PDVScreenViewModel(ScreenContext ctx) {
        this(ctx, createProdutoService(), createClienteService(), new PDVService());
    }

    public PDVScreenViewModel(ScreenContext ctx, ProdutoService produtoService, ClienteService clienteService, PDVService pdvService) {
        this.ctx = ctx;
        this.produtoService = produtoService;
        this.clienteService = clienteService;
        this.pdvService = pdvService;
        this.pedidoItemService = createPedidoItemService();
        this.empresaService = createEmpresaService();
        var porta = carregarPortaImpressora();
        this.escPosPrinter = Main.devMode ? EscPosPrinter.viaTcp("virtual-printer.online")
                : (porta != null ? new EscPosPrinter(empresaService, porta) : new EscPosPrinter(empresaService));
        this.onInit();
    }

    private static ProdutoService createProdutoService() {
        try {
            return new ProdutoService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ClienteService createClienteService() {
        try {
            return new ClienteService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static PedidoItemService createPedidoItemService() {
        try {
            return new PedidoItemService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String carregarPortaImpressora() {
        try {
            var prefsService = new PreferenciasService();
            var prefs = prefsService.listar();
            if (!prefs.isEmpty()) {
                var port = prefs.getFirst().getPortaImpressora();
                if (port != null && !port.isBlank()) return port;
            }
        } catch (SQLException e) {
            log.warn("Não foi possível carregar porta da impressora", e);
        }
        return null;
    }

    private static EmpresaService createEmpresaService() {
        try {
            return new EmpresaService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onInit() {
        itensCarrinho.onChange(itens -> {
            BigDecimal total = itens.stream()
                    .map(ItemVenda::totalItem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            subtotal.set(Utils.deRealParaCentavos(total));
        });

        totalRecebido.subscribe(recebido -> {
            try {
                BigDecimal recebidoBD = Utils.deCentavosParaReal(recebido);
                BigDecimal subtotalBD = Utils.deCentavosParaReal(subtotal.get());
                BigDecimal t = recebidoBD.subtract(subtotalBD);
                troco.set(t.compareTo(BigDecimal.ZERO) < 0 ? "0" : Utils.deRealParaCentavos(t));
            } catch (NumberFormatException e) {
                troco.set("0");
            }
        });

        EventBus.getInstance().subscribe(event -> {
            if(event instanceof EntityEvent<?> ee && ee.is(EntityEvent.EventType.CRIADO) && ee.entity() instanceof ClienteModel cm){
                clientes.add(cm);
            }
        });

        produtoEncontrado.subscribe(this::selecionarProduto);
        codigoBarrasInput.subscribe(this::filtrarProdutos);

    }

    void selecionarProduto(ProdutoModel produto) {
        if (produto != null) {
            codigoBarrasInput.set(produto.getCodigoBarras());
            //quantidadeRef.requestFocus();
        }
    }

    void filtrarProdutos(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            sugestoesProduto.clear();
            return;
        }

        var selected = produtoEncontrado.get();

        if (selected == null || !selected.getCodigoBarras().equals(termo.trim())) {
            produtoEncontrado.set(null);
        }

        if(selected != null) {
            IO.println(selected.getDescricao());
        }


        var filtrados = produtosCache.values().stream()
                .filter(p -> p.getCodigoBarras().contains(termo.trim())
                        || p.getDescricao().toLowerCase().contains(termo.trim().toLowerCase()))
                .limit(8)
                .toList();

        sugestoesProduto.set(filtrados);
    }

    void loadProdutos() {
        Async.Run(() -> {
            try {
                var list = produtoService.listar();

                System.out.println("=== loadProdutos ===");
                System.out.println("Lista retornada: " + list.size());
                if (!list.isEmpty()) {
                    System.out.println("Primeiro produto: " + list.getFirst().getDescricao());
                }

                UI.runOnUi(() -> {
                    produtosCache.clear();
                    list.forEach(p -> produtosCache.put(p.getCodigoBarras(), p));
                    System.out.println("Cache populado com: " + produtosCache.size() + " produtos");
                });
            } catch (Exception e) {
                log.error("Erro ao buscar produtos", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar produtos: " + e.getMessage()));
            }
        });
    }

    void adicionarPorCodigo(String codigo) {
        var produto = produtosCache.get(codigo.trim());
        if (produto == null) {
            Components.ShowAlertError("Produto não encontrado: " + codigo);
            return;
        }

        // Se já existe no carrinho, incrementa a qtd
        var existente = itensCarrinho.get().stream()
                .filter(i -> i.produto.getCodigoBarras().equals(codigo))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().quantidade = existente.get().quantidade.add(BigDecimal.ONE);
            itensCarrinho.refresh(); // notifica observers sem substituir a lista
        } else {
            var item = new ItemVenda(produto);
            item.quantidade = new BigDecimal(quantidadeInput.get());
            itensCarrinho.add(item);
        }

        codigoBarrasInput.set(""); // limpa o campo após adicionar
        quantidadeInput.set("1");
    }

    void atualizarQuantidade(ItemVenda item, BigDecimal novaQtd) {
        if (novaQtd.compareTo(BigDecimal.ZERO) <= 0) {
            itensCarrinho.remove(item);
        } else {
            item.quantidade = novaQtd;
            itensCarrinho.refresh();
        }
    }

    void removerItem(ItemVenda item) {
        itensCarrinho.remove(item);
    }

    void finalizarVenda() {
        if (itensCarrinho.isEmpty()) {
            Components.ShowAlertError("Carrinho vazio.");
            return;
        }

        boolean fiado = isVendaFiada.get();
        Integer clienteId = clienteSelected.get() != null ? clienteSelected.get().getId() : null;

        if (fiado && clienteId == null) {
            Components.ShowAlertError("Selecione um cliente para venda fiada.");
            return;
        }

        if (!fiado && clienteId == null) {
            clienteId = 1; // CLIENTE PADRÃO (inserido pela migration V16)
        }

        final Integer finalClienteId = clienteId;
        Async.Run(() -> {
            try {
                lastPedido = pdvService.finalizarVenda(
                        itensCarrinho.get(),
                        "A VISTA",
                        finalClienteId,
                        fiado
                );
                UI.runOnUi(() -> {
                    itensCarrinho.clear();
                    codigoBarrasInput.set("");
                    quantidadeInput.set("1");
                    subtotal.set("0");
                    totalRecebido.set("0");
                    troco.set("0");
                    clienteSelected.set(null);
                    isVendaFiada.set(false);
                    Components.ShowPopup(ctx, "Venda finalizada com sucesso!");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    isPrintNotaVendaVisible.set(true);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao finalizar venda: " + e.getMessage()));
            }
        });
    }

    void imprimirNota(){
        if (lastPedido == null) {
            Components.ShowAlertError("Nenhuma venda para imprimir.");
            return;
        }

        Async.Run(() -> {
            try {
                var itens = pedidoItemService.listarPorPedido(lastPedido.getId());
                var empresa = empresaService.buscarUnico();
                var clienteId = lastPedido.getClienteId();
                final ClienteModel cliente;
                if (clienteId != null) {
                    cliente = clienteService.listar().stream()
                            .filter(c -> c.getId().equals(clienteId))
                            .findFirst()
                            .orElse(null);
                } else {
                    cliente = null;
                }

                final var pedido = lastPedido;

                try {
                    escPosPrinter.imprimirNotaVenda(pedido, itens, cliente, empresa);
                    } catch (Exception e) {
                        UI.runOnUi(()->Components.ShowAlertError("Erro ao imprimir: " + e.getMessage()));
                    }

            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar dados para impressao: " + e.getMessage()));
            }
        });
    }

    void handleCriarCliente(){
        ctx.router().spawnWindow("clientes",e->{});
    }
}
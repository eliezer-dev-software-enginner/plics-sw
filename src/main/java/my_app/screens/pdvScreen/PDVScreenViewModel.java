package my_app.screens.pdvScreen;

import megalodonte.base.state.State;
import megalodonte.v2.ListState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ClienteModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ProdutoService;
import my_app.core.events.EntityEvent;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.domain.components.Components;
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

    final ListState<ClienteModel> clientes = ListState.ofEmpty();

    State<ClienteModel> clienteSelected = State.of(null);

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



    public PDVScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        try {
            this.produtoService = new ProdutoService();
            this.clienteService = new ClienteService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        pdvService = new PDVService();
        this.onInit();
    }

    protected void onInit() {
        // Sempre que o carrinho mudar, recalcula o subtotal
        itensCarrinho.onChange(itens -> {
            BigDecimal total = itens.stream()
                    .map(ItemVenda::totalItem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            subtotal.set(total.toPlainString());
        });

        totalRecebido.subscribe(recebido -> {
            try {
                BigDecimal recebidoBD = Utils.deCentavosParaReal(recebido);
                BigDecimal subtotalBD = new BigDecimal(subtotal.get());
                BigDecimal t = recebidoBD.subtract(subtotalBD);
                troco.set(t.compareTo(BigDecimal.ZERO) < 0 ? "0" : t.toPlainString());
            } catch (NumberFormatException e) {
                troco.set("0");
            }
        });

        EventBus.getInstance().subscribe(event -> {
            if(event instanceof EntityEvent<?> ee && ee.is(EntityEvent.EventType.CRIADO) && ee.entity() instanceof ClienteModel cm){
                clientes.add(cm);
            }
        });
    }

    void loadProdutos() {
        Async.Run(() -> {
            try {
                var list = produtoService.listar();

                System.out.println("=== loadProdutos ===");
                System.out.println("Lista retornada: " + list.size());
                if (!list.isEmpty()) {
                    System.out.println("Primeiro produto: " + list.get(0).getDescricao());
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

    void loadClientes() {
        Async.Run(() -> {
            try {
                var list = clienteService.listar();
               // UI.runOnUi(() -> clientes.set(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar clientes: " + e.getMessage()));
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

        Async.Run(() -> {
            try {
                pdvService.finalizarVenda(
                        itensCarrinho.get(),
                        "À VISTA", // TODO: adicionar seletor de forma de pagamento
                        Long.valueOf(clienteId),
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
                });
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao finalizar venda: " + e.getMessage()));
            }
        });
    }

    void handleCriarCliente(){
        ctx.router().spawnWindow("clientes",e->{});
    }
}
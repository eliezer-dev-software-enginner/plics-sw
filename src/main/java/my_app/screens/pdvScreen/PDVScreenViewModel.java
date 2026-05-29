package my_app.screens.pdvScreen;

import megalodonte.State;
import megalodonte.v2.ListState;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ClienteModel;
import my_app.db.models_old.ProdutoModel;
import my_app.db.repositories_old.ClienteRepository;
import my_app.db.repositories_old.ProdutoRepository;
import my_app.events.ClienteEvents;
import my_app.events.DadosFinanceirosAtualizadosEvent;
import my_app.events.EventBus;
import my_app.domain.components.Components;
import my_app.services.PDVService;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PDVScreenViewModel {

    private final ScreenContext ctx;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
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
        this.produtoRepository = new ProdutoRepository();
        this.clienteRepository = new ClienteRepository();
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
            if(event instanceof ClienteEvents.Criado(ClienteModel clienteModel)){
                clientes.add(clienteModel);
            }
        });
    }

    void loadProdutos() {
        Async.Run(() -> {
            try {
                var list = produtoRepository.listar();

                System.out.println("=== loadProdutos ===");
                System.out.println("Lista retornada: " + list.size());
                if (!list.isEmpty()) {
                    System.out.println("Primeiro produto: " + list.get(0).descricao);
                }

                UI.runOnUi(() -> {
                    produtosCache.clear();
                    list.forEach(p -> produtosCache.put(p.codigoBarras, p));
                    System.out.println("Cache populado com: " + produtosCache.size() + " produtos");
                });
            } catch (Exception e) {
                e.printStackTrace(); // <- isso é importante, pode estar engolindo exceção
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar produtos: " + e.getMessage()));
            }
        });
    }

    void loadClientes() {
        Async.Run(() -> {
            try {
                var list = clienteRepository.listar();
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
                .filter(i -> i.produto.codigoBarras.equals(codigo))
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
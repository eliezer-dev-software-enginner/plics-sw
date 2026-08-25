package my_app.screens.pedidosScreen.details;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.core.events.DadosFinanceirosAtualizadosEvent;
import my_app.core.events.EventBus;
import my_app.db.models.PedidoModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.PedidoItemService;
import my_app.db.services.PedidoService;
import my_app.db.services.ProdutoService;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.screens.pdvScreen.ItemVenda;
import my_app.services.PDVService;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PedidoTrocaViewModel extends ViewModelScreenContract<PedidoModel> {

    private static final Logger log = LoggerFactory.getLogger(PedidoTrocaViewModel.class);

    private final PedidoService pedidoService;
    private final PedidoItemService pedidoItemService;
    private final ProdutoService produtoService;
    private final PDVService pdvService;

    final State<PedidoModel> pedidoSelecionado = State.of(null);

    // Troca states
    private final Map<String, ProdutoModel> produtosCacheTroca = new HashMap<>();
    final State<ItemVenda> trocaItemSelected = State.of(null);
    final State<Boolean> trocaSearchVisible = State.of(false);
    final State<Boolean> trocaShowPesquisarBtn = State.of(false);
    final State<Boolean> trocaShowTrocarBtn = State.of(false);
    final megalodonte.v2.ListState<ProdutoModel> trocaSugestoes = megalodonte.v2.ListState.ofEmpty();
    final ComputedState<Boolean> trocaSugestoesVisiveis = ComputedState.of(
            () -> !trocaSugestoes.get().isEmpty(), trocaSugestoes);
    final State<ProdutoModel> trocaProdutoEncontrado = State.of(null);
    final State<String> trocaBuscaInput = State.of("");
    final State<String> trocaQuantidadeInput = State.of("1");
    final megalodonte.v2.ListState<ItemVenda> trocaItens = megalodonte.v2.ListState.ofEmpty();
    final State<String> trocaFormaPagamento = State.of(Data.tiposPagamentoList.getFirst());
    private final Set<String> trocaOriginaisCodigos = new HashSet<>();

    public PedidoTrocaViewModel(ScreenContext ctx) {
        super(ctx);
        this.pedidoService = createOrReport(PedidoService::new);
        this.pedidoItemService = createOrReport(PedidoItemService::new);
        this.produtoService = createOrReport(ProdutoService::new);
        this.pdvService = createOrReport(PDVService::new);
        onInit();
    }

    @Override
    protected void onInit() {
        trocaBuscaInput.subscribe(this::filtrarProdutosTroca);

        trocaItemSelected.subscribe(item -> {
            trocaSearchVisible.set(item != null);
            trocaShowPesquisarBtn.set(item != null);
            if (item != null) {
                trocaBuscaInput.set("");
                trocaQuantidadeInput.set("1");
                trocaProdutoEncontrado.set(null);
                trocaSugestoes.clear();
            }
        });

        trocaProdutoEncontrado.subscribe(produto -> {
            if (produto != null) {
                var item = trocaItemSelected.get();
                if (item != null) {
                    if (trocaItens.get().stream().anyMatch(i ->
                            i.produto.getCodigoBarras().equals(produto.getCodigoBarras())
                                    && !i.equals(item))) {
                        Components.ShowAlertError("Este produto já está na lista.");
                        trocaProdutoEncontrado.set(null);
                        return;
                    }
                    trocaShowPesquisarBtn.set(false);
                    trocaShowTrocarBtn.set(true);
                }
                trocaBuscaInput.set(produto.getCodigoBarras());
                trocaSugestoes.clear();
            }
        });
    }

    @Override
    public void populateFieldsFromModel() {
    }

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
    }

    @Override
    public void fetchListData() {
    }

    @Override
    protected boolean matchesSearch(PedidoModel model, String query) {
        return false;
    }

    public void preparar(Integer pedidoId) {
        Async.Run(() -> {
            try {
                var pedido = pedidoService.buscarById(pedidoId);
                if (pedido == null || Boolean.TRUE.equals(pedido.getDevolvida())) return;

                var produtos = produtoService.listar();
                var itensOriginais = pedidoItemService.listarPorPedido(pedidoId);

                UI.runOnUi(() -> {
                    pedidoSelecionado.set(pedido);

                    produtosCacheTroca.clear();
                    produtos.forEach(p -> produtosCacheTroca.put(p.getCodigoBarras(), p));

                    trocaOriginaisCodigos.clear();
                    var itens = new ArrayList<ItemVenda>();
                    for (var item : itensOriginais) {
                        var produto = produtosCacheTroca.get(item.getProdutoCod());
                        if (produto != null) {
                            var iv = new ItemVenda(produto);
                            iv.quantidade = item.getQuantidade();
                            itens.add(iv);
                            trocaOriginaisCodigos.add(item.getProdutoCod());
                        }
                    }
                    trocaItens.set(itens);

                    trocaItemSelected.set(null);
                    trocaSearchVisible.set(false);
                    trocaShowPesquisarBtn.set(false);
                    trocaShowTrocarBtn.set(false);
                    trocaBuscaInput.set("");
                    trocaQuantidadeInput.set("1");
                    trocaProdutoEncontrado.set(null);
                    trocaSugestoes.clear();
                    trocaFormaPagamento.set(pedido.getFormaPagamento() != null
                            && Data.tiposPagamentoList.contains(pedido.getFormaPagamento())
                            ? pedido.getFormaPagamento()
                            : Data.tiposPagamentoList.getFirst());
                });
            } catch (Exception e) {
                log.error("Erro ao carregar dados para troca", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar dados: " + e.getMessage()));
            }
        });
    }

    private void filtrarProdutosTroca(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            trocaSugestoes.clear();
            return;
        }

        var selected = trocaProdutoEncontrado.get();
        if (selected != null && !selected.getCodigoBarras().equals(termo.trim())) {
            trocaProdutoEncontrado.set(null);
        }

        var filtrados = produtosCacheTroca.values().stream()
                .filter(p -> p.getCodigoBarras().contains(termo.trim())
                        || (p.getDescricao() != null
                            && p.getDescricao().toLowerCase().contains(termo.trim().toLowerCase())))
                .limit(8)
                .toList();

        trocaSugestoes.set(filtrados);
    }

    BigDecimal totalItensTroca() {
        return trocaItens.get().stream()
                .map(ItemVenda::totalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void handleTrocaPesquisarClick() {
        var produto = trocaProdutoEncontrado.get();
        var item = trocaItemSelected.get();
        if (produto == null || item == null) return;

        var list = new ArrayList<>(trocaItens.get());
        int idx = list.indexOf(item);
        if (idx >= 0) {
            var newItem = new ItemVenda(produto);
            newItem.quantidade = item.quantidade;
            list.set(idx, newItem);
            trocaItens.set(list);
        }
        trocaItemSelected.set(null);
        trocaProdutoEncontrado.set(null);
        trocaBuscaInput.set("");
        trocaSugestoes.clear();
    }

    void updateTrocaBtnStates() {
        var item = trocaItemSelected.get();
        var produto = trocaProdutoEncontrado.get();
        trocaShowPesquisarBtn.set(item != null && produto == null);
        trocaShowTrocarBtn.set(item != null && produto != null);
    }

    void confirmarTroca(Runnable onSuccess) {
        var pedido = pedidoSelecionado.get();
        if (pedido == null || Boolean.TRUE.equals(pedido.getDevolvida())) return;

        if (trocaItens.get().isEmpty()) {
            Components.ShowAlertError("Adicione pelo menos um produto para a troca.");
            return;
        }

        boolean algumItemDiferente = trocaItens.get().stream()
                .anyMatch(item -> !trocaOriginaisCodigos.contains(item.produto.getCodigoBarras()));
        if (!algumItemDiferente) {
            Components.ShowAlertError("A troca deve alterar pelo menos um produto em relação à venda original.");
            return;
        }

        var mensagem = "Confirma a troca da venda #" + pedido.getId() + "? "
                + "Os produtos originais voltam ao estoque e a venda será registrada como devolvida, "
                + "criando uma nova venda no total de " + Utils.toBRLCurrency(totalItensTroca()) + ".";

        Components.ShowAlertAdvice(mensagem, () -> Async.Run(() -> {
            try {
                var novoPedido = pdvService.trocarVenda(pedido.getId(), List.copyOf(trocaItens.get()),
                        trocaFormaPagamento.get());
                UI.runOnUi(() -> {
                    Components.ShowPopup(ctx, "Troca realizada! Nova venda #" + novoPedido.getId() + " criada.");
                    EventBus.getInstance().publish(DadosFinanceirosAtualizadosEvent.getInstance());
                    onSuccess.run();
                });
            } catch (Exception e) {
                log.error("Erro ao realizar troca da venda #" + pedido.getId(), e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao realizar troca: " + e.getMessage()));
            }
        }));
    }

    @Override
    public void onDestroy() throws Exception {
        this.pedidoService.close();
        this.pedidoItemService.close();
        this.produtoService.close();
    }
}

package my_app.screens.pedidosScreen.details;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;
import my_app.screens.pdvScreen.ItemVenda;
import pack.utilities.CurrencyPack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Order exchange layout and actions are declared in JSONC. */
public final class PedidoDetails implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(PedidoDetails.class, "/views/pedido-troca.view.jsonc");
    private static final Gson JSON = new Gson();
    private final ScreenContextInterface ctx;
    private final PedidoTrocaViewModel vm;
    private final Long pedidoId;
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public PedidoDetails(ScreenContextInterface ctx) {
        this.ctx = ctx;
        vm = new PedidoTrocaViewModel(ctx);
        pedidoId = Long.parseLong(ctx.getParams().get("id"));
        session = VIEW.open(new Actions()
                .register("selectItem", ignored -> selectItem())
                .register("resetSearch", ignored -> resetSearch())
                .register("replace", ignored -> replace())
                .register("confirm", ignored -> vm.confirmarTroca(() -> ctx.selfStage().close())));
        session.states().string("title").set("Trocar venda #" + pedidoId);
        session.bind("search", vm.trocaBuscaInput, String.class);
        session.bind("payment", vm.trocaFormaPagamento, String.class);
        session.display("searchVisible", vm.trocaSearchVisible, Boolean.class);
        session.display("showSearchButton", vm.trocaShowPesquisarBtn, Boolean.class);
        session.display("showReplaceButton", vm.trocaShowTrocarBtn, Boolean.class);
        session.states().string("paymentOptions").set(JSON.toJson(Data.tiposPagamentoList));
        subscriptions.add(vm.trocaItens.observe(items -> session.states().string("items").set(itemsJson(items))));
        subscriptions.add(vm.trocaSugestoes.observe(this::updateOptions));
        subscriptions.add(session.states().string("selectedProduct").observe(this::selectProduct));
    }

    private void selectItem() {
        var selected = session.states().string("selectedItem").get();
        ItemVenda item = null;
        if (selected != null && !selected.isBlank()) {
            try {
                String code = JsonParser.parseString(selected).getAsJsonObject().get("codigo").getAsString();
                item = vm.trocaItens.get().stream().filter(candidate -> candidate.produto.getCodigoBarras().equals(code))
                        .findFirst().orElse(null);
            } catch (RuntimeException ignored) { }
        }
        vm.trocaItemSelected.set(item);
    }

    private void updateOptions(List<my_app.core.db.models.ProdutoModel> products) {
        var options = new ArrayList<Map<String, String>>(products.size());
        for (var product : products) options.add(Map.of("value", product.getCodigoBarras(),
                "label", product.getDescricao() + " — " + product.getCodigoBarras()));
        session.states().string("productOptions").set(JSON.toJson(options));
    }

    private void selectProduct(String code) {
        if (code == null || code.isBlank()) return;
        vm.trocaSugestoes.get().stream().filter(product -> product.getCodigoBarras().equals(code))
                .findFirst().ifPresent(vm.trocaProdutoEncontrado::set);
    }

    private void resetSearch() {
        vm.trocaBuscaInput.set("");
        vm.trocaQuantidadeInput.set("1");
        vm.trocaProdutoEncontrado.set(null);
        session.states().string("selectedProduct").set("");
        vm.updateTrocaBtnStates();
    }

    private void replace() {
        vm.handleTrocaPesquisarClick();
        session.states().string("selectedProduct").set("");
        vm.updateTrocaBtnStates();
    }

    private String itemsJson(List<ItemVenda> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("codigo", item.produto.getCodigoBarras());
            row.put("nome", item.produto.getDescricao());
            row.put("cor", item.produto.getCor() == null ? "" : item.produto.getCor());
            row.put("tamanho", item.produto.getTamanho() == null ? "" : item.produto.getTamanho());
            row.put("quantidade", item.quantidade.toPlainString());
            row.put("precoUnitario", CurrencyPack.toBRLCurrency(item.produto.getPrecoVenda()));
            row.put("total", CurrencyPack.toBRLCurrency(item.totalItem()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { vm.preparar(pedidoId); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        try { vm.onDestroy(); }
        catch (Exception error) { throw new IllegalStateException("Could not close exchange services", error); }
    }
}

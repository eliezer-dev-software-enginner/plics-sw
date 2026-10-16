package my_app.screens.pedidosScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.AppRoutes;
import my_app.core.db.models.PedidoItemModel;
import my_app.core.db.models.PedidoModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Order history layout and actions are declared in JSONC. */
public final class PedidosScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(PedidosScreen.class, "/views/pedidos.view.jsonc");
    private static final Gson JSON = new Gson();
    private final ScreenContextInterface ctx;
    private final PedidosScreenViewModel vm;
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public PedidosScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        vm = new PedidosScreenViewModel(ctx);
        session = VIEW.open(new Actions()
                .register("select", ignored -> selectOrder())
                .register("print", ignored -> vm.imprimirVendaSelecionada())
                .register("delete", ignored -> vm.handleClickMenuDelete())
                .register("return", ignored -> vm.handleClickMenuDevolucaoVenda())
                .register("exchange", ignored -> handleClickMenuTroca()));
        session.bind("search", vm.searchState, String.class);
        session.display("hasSelection", vm.temPedidoSelecionado, Boolean.class);
        session.display("canReturn", vm.podeDevolver, Boolean.class);
        subscriptions.add(vm.filteredList.observe(items -> session.states().string("orders").set(ordersJson(items))));
        subscriptions.add(vm.itensDoPedidoSelecionado.observe(items -> session.states().string("items").set(itemsJson(items))));
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { vm.fetchListData(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        try { vm.onDestroy(); }
        catch (Exception error) { throw new IllegalStateException("Could not close order services", error); }
    }

    private void selectOrder() {
        var selected = session.states().string("selectedOrder").get();
        PedidoModel order = null;
        if (!selected.isBlank()) {
            try {
                long id = JsonParser.parseString(selected).getAsJsonObject().get("id").getAsLong();
                order = vm.filteredList.get().stream().filter(item -> item.getId() == id).findFirst().orElse(null);
            } catch (RuntimeException ignored) { }
        }
        vm.selected.set(order);
        vm.focusState.set(order != null);
        vm.pedidoSelecionado.set(order);
    }

    private String ordersJson(List<PedidoModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("id", String.valueOf(item.getId()));
            row.put("cliente", vm.nomeClienteDoPedido(item));
            row.put("total", CurrencyPack.toBRLCurrency(item.getTotalLiquido()));
            row.put("pagamento", value(item.getFormaPagamento()));
            row.put("fiado", item.getFiado() != null && item.getFiado() == 1 ? "Sim" : "Não");
            row.put("desconto", CurrencyPack.toBRLCurrency(item.getDesconto()));
            row.put("frete", CurrencyPack.toBRLCurrency(item.getFrete()));
            row.put("observacao", value(item.getObservacao()));
            row.put("data", DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            row.put("status", Boolean.TRUE.equals(item.getDevolvida()) ? "Devolvida" : "-");
            row.put("devolvidaEm", Boolean.TRUE.equals(item.getDevolvida()) && item.getDataDevolucao() != null
                    ? DatePack.millisToBrazilianDateTime(item.getDataDevolucao()) : "-");
            rows.add(row);
        }
        return JSON.toJson(rows);
    }

    private String itemsJson(List<PedidoItemModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("produto", value(item.getProdutoCod()));
            row.put("quantidade", value(item.getQuantidade()));
            row.put("precoUnitario", CurrencyPack.toBRLCurrency(item.getPrecoUnitario()));
            row.put("desconto", CurrencyPack.toBRLCurrency(item.getDesconto()));
            row.put("total", CurrencyPack.toBRLCurrency(item.getTotalItem()));
            row.put("data", item.getDataCriacao() == null ? "" : DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }

    private static String value(Object value) { return value == null ? "" : String.valueOf(value); }

    private void handleClickMenuTroca() {
        var pedido = vm.pedidoSelecionado.get();
        if (pedido == null) return;
        ctx.spawnWindow(AppRoutes.Screens.PEDIDO_DETAILS.name() + "/" + pedido.getId());
    }
}

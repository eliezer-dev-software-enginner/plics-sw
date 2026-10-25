package my_app.screens.pdvScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.models.ProdutoModel;
import pack.utilities.CurrencyPack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Point-of-sale layout and actions are declared in JSONC. */
public final class PDVScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(PDVScreen.class, "/views/pdv.view.jsonc");
    private static final Gson JSON = new Gson();
    private final PDVScreenViewModel vm;
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public PDVScreen(ScreenContextInterface ctx) {
        vm = new PDVScreenViewModel(ctx);
        session = VIEW.open(new Actions()
                .register("addProduct", ignored -> addProduct())
                .register("selectCart", ignored -> selectCart())
                .register("updateQuantity", ignored -> updateQuantity())
                .register("removeItem", ignored -> removeItem())
                .register("createCustomer", ignored -> vm.handleCriarCliente())
                .register("finish", ignored -> vm.finalizarVenda())
                .register("print", ignored -> vm.imprimirNota())
                .register("printAlternative", ignored -> vm.imprimirNotaAlternativa()));
        session.bind("productSearch", vm.codigoBarrasInput, String.class);
        session.bind("quantity", vm.quantidadeInput, String.class);
        session.bind("totalRecebido", vm.totalRecebido, String.class);
        session.bind("desconto", vm.desconto, String.class);
        session.bind("frete", vm.frete, String.class);
        session.bind("formaPagamento", vm.formaPagamentoSelecionado, String.class);
        session.bind("numeroParcelas", vm.numeroParcelas, String.class);
        session.display("installmentsVisible", vm.tipoPagamentoIsAPrazo, Boolean.class);
        session.states().string("paymentOptions").set(JSON.toJson(Data.tiposPagamentoList));
        watchMoney("subtotal", vm.subtotal);
        watchMoney("totalAPagar", vm.totalAPagar);
        watchMoney("troco", vm.troco);
        subscriptions.add(vm.sugestoesProduto.observe(this::updateProductOptions));
        subscriptions.add(vm.itensCarrinho.observe(items -> {
            session.states().string("cart").set(cartJson(items));
            selectCart();
        }));
        subscriptions.add(session.states().string("selectedCart").observe(ignored -> selectCart()));
        subscriptions.add(vm.clientes.observe(this::updateClientOptions));
        subscriptions.add(vm.clienteSelected.observe(cliente -> session.states().string("cliente").set(
                cliente == null ? "" : String.valueOf(cliente.getId()))));
        subscriptions.add(session.states().string("cliente").observe(this::selectClient));
    }

    private void watchMoney(String name, State<String> cents) {
        subscriptions.add(cents.observe(value -> session.states().string(name).set(
                CurrencyPack.toBRLCurrency(CurrencyPack.deCentavosParaReal(value == null || value.isBlank() ? "0" : value)))));
    }

    private void updateProductOptions(List<ProdutoModel> products) {
        var options = new ArrayList<Map<String, String>>(products.size());
        for (var product : products) options.add(Map.of("value", product.getCodigoBarras(),
                "label", product.getDescricao() + " — " + product.getCodigoBarras()));
        session.states().string("productOptions").set(JSON.toJson(options));
    }

    private void updateClientOptions(List<ClienteModel> clients) {
        var options = new ArrayList<Map<String, String>>(clients.size());
        for (var client : clients) options.add(Map.of("value", String.valueOf(client.getId()),
                "label", client.getNome() == null ? "Cliente #" + client.getId() : client.getNome()));
        session.states().string("clientOptions").set(JSON.toJson(options));
    }

    private void selectClient(String id) {
        if (id == null || id.isBlank()) return;
        vm.clientes.get().stream().filter(client -> String.valueOf(client.getId()).equals(id))
                .findFirst().ifPresent(client -> {
                    if (vm.clienteSelected.get() != client) vm.clienteSelected.set(client);
                });
    }

    private void addProduct() {
        var selected = session.states().string("selectedProduct").get();
        var code = selected.isBlank() ? vm.codigoBarrasInput.get() : selected;
        vm.adicionarPorCodigo(code);
        session.states().string("selectedProduct").set("");
    }

    private ItemVenda selectedCartItem() {
        var selected = session.states().string("selectedCart").get();
        if (selected == null || selected.isBlank()) return null;
        try {
            var code = JsonParser.parseString(selected).getAsJsonObject().get("codigo").getAsString();
            return vm.itensCarrinho.get().stream().filter(item -> item.produto.getCodigoBarras().equals(code))
                    .findFirst().orElse(null);
        } catch (RuntimeException error) { return null; }
    }

    private void selectCart() {
        var item = selectedCartItem();
        session.states().state("selectedCartVisible", Boolean.class).set(item != null);
        session.states().string("editQuantity").set(item == null ? "" : item.quantidade.toPlainString());
    }

    private void updateQuantity() {
        var item = selectedCartItem();
        if (item == null) return;
        try { vm.atualizarQuantidade(item, new BigDecimal(session.states().string("editQuantity").get().trim())); }
        catch (NumberFormatException error) { my_app.core.components.Components.ShowAlertError("Quantidade inválida."); }
    }

    private void removeItem() {
        var item = selectedCartItem();
        if (item != null) {
            vm.removerItem(item);
            session.states().string("selectedCart").set("");
        }
    }

    private String cartJson(List<ItemVenda> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("imagem", item.produto.getImagem() == null ? "" : item.produto.getImagem());
            row.put("codigo", item.produto.getCodigoBarras());
            row.put("nome", item.produto.getDescricao());
            row.put("quantidade", item.quantidade.toPlainString());
            row.put("precoUnitario", CurrencyPack.toBRLCurrency(item.produto.getPrecoVenda()));
            row.put("total", CurrencyPack.toBRLCurrency(item.totalItem()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { vm.loadProdutos(); vm.loadClientes(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        try { vm.onDestroy(); }
        catch (Exception error) { throw new IllegalStateException("Could not close point-of-sale services", error); }
    }
}

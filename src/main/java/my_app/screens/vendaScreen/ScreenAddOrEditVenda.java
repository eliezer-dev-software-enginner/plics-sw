package my_app.screens.vendaScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;
import my_app.core.Parcela;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.models.VendaModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.VendaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Sales form layout and installment controls are declared in JSONC. */
public final class ScreenAddOrEditVenda extends ScreenAddOrEdit<VendaModel, VendaMercadoriaScreenViewModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(ScreenAddOrEditVenda.class, "/views/venda-form.view.jsonc");
    private static final Gson JSON = new Gson();
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public ScreenAddOrEditVenda(ScreenContextInterface screenContext) {
        super(screenContext);
        session = VIEW.open(new Actions()
                .register("save", ignored -> handleAddOrUpdate())
                .register("selectProduct", ignored -> selectProduct())
                .register("generateInstallments", ignored -> generateInstallments()));
        bind("codigo", viewModel.codigo);
        bind("numeroNota", viewModel.numeroNota);
        bind("qtd", viewModel.qtd);
        bind("pcVenda", viewModel.pcVenda);
        bind("desconto", viewModel.descontoEmDinheiro);
        bind("frete", viewModel.frete);
        bind("tipoPagamento", viewModel.tipoPagamentoSelecionado);
        bind("afetaEstoque", viewModel.opcaoEstoqueSelected);
        bind("observacao", viewModel.observacao);
        session.display("btnText", getBtnActionText(), String.class);
        session.display("installmentsVisible", viewModel.tipoPagamentoIsAPrazo, Boolean.class);
        session.states().string("paymentOptions").set(JSON.toJson(Data.tiposPagamentoList));
        session.states().string("yesNoOptions").set(JSON.toJson(Data.simNaoList));
        bindDate("dataVenda", viewModel.dataVenda);
        subscriptions.add(viewModel.sugestoesProduto.observe(products -> session.states().string("suggestions").set(JSON.toJson(
                products.stream().map(product -> Map.of("codigo", product.getCodigoBarras(), "descricao", product.getDescricao())).toList()))));
        subscriptions.add(viewModel.clientes.observe(clients -> {
            session.states().string("clientOptions").set(JSON.toJson(clients.stream().map(this::clientLabel).toList()));
            syncClient();
        }));
        subscriptions.add(viewModel.clienteSelected.observe(ignored -> syncClient()));
        subscriptions.add(session.states().string("cliente").observe(label -> viewModel.clientes.get().stream()
                .filter(client -> clientLabel(client).equals(label)).findFirst()
                .ifPresent(client -> {
                    if (!Objects.equals(viewModel.clienteSelected.get(), client)) viewModel.clienteSelected.set(client);
                })));
        subscriptions.add(viewModel.totais.totalBruto.observe(v -> session.states().string("totalBruto").set(v)));
        subscriptions.add(viewModel.totais.descontoComputed.observe(v -> session.states().string("descontoExibido").set(v)));
        subscriptions.add(viewModel.totalComFrete.observe(v -> session.states().string("totalComFrete").set(
                CurrencyPack.toBRLCurrency(new java.math.BigDecimal(v)))));
        subscriptions.add(viewModel.estoqueAnterior.observe(v -> session.states().string("estoqueAnterior").set(v)));
        subscriptions.add(viewModel.estoqueAtual.observe(v -> session.states().string("estoqueAtual").set(v)));
        subscriptions.add(viewModel.parcelas.observe(this::updateInstallments));
        session.states().string("primeiroVencimento").set(LocalDate.now().plusMonths(1).minusDays(1).toString());
        session.states().string("quantidadeParcelas").set("1");
        viewModel.fetchListData("add".equals(type));
    }

    private void bind(String name, State<String> state) { session.bind(name, state, String.class); }

    private void bindDate(String name, State<LocalDate> model) {
        var view = session.states().string(name);
        subscriptions.add(model.observe(date -> {
            String iso = date == null ? "" : date.toString();
            if (!Objects.equals(view.get(), iso)) view.set(iso);
        }));
        subscriptions.add(view.observe(iso -> {
            LocalDate date = iso == null || iso.isBlank() ? null : LocalDate.parse(iso);
            if (!Objects.equals(model.get(), date)) model.set(date);
        }));
    }

    private String clientLabel(ClienteModel client) { return client.getId() + " · " + client.getNome(); }
    private void syncClient() {
        var client = viewModel.clienteSelected.get();
        String label = client == null ? "" : clientLabel(client);
        if (!Objects.equals(session.states().string("cliente").get(), label)) session.states().string("cliente").set(label);
    }

    private void selectProduct() {
        String selected = session.states().string("selectedSuggestion").get();
        if (selected.isBlank()) return;
        try {
            String barcode = JsonParser.parseString(selected).getAsJsonObject().get("codigo").getAsString();
            viewModel.sugestoesProduto.get().stream().filter(product -> barcode.equals(product.getCodigoBarras()))
                    .findFirst().ifPresent(viewModel.produtoEncontrado::set);
        } catch (RuntimeException ignored) { /* A stale selection is harmless. */ }
    }

    private void generateInstallments() {
        try {
            int count = Integer.parseInt(session.states().string("quantidadeParcelas").get());
            LocalDate first = LocalDate.parse(session.states().string("primeiroVencimento").get());
            if (count < 1 || count > 120) throw new IllegalArgumentException("Use de 1 a 120 parcelas.");
            var total = new java.math.BigDecimal(viewModel.totalComFrete.get());
            viewModel.parcelas.set(Parcela.gerarParcelas(first, count, total));
        } catch (RuntimeException e) {
            Components.ShowAlertError("Não foi possível gerar parcelas: " + e.getMessage());
        }
    }

    private void updateInstallments(List<Parcela> installments) {
        var rows = new ArrayList<Map<String, String>>();
        for (var installment : installments) rows.add(Map.of(
                "numero", String.valueOf(installment.numero()),
                "vencimento", DatePack.millisParaLocalDate(installment.dataVencimento()).toString(),
                "valor", CurrencyPack.toBRLCurrency(installment.valor())));
        session.states().string("installments").set(JSON.toJson(rows));
    }

    @Override public Component render() { return session.render(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        super.onDestroy();
    }
    @Override protected VendaMercadoriaScreenViewModel getViewModel(ScreenContextInterface ctx) { return new VendaMercadoriaScreenViewModel(ctx); }
    @Override protected BaseService<VendaModel> getService() throws SQLException { return new VendaService(); }
    @Override protected String getTitle() { return "venda"; }
    @Override protected Logger getLogger() { return LoggerFactory.getLogger(ScreenAddOrEditVenda.class); }
}

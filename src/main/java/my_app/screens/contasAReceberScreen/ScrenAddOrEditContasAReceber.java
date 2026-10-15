package my_app.screens.contasAReceberScreen;

import com.google.gson.Gson;
import megalodonte.base.components.Component;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.ScreenAddOrEdit;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.models.ContaAreceberModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ContaAreceberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Receivable form and payment actions rendered from a JSONC view. */
public final class ScrenAddOrEditContasAReceber extends ScreenAddOrEdit<ContaAreceberModel, ContasAReceberScreenViewModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(ScrenAddOrEditContasAReceber.class, "/views/conta-a-receber-form.view.jsonc");
    private static final Gson JSON = new Gson();
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public ScrenAddOrEditContasAReceber(ScreenContextInterface screenContext) {
        super(screenContext);
        session = VIEW.open(new Actions()
                .register("save", ignored -> handleAddOrUpdate())
                .register("startReceipt", ignored -> viewModel.modoRecebimento.set(true))
                .register("registerReceipt", ignored -> viewModel.registrarRecebimento(screenContext))
                .register("cancelReceipt", ignored -> {
                    viewModel.modoRecebimento.set(false);
                    viewModel.valorRecebimento.set("0");
                })
                .register("settle", ignored -> viewModel.quitarConta(screenContext)));
        session.bind("descricao", viewModel.descricao, String.class);
        session.bind("valorOriginal", viewModel.valorOriginal, String.class);
        session.bind("valorRecebimento", viewModel.valorRecebimento, String.class);
        session.bind("status", viewModel.status, String.class);
        session.bind("tipoDocumento", viewModel.tipoDocumento, String.class);
        session.bind("numeroDocumento", viewModel.numeroDocumento, String.class);
        session.bind("observacao", viewModel.observacao, String.class);
        session.display("btnText", getBtnActionText(), String.class);
        session.display("receiptVisible", viewModel.modoRecebimento, Boolean.class);
        session.states().set("editVisible", isEdit());
        session.states().string("statusOptions").set(JSON.toJson(viewModel.statusOptions));
        session.states().string("documentOptions").set(JSON.toJson(viewModel.tipoDocumentoOptions));
        bindDate("dataVencimento", viewModel.dataVencimento);
        bindDate("dataRecebimento", viewModel.dataRecebimento);
        var clientId = session.states().string("clienteId");
        subscriptions.add(viewModel.clientes.observe(list -> {
            session.states().string("clientOptions").set(JSON.toJson(list.stream().map(this::clientLabel).toList()));
            syncSelectedClient();
        }));
        subscriptions.add(viewModel.clienteSelected.observe(ignored -> syncSelectedClient()));
        subscriptions.add(clientId.observe(label -> viewModel.clientes.get().stream()
                .filter(client -> clientLabel(client).equals(label))
                .findFirst().ifPresent(client -> {
                    if (!Objects.equals(viewModel.clienteSelected.get(), client)) viewModel.clienteSelected.set(client);
                })));
        session.states().string("totalOpen").set(CurrencyPack.toBRLCurrency(viewModel.getTotalEmAberto()));
        session.states().string("totalOverdue").set(CurrencyPack.toBRLCurrency(viewModel.getTotalVencidas()));
    }

    private String clientLabel(ClienteModel client) { return client.getId() + " · " + client.getNome(); }

    private void syncSelectedClient() {
        var client = viewModel.clienteSelected.get();
        String label = client == null ? "" : clientLabel(client);
        if (!Objects.equals(session.states().string("clienteId").get(), label)) session.states().string("clienteId").set(label);
    }

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

    @Override public Component render() { return session.render(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        super.onDestroy();
    }
    @Override protected ContasAReceberScreenViewModel getViewModel(ScreenContextInterface ctx) { return new ContasAReceberScreenViewModel(ctx); }
    @Override protected BaseService<ContaAreceberModel> getService() throws SQLException { return new ContaAreceberService(); }
    @Override protected String getTitle() { return "conta a receber"; }
    @Override protected Logger getLogger() { return LoggerFactory.getLogger(ScrenAddOrEditContasAReceber.class); }
}

package my_app.screens.clienteScreen;

import com.google.gson.Gson;
import megalodonte.base.components.Component;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Customer form layout and validation are declared in JSONC. */
public final class AddOrEditClientes extends ScreenAddOrEdit<ClienteModel, ClienteViewModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(AddOrEditClientes.class, "/views/cliente-form.view.jsonc");
    private static final Gson JSON = new Gson();
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public AddOrEditClientes(ScreenContextInterface screenContext) {
        super(screenContext);
        session = VIEW.open(new Actions().register("save", ignored -> handleAddOrUpdate()));
        session.bind("nome", viewModel.nome, String.class);
        session.bind("tipoPessoa", viewModel.tipoPessoaSelected, String.class);
        session.bind("documento", viewModel.cnpjCpf, String.class);
        session.bind("celular", viewModel.celular, String.class);
        session.bind("email", viewModel.email, String.class);
        session.bind("gestante", viewModel.isGestante, String.class);
        session.bind("cep", viewModel.enderecoState.get().cep, String.class);
        session.bind("uf", viewModel.enderecoState.get().ufSelected, String.class);
        session.bind("cidade", viewModel.enderecoState.get().cidade, String.class);
        session.bind("bairro", viewModel.enderecoState.get().bairro, String.class);
        session.bind("rua", viewModel.enderecoState.get().rua, String.class);
        session.bind("numero", viewModel.enderecoState.get().numero, String.class);
        session.bind("observacao", viewModel.observacao, String.class);
        session.display("btnText", getBtnActionText(), String.class);
        session.display("pessoaFisica", viewModel.tipoPessoaEhFisica, Boolean.class);
        session.display("gestanteVisible", viewModel.isGestanteComputed, Boolean.class);
        subscriptions.add(viewModel.tipoPessoaEhFisica.observe(value -> session.states().set("pessoaJuridica", !value)));
        session.states().string("tipoOptions").set(JSON.toJson(Data.tiposPessoaList));
        session.states().string("simNaoOptions").set(JSON.toJson(Data.simNaoList));
        session.states().string("ufOptions").set(JSON.toJson(Data.ufList));
        bindDate("dataNascimento", viewModel.dataNascimento);
        bindDate("dataNascimentoBebe", viewModel.dataNascimentoBebe);
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

    @Override
    protected ClienteViewModel getViewModel(ScreenContextInterface screenContext) {
        return new ClienteViewModel(screenContext);
    }

    @Override
    protected BaseService<ClienteModel> getService() throws SQLException {
        return new ClienteService();
    }

    @Override
    protected String getTitle() {
        return "cliente";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(AddOrEditClientes.class);
    }
}

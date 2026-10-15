package my_app.screens.fornecedorScreen;

import com.google.gson.Gson;
import megalodonte.base.components.Component;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.FornecedorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Supplier form layout and validation are declared in JSONC. */
public final class ScreenAddOrEditFornecedor extends ScreenAddOrEdit<FornecedorModel, FornecedorScreenViewModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(ScreenAddOrEditFornecedor.class, "/views/fornecedor-form.view.jsonc");
    private static final Gson JSON = new Gson();
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public ScreenAddOrEditFornecedor(ScreenContextInterface screenContext) {
        super(screenContext);
        session = VIEW.open(new Actions().register("save", ignored -> handleAddOrUpdate()));
        session.bind("nome", viewModel.nome, String.class);
        session.bind("tipoPessoa", viewModel.tipoPessoaSelected, String.class);
        session.bind("documento", viewModel.cnpjCpf, String.class);
        session.bind("celular", viewModel.celular, String.class);
        session.bind("email", viewModel.email, String.class);
        session.bind("inscricaoEstadual", viewModel.inscricaoEstadual, String.class);
        session.bind("cep", viewModel.enderecoState.get().cep, String.class);
        session.bind("uf", viewModel.enderecoState.get().ufSelected, String.class);
        session.bind("cidade", viewModel.enderecoState.get().cidade, String.class);
        session.bind("bairro", viewModel.enderecoState.get().bairro, String.class);
        session.bind("rua", viewModel.enderecoState.get().rua, String.class);
        session.bind("numero", viewModel.enderecoState.get().numero, String.class);
        session.bind("observacao", viewModel.observacao, String.class);
        session.display("btnText", getBtnActionText(), String.class);
        session.display("pessoaFisica", viewModel.tipoPessoaEhFisica, Boolean.class);
        subscriptions.add(viewModel.tipoPessoaEhFisica.observe(value -> session.states().set("pessoaJuridica", !value)));
        session.states().string("tipoOptions").set(JSON.toJson(Data.tiposPessoaList));
        session.states().string("ufOptions").set(JSON.toJson(Data.ufList));
    }

    @Override public Component render() { return session.render(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        super.onDestroy();
    }

    @Override
    protected FornecedorScreenViewModel getViewModel(ScreenContextInterface screenContext) {
        return new FornecedorScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<FornecedorModel> getService() throws SQLException {
        return new FornecedorService();
    }

    @Override
    protected String getTitle() {
        return "fornecedor";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ScreenAddOrEditFornecedor.class);
    }
}

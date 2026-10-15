package my_app.screens.produtoScreen;

import com.google.gson.Gson;
import javafx.stage.FileChooser;
import megalodonte.base.components.Component;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.models.ProdutoModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ProdutoService;
import my_app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Product fields and actions are declared in JSONC; persistence remains in the view model. */
public final class AddOrEditProduto extends ScreenAddOrEdit<ProdutoModel, ProdutoScreenViewModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(AddOrEditProduto.class, "/views/produto-form.view.jsonc");
    private static final Gson JSON = new Gson();
    private final UiSession session;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public AddOrEditProduto(ScreenContextInterface screenContext) {
        super(screenContext);
        session = VIEW.open(new Actions()
                .register("save", ignored -> handleAddOrUpdate())
                .register("generateBarcode", ignored -> viewModel.codigoBarras.set(Utils.gerarCodigoBarrasEAN13()))
                .register("chooseImage", ignored -> chooseImage()));
        bind("codigoBarras", viewModel.codigoBarras);
        bind("descricao", viewModel.descricao);
        bind("unidade", viewModel.unidadeSelected);
        bind("marca", viewModel.marca);
        bind("tamanho", viewModel.tamanhoSelected);
        bind("modelo", viewModel.modelo);
        bind("precoCompra", viewModel.precoCompra);
        bind("frete", viewModel.frete);
        bind("precoVenda", viewModel.precoVenda);
        bind("perecivel", viewModel.perecivelSelected);
        bind("garantia", viewModel.garantia);
        bind("aceitaDevolucao", viewModel.aceitaDevolucao);
        bind("observacoes", viewModel.observacoes);
        bind("estoque", viewModel.estoque);
        bind("estoqueMinimo", viewModel.estoqueMinimo);
        bind("imagem", viewModel.imagem);
        session.display("btnText", getBtnActionText(), String.class);
        session.states().string("unitOptions").set(JSON.toJson(Data.unidadesDeMedidaList));
        session.states().string("yesNoOptions").set(JSON.toJson(Data.simNaoList));
        session.states().string("perishableOptions").set(JSON.toJson(List.of("Sim", "Não")));
        subscriptions.add(viewModel.perecivelSelected.observe(v -> session.states().set("expiryVisible", "Sim".equals(v))));
        bindDate("validade", viewModel.validade);
        bindSelection("categoria", "categoryOptions", viewModel.categorias, viewModel.categoriaSelected, CategoriaModel::getId, CategoriaModel::getNome);
        bindSelection("fornecedor", "supplierOptions", viewModel.fornecedores, viewModel.fornecedorSelected, FornecedorModel::getId, FornecedorModel::getNome);
        var colorsText = session.states().string("cores");
        final boolean[] updatingColorsFromInput = {false};
        subscriptions.add(viewModel.coresSelecionadas.observe(values -> {
            if (updatingColorsFromInput[0]) return;
            String joined = String.join(", ", values);
            if (!Objects.equals(colorsText.get(), joined)) colorsText.set(joined);
        }));
        subscriptions.add(colorsText.observe(value -> {
            var names = Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
            if (!viewModel.coresSelecionadas.get().equals(names)) {
                updatingColorsFromInput[0] = true;
                try { viewModel.coresSelecionadas.set(names); }
                finally { updatingColorsFromInput[0] = false; }
            }
        }));
        subscriptions.add(viewModel.cores.observe(colors -> session.states().string("availableColors").set(
                "Cores disponíveis: " + String.join(", ", colors.stream().map(color -> color.getNome()).toList()))));
        viewModel.fetchListData();
    }

    private void bind(String name, State<String> model) { session.bind(name, model, String.class); }

    private <T> void bindSelection(String stateName, String optionsName, State<List<T>> choices, State<T> selected,
                                   Function<T, Long> id, Function<T, String> name) {
        var view = session.states().string(stateName);
        Function<T, String> label = value -> id.apply(value) + " · " + name.apply(value);
        subscriptions.add(choices.observe(list -> {
            session.states().string(optionsName).set(JSON.toJson(list.stream().map(label).toList()));
            T value = selected.get();
            if (value != null) view.set(label.apply(value));
        }));
        subscriptions.add(selected.observe(value -> {
            String next = value == null ? "" : label.apply(value);
            if (!Objects.equals(view.get(), next)) view.set(next);
        }));
        subscriptions.add(view.observe(text -> choices.get().stream()
                .filter(value -> label.apply(value).equals(text)).findFirst()
                .ifPresent(value -> { if (!Objects.equals(selected.get(), value)) selected.set(value); })));
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

    private void chooseImage() {
        var chooser = new FileChooser();
        chooser.setTitle("Escolha a imagem");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        var file = chooser.showOpenDialog(screenContext.selfStage());
        if (file != null) viewModel.imagem.set(file.toURI().toString());
    }

    @Override public Component render() { return session.render(); }
    @Override public void onDestroy() {
        subscriptions.forEach(Subscription::close);
        session.close();
        super.onDestroy();
    }
    @Override protected ProdutoScreenViewModel getViewModel(ScreenContextInterface ctx) { return new ProdutoScreenViewModel(ctx); }
    @Override protected BaseService<ProdutoModel> getService() throws SQLException { return new ProdutoService(); }
    @Override protected String getTitle() { return "produto"; }
    @Override protected Logger getLogger() { return LoggerFactory.getLogger(AddOrEditProduto.class); }
}

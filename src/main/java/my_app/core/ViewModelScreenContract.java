package my_app.core;

import megalodonte.application.ErrorReporter;
import megalodonte.base.UI;
import megalodonte.base.state.State;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.utils.ThrowingSupplier;
import megalodonte.v2.ListState;

import java.sql.SQLException;

public abstract class ViewModelScreenContract<Model extends Identifier> {
    public String screenNameSpawn = "";
    protected final ScreenContextInterface ctx;

    public final State<Boolean> focusState = new State<>(false);

    public final State<String> searchState = new State<>("");
    public final ListState<Model> allDataList = ListState.ofEmpty();
    public final ListState<Model> filteredList = ListState.ofEmpty();
    public final State<Model> selected = State.of(null);


    protected boolean isEditing = false;
    public final State<String> btnText = State.of("Cadastrar");

    public ViewModelScreenContract(ScreenContextInterface ctx) {
        this.ctx = ctx;

        searchState.subscribe(_ -> applyFilter());
        allDataList.subscribe(_ -> applyFilter());
    }

    private void applyFilter() {
        var query = searchState.get();
        if (query == null || query.isBlank()) {
            filteredList.set(allDataList.get());
            return;
        }
        filteredList.set(allDataList.get().stream()
                .filter(it -> matchesSearch(it, query.trim().toLowerCase()))
                .toList());
    }

    protected abstract boolean matchesSearch(Model model, String query);

    public void onDestroy() throws Exception {
        // no-op por padrão, subclasses sobrescrevem se precisar
    }

    public abstract Model findById(Long id) throws SQLException;
    public abstract void populateFieldsFromModel(Model model);

    @Deprecated(forRemoval = true)
    public void populateFieldsFromModel(){}

    //inverso de populateFieldsFromModel(): monta um Model a partir do estado atual dos campos do formulário
    public abstract Model populateModelFromFields();

    public abstract void clearForm();
    public abstract void handleAddOrUpdate();
    public abstract void handleClickMenuDelete();

    //deve popular allDataList e filteredList
    //filteredList é o que vai preencher a tabela
    public abstract void fetchListData();

    public void handleFocusChange(boolean focus) {
        focusState.set(focus);
    }

    protected <T> T createOrReport(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            ErrorReporter.handle(e);
            throw new IllegalStateException(e); // interrompe a construção da tela de forma previsível
        }
    }

    public void isEditing() {
        isEditing = true;
        UI.runOnUi(()->   btnText.set("Atualizar"));
    }public void finishEditing() {
        isEditing = false;
        UI.runOnUi(()-> btnText.set("Cadastrar"));
    }
}

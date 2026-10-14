package my_app.screens.categoriaScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.components.SimpleTable;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.db.models.CategoriaModel;
import pack.utilities.DatePack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Category list layout and actions are declared in JSONC. */
public final class CategoriaScreen implements ScreenComponent, ScreenContract<CategoriaModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(CategoriaScreen.class, "/views/categoria-list.view.jsonc");
    private static final Gson JSON = new Gson();
    private final CategoriaScreenViewModel viewModel;
    private final UiSession session;
    private final Subscription rowsSubscription;
    private SimpleTable<CategoriaModel> exportTable;

    public CategoriaScreen(ScreenContextInterface context) {
        viewModel = new CategoriaScreenViewModel(context);
        session = VIEW.open(new Actions()
                .register("select", ignored -> selectRows())
                .register("create", ignored -> handleClickNew())
                .register("edit", ignored -> handleClickMenuEdit())
                .register("delete", ignored -> handleClickMenuDelete())
                .register("clone", ignored -> handleClickMenuClone())
                .register("exportCsv", ignored -> my_app.services.exports.TableExportActions.csv(context, exportTitle(), table()))
                .register("exportPdf", ignored -> my_app.services.exports.TableExportActions.pdf(context, exportTitle(), table())));
        session.bind("search", viewModel.searchState, String.class);
        rowsSubscription = viewModel.filteredList.observe(items -> session.states().string("rows").set(rowsJson(items)));
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { viewModel.fetchListData(); }
    @Override public void onDestroy() {
        rowsSubscription.close();
        session.close();
        ScreenContract.super.onDestroy();
    }

    private void selectRows() {
        var checked = session.states().string("selectedItems").get();
        var models = new ArrayList<CategoriaModel>();
        try {
            for (var element : JsonParser.parseString(checked).getAsJsonArray()) {
                long id = element.getAsJsonObject().get("id").getAsLong();
                viewModel.filteredList.get().stream().filter(model -> model.getId() == id).findFirst().ifPresent(models::add);
            }
        } catch (RuntimeException ignored) { models.clear(); }
        viewModel.selectedItems.set(models);
        var selected = session.states().string("selected").get();
        if (models.isEmpty() && !selected.isBlank()) {
            try {
                long id = JsonParser.parseString(selected).getAsJsonObject().get("id").getAsLong();
                viewModel.selected.set(viewModel.filteredList.get().stream().filter(model -> model.getId() == id).findFirst().orElse(null));
                viewModel.focusState.set(true);
                return;
            } catch (RuntimeException ignored) { }
        }
        viewModel.selected.set(models.isEmpty() ? null : models.getLast());
        viewModel.focusState.set(!models.isEmpty());
    }

    private String rowsJson(List<CategoriaModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("id", String.valueOf(item.getId()));
            row.put("nome", item.getNome() == null ? "" : item.getNome());
            row.put("dataCriacao", DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }

    @Override public ViewModelScreenContract<CategoriaModel> viewModel() { return viewModel; }
    @Override public Component itemDetails(CategoriaModel model) { return null; }

    /** Export columns still use the same native table model as before. */
    @Override public SimpleTable<CategoriaModel> table() {
        if (exportTable != null) return exportTable;
        var table = new SimpleTable<CategoriaModel>();
        table.fromData(viewModel.filteredList).header().columns()
                .column("ID", CategoriaModel::getId)
                .column("Nome", CategoriaModel::getNome)
                .column("Data de criação", item -> DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()))
                .build();
        exportTable = table;
        return exportTable;
    }
}

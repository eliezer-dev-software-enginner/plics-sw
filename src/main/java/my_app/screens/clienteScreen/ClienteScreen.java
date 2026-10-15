package my_app.screens.clienteScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.v2.Show;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ClienteModel;
import pack.utilities.DatePack;
import pack.utilities.FormatterPack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Customer list layout and actions are declared in JSONC. */
public final class ClienteScreen implements ScreenComponent, ScreenContract<ClienteModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(ClienteScreen.class, "/views/cliente-list.view.jsonc");
    private static final Gson JSON = new Gson();
    private final ClienteViewModel vm;
    private final ScreenContextInterface ctx;
    private final UiSession session;
    private final Subscription rowsSubscription;
    private SimpleTable<ClienteModel> exportTable;

    public ClienteScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.vm = new ClienteViewModel(ctx);
        session = VIEW.open(new Actions()
                .register("select", ignored -> selectRows())
                .register("details", ignored -> showDetails())
                .register("create", ignored -> handleClickNew())
                .register("edit", ignored -> handleClickMenuEdit())
                .register("delete", ignored -> handleClickMenuDelete())
                .register("clone", ignored -> handleClickMenuClone())
                .register("exportCsv", ignored -> my_app.services.exports.TableExportActions.csv(ctx, exportTitle(), table()))
                .register("exportPdf", ignored -> my_app.services.exports.TableExportActions.pdf(ctx, exportTitle(), table())));
        session.bind("search", vm.searchState, String.class);
        rowsSubscription = vm.filteredList.observe(items -> session.states().string("rows").set(rowsJson(items)));
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { vm.fetchListData(); }
    @Override public void onDestroy() {
        rowsSubscription.close();
        session.close();
        ScreenContract.super.onDestroy();
    }

    private void selectRows() {
        var models = new ArrayList<ClienteModel>();
        try {
            for (var element : JsonParser.parseString(session.states().string("selectedItems").get()).getAsJsonArray()) {
                long id = element.getAsJsonObject().get("id").getAsLong();
                vm.filteredList.get().stream().filter(model -> model.getId() == id).findFirst().ifPresent(models::add);
            }
        } catch (RuntimeException ignored) { models.clear(); }
        vm.selectedItems.set(models);
        var selected = session.states().string("selected").get();
        if (models.isEmpty() && !selected.isBlank()) {
            try {
                long id = JsonParser.parseString(selected).getAsJsonObject().get("id").getAsLong();
                vm.selected.set(vm.filteredList.get().stream().filter(model -> model.getId() == id).findFirst().orElse(null));
                vm.focusState.set(true);
                return;
            } catch (RuntimeException ignored) { }
        }
        vm.selected.set(models.isEmpty() ? null : models.getLast());
        vm.focusState.set(!models.isEmpty());
    }

    private void showDetails() {
        selectRows();
        if (vm.selected.get() != null) Components.ShowModal(itemDetails(vm.selected.get()), ctx, 400);
    }

    private String rowsJson(List<ClienteModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("id", String.valueOf(item.getId()));
            row.put("nome", value(item.getNome()));
            row.put("telefone", FormatterPack.formatPhone(item.getCelular()));
            row.put("email", value(item.getEmail()));
            row.put("cpfCnpj", item.getCpfCnpj() == null ? "" : item.getCpfCnpj().length() == 11
                    ? FormatterPack.formatCpf(item.getCpfCnpj()) : FormatterPack.formatCnpj(item.getCpfCnpj()));
            row.put("dataNascimento", item.getDataNascimento() == null ? "" : DatePack.millisToBrazilianDate(item.getDataNascimento()));
            row.put("gestante", value(item.getGestanteText()));
            row.put("dataNascimentoBebe", item.getDataNascimentoBebe() == null ? "" : DatePack.millisToBrazilianDate(item.getDataNascimentoBebe()));
            row.put("uf", value(item.getUf()));
            row.put("cep", item.getCep() == null ? "" : FormatterPack.formatCep(item.getCep()));
            row.put("cidade", value(item.getCidade()));
            row.put("bairro", value(item.getBairro()));
            row.put("rua", value(item.getRua()));
            row.put("numero", value(item.getNumero()));
            row.put("observacao", value(item.getObservacao()));
            row.put("dataCriacao", DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }
    private static String value(String value) { return value == null ? "" : value; }

    @Override public ViewModelScreenContract<ClienteModel> viewModel() { return vm; }
    @Override public SimpleTable<ClienteModel> table() {
        if (exportTable != null) return exportTable;
        exportTable = new SimpleTable<ClienteModel>()
                .fromData(vm.filteredList).header().columns()
                .column("ID", ClienteModel::getId)
                .column("Nome", ClienteModel::getNome)
                .column("Celular/Telefone", it -> FormatterPack.formatPhone(it.getCelular()))
                .column("Email", ClienteModel::getEmail)
                .column("CPF/CNPJ", it -> it.getCpfCnpj().length() == 11
                        ? FormatterPack.formatCpf(it.getCpfCnpj()) : FormatterPack.formatCnpj(it.getCpfCnpj()))
                .column("Data de nascimento", it -> it.getDataNascimento() == null ? "" : DatePack.millisToBrazilianDate(it.getDataNascimento()))
                .column("Gestante", ClienteModel::getGestanteText)
                .column("Data nasc. bebê", it -> it.getDataNascimentoBebe() == null ? "" : DatePack.millisToBrazilianDate(it.getDataNascimentoBebe()))
                .column("UF", it -> value(it.getUf()))
                .column("CEP", it -> it.getCep() == null ? "" : FormatterPack.formatCep(it.getCep()))
                .column("Cidade", it -> value(it.getCidade()))
                .column("Bairro", it -> value(it.getBairro()))
                .column("Rua", it -> value(it.getRua()))
                .column("Número", it -> value(it.getNumero()))
                .column("Observação", ClienteModel::getObservacao)
                .column("Data de criação", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build();
        return exportTable;
    }

    public Component itemDetails(ClienteModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do cliente", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Nome: ", model.getNome()))
                .c_child(Components.TextWithDetails("CPF/CNPJ: ", model.getCpfCnpj()))
                .c_child(Components.TextWithDetails("Email: ", model.getEmail()))
                .c_child(Components.TextWithDetails("Telefone: ",FormatterPack.formatPhone(model.getCelular())))
                .c_child(Components.TextWithDetails("Data de nascimento: ", DatePack.millisToBrazilianDate(model.getDataNascimento())))
                .c_child(Components.TextWithDetails("É gestante: ", model.getGestanteText()))
                .c_child(Show.when(model.getGestante()!=null && model.getGestante(), ()-> new Container().children(
                        Components.TextWithDetails("Data de nascimento do bebê: ", DatePack.millisToBrazilianDate(model.getDataNascimentoBebe()))
                )))
                .c_child(Components.ItemDetailEndereco(model.getEndereco()))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }
}

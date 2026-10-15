package my_app.screens.contasAReceberScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import megalodonte.v2.Show;
import megalodonte.base.theme.ThemeManager;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ContaAreceberModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Receivables list layout and actions are declared in JSONC. */
public final class ContasAReceberScreen implements ScreenComponent, ScreenContract<ContaAreceberModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(ContasAReceberScreen.class, "/views/contas-a-receber-list.view.jsonc");
    private static final Gson JSON = new Gson();
    private final ContasAReceberScreenViewModel vm;
    private final ScreenContextInterface ctx;
    private final UiSession session;
    private final Subscription rowsSubscription;

    public ContasAReceberScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.vm = new ContasAReceberScreenViewModel(ctx);
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
        var models = new ArrayList<ContaAreceberModel>();
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
        if (vm.selected.get() != null) Components.ShowModal(itemDetails(vm.selected.get()), ctx, 600);
    }

    private String rowsJson(List<ContaAreceberModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("id", String.valueOf(item.getId()));
            row.put("descricao", value(item.getDescricao()));
            row.put("cliente", item.getCliente() == null ? "" : value(item.getCliente().getNome()));
            row.put("valorOriginal", CurrencyPack.toBRLCurrency(item.getValorOriginal()));
            row.put("valorRecebido", CurrencyPack.toBRLCurrency(item.getValorRecebido()));
            row.put("valorRestante", CurrencyPack.toBRLCurrency(item.getValorRestante()));
            row.put("vencimento", item.getDataVencimento() == null ? "" : DatePack.millisToBrazilianDateTime(item.getDataVencimento()));
            row.put("status", formatStatus(item.getStatus()));
            row.put("produtoVendido", item.getVenda() == null || item.getVenda().getProduto() == null ? "" : value(item.getVenda().getProduto().getDescricao()));
            row.put("vendaId", value(item.getVendaId()));
            row.put("dataRecebimento", item.getDataRecebimento() == null ? "" : DatePack.millisToBrazilianDate(item.getDataRecebimento()));
            row.put("dataCriacao", item.getDataCriacao() == null ? "" : DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            row.put("observacao", value(item.getObservacao()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }
    private static String value(Object value) { return value == null ? "" : String.valueOf(value); }

    @Override
    public ViewModelScreenContract<ContaAreceberModel> viewModel() {
        return vm;
    }


    @Override
    public Component itemDetails(ContaAreceberModel model) {
          return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da conta a receber", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Descricao: ", model.getDescricao()))
                .c_child(Components.TextWithDetails("Status: ", formatStatus(model.getStatus())))
                .c_child(Components.TextWithDetails("Valor original: ", CurrencyPack.toBRLCurrency(model.getValorOriginal())))
                .c_child(Components.TextWithDetails("Valor recebido: ", CurrencyPack.toBRLCurrency(model.getValorRecebido())))
                .c_child(Components.TextWithDetails("Valor restante: ", CurrencyPack.toBRLCurrency(model.getValorRestante())))
                .c_child(Show.when(model.getVenda() != null && model.getVenda().getProduto() != null, () -> new Column()
                        .c_child(Components.TextWithDetails("Produto vendido: ", model.getVenda().getProduto().getDescricao()))
                        .c_child(Components.TextWithDetails("- Id do produto: ", model.getVenda().getProduto().getId()))
                ))
                .c_child(Components.TextWithDetails("Nome do cliente: ", model.getCliente() != null ? model.getCliente().getNome() : "-"))
                .c_child(Components.TextWithDetails("- Id do cliente: ", model.getCliente() != null ? model.getCliente().getId() : "-"))
                .c_child(Components.TextWithDetails("- Id da venda: ", model.getVendaId()))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Data de vencimento: ", DatePack.millisToBrazilianDate(model.getDataVencimento())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }

    @Override
    public SimpleTable<ContaAreceberModel> table() {
        var simpleTable = new SimpleTable<ContaAreceberModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", it -> it.getId() != null ? "#" + it.getId() : "")
                .column("Descrição", ContaAreceberModel::getDescricao)
                .column("Cliente", it -> it.getCliente() != null ? it.getCliente().getNome() : "")
                .column("Valor Original", it -> CurrencyPack.toBRLCurrency(it.getValorOriginal()))
                .column("Valor Recebido", it -> CurrencyPack.toBRLCurrency(it.getValorRecebido()))
                .column("Valor Restante", it -> CurrencyPack.toBRLCurrency(it.getValorRestante()))
                .column("Vencimento", it -> it.getDataVencimento() != null ? DatePack.millisToBrazilianDateTime(it.getDataVencimento()) : "")
                .column("Status", it -> formatStatus(it.getStatus()))
                .column("Produto Vendido", it -> it.getVenda() != null && it.getVenda().getProduto() != null ? it.getVenda().getProduto().getDescricao() : "")
                .column("Id Venda", it -> it.getVendaId() != null ? it.getVendaId() : "")
                .column("Data Recebimento", it -> it.getDataRecebimento() != null ? DatePack.millisToBrazilianDate(it.getDataRecebimento()) : "")
                .column("Data de criação", it -> it.getDataCriacao() != null ? DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()) : "")
                .column("Observação", ContaAreceberModel::getObservacao)
                .build()
                .onItemSelectChange(vm.selected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), ctx, 600));

        return simpleTable;
    }

    private String formatStatus(String status) {
        if ("PAGO".equals(status)) return "✅ " + status;
        if ("ATRASADO".equals(status)) return "⚠️ " + status;
        if ("PARCIAL".equals(status)) return "📊 " + status;
        return "⏳ " + (status != null ? status : "");
    }
}

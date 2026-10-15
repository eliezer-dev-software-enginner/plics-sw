package my_app.screens.vendaScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import megalodonte.props.ColumnProps;
import megalodonte.props.ImageProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.VendaModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Sales list layout and actions are declared in JSONC. */
public final class VendaMercadoriaScreen implements ScreenComponent, ScreenContract<VendaModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(VendaMercadoriaScreen.class, "/views/venda-list.view.jsonc");
    private static final Gson JSON = new Gson();
    private final VendaMercadoriaScreenViewModel vm;
    private final ScreenContextInterface screenContext;
    private final UiSession session;
    private final Subscription rowsSubscription;

    public VendaMercadoriaScreen(ScreenContextInterface ctx) {
        this.vm = new VendaMercadoriaScreenViewModel(ctx);
        this.screenContext = ctx;
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
        var models = new ArrayList<VendaModel>();
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
        if (vm.selected.get() != null) Components.ShowModal(itemDetails(vm.selected.get()), screenContext, 550);
    }

    private String rowsJson(List<VendaModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("id", String.valueOf(item.getId()));
            row.put("imagem", item.getProduto() == null ? "" : value(item.getProduto().getImagem()));
            row.put("produtoCod", value(item.getProdutoCod()));
            row.put("produto", item.getProduto() == null ? "" : value(item.getProduto().getDescricao()));
            row.put("cliente", item.getCliente() == null ? "" : value(item.getCliente().getNome()));
            row.put("numeroNota", value(item.getNumeroNota()));
            row.put("tipoPagamento", value(item.getTipoPagamento()));
            row.put("quantidade", value(item.getQuantidade()));
            row.put("precoUnitario", CurrencyPack.toBRLCurrency(item.getPrecoUnitario()));
            row.put("desconto", CurrencyPack.toBRLCurrency(item.getDesconto()));
            row.put("frete", CurrencyPack.toBRLCurrency(item.getFrete()));
            row.put("totalLiquido", CurrencyPack.toBRLCurrency(item.getTotalLiquido()));
            row.put("dataValidade", item.getDataValidade() == null ? "" : DatePack.millisToBrazilianDateTime(item.getDataValidade()));
            row.put("observacao", value(item.getObservacao()));
            row.put("dataCriacao", DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            row.put("status", Boolean.TRUE.equals(item.getDevolvida()) ? "Devolvida" : "-");
            row.put("dataDevolucao", Boolean.TRUE.equals(item.getDevolvida()) && item.getDataDevolucao() != null ? DatePack.millisToBrazilianDateTime(item.getDataDevolucao()) : "-");
            rows.add(row);
        }
        return JSON.toJson(rows);
    }
    private static String value(Object value) { return value == null ? "" : String.valueOf(value); }

    @Override
    public SimpleTable<VendaModel> table() {
        return new SimpleTable<VendaModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", VendaModel::getId)
                .imageColumn("Imagem", it -> it.getProduto().getImagem())
                .column("Código", VendaModel::getProdutoCod)
                .column("Produto", it -> it.getProduto().getDescricao())
                .column("Cliente", it -> it.getCliente() != null ? it.getCliente().getNome() : "")
                .column("Nº Nota", it -> it.getNumeroNota() != null ? it.getNumeroNota() : "")
                .column("Pagamento", VendaModel::getTipoPagamento)
                .column("Quantidade", VendaModel::getQuantidade)
                .column("Preço de venda", it -> CurrencyPack.toBRLCurrency(it.getPrecoUnitario()))
                .column("Desconto", it -> CurrencyPack.toBRLCurrency(it.getDesconto()))
                .column("Frete", it -> CurrencyPack.toBRLCurrency(it.getFrete()))
                .column("Total líquido", it -> CurrencyPack.toBRLCurrency(it.getTotalLiquido()))
                .column("Validade", it -> it.getDataValidade() != null ? DatePack.millisToBrazilianDateTime(it.getDataValidade()) : "")
                .column("Observação", VendaModel::getObservacao)
                .column("Data", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .column("Status", it -> Boolean.TRUE.equals(it.getDevolvida()) ? "Devolvida" : "-")
                .column("Devolvida em", it -> Boolean.TRUE.equals(it.getDevolvida()) && it.getDataDevolucao() != null ? DatePack.millisToBrazilianDateTime(it.getDataDevolucao()) : "-")
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.selected::set)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), this.screenContext, 550));
    }

    public Component itemDetails(VendaModel model) {
        var validade = model.getDataValidade() != null ? DatePack.millisToBrazilianDateTime(model.getDataValidade()) : "Sem validade";
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da venda de mercadoria", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Show.when(model.getProduto().getImagem()!=null,
                            ()->new Image(model.getProduto().getImagem(), new ImageProps().size(100)))
                )
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Código do produto: ", model.getProdutoCod()))
                .c_child(Components.TextWithDetails("Nome do produto: ", model.getProduto().getDescricao()))
                .c_child(Components.TextWithDetails("Id do cliente: ", model.getCliente().getId()))
                .c_child(Components.TextWithDetails("Nome do cliente: ", model.getCliente().getNome()))
                .c_child(Components.TextWithDetails("Número da nota: ", model.getNumeroNota()))
                .c_child(Components.TextWithDetails("Tipo de pagamento: ", model.getTipoPagamento()))
                .c_child(Components.TextWithDetails("Quantidade: ", model.getQuantidade()))
                .c_child(Components.TextWithDetails("Preço de venda: ", CurrencyPack.toBRLCurrency(model.getPrecoUnitario())))
                .c_child(Components.TextWithDetails("Desconto: ", CurrencyPack.toBRLCurrency(model.getDesconto())))
                .c_child(Components.TextWithDetails("Frete: ", CurrencyPack.toBRLCurrency(model.getFrete())))
                .c_child(Components.TextWithDetails("Total da venda: ", CurrencyPack.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Validade: ", validade))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true))
                .c_child(Show.when(Boolean.TRUE.equals(model.getDevolvida()),
                        () -> Components.TextWithDetails("Devolvida em: ",
                                DatePack.millisToBrazilianDateTime(model.getDataDevolucao()))
                ))
                .c_child(new Row(new RowProps().spacingOf(10)).children(
                        new Button("Imprimir nota de venda").onClick(() -> vm.imprimirNotaDeVenda(model)),
                        new Button("Imprimir (modo alternativo)").onClick(() -> vm.imprimirNotaDeVendaAlternativo(model))
                ))
                .c_child(Show.when(!Boolean.TRUE.equals(model.getDevolvida()),
                        () -> new Button("Devolver venda").onClick(() -> vm.handleClickMenuDevolucao(model))
                ));
    }

    @Override
    public ViewModelScreenContract<VendaModel> viewModel() { return vm; }
}

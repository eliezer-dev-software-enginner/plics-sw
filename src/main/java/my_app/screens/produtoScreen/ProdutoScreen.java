package my_app.screens.produtoScreen;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Image;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import megalodonte.props.ColumnProps;
import megalodonte.props.ImageProps;
import megalodonte.props.TextProps;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ProdutoModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Product list layout and actions are declared in JSONC. */
public final class ProdutoScreen implements ScreenComponent, ScreenContract<ProdutoModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(ProdutoScreen.class, "/views/produto-list.view.jsonc");
    private static final Gson JSON = new Gson();
    private final ProdutoScreenViewModel vm;
    private final ScreenContextInterface ctx;
    private final ThemeInterface theme = ThemeManager.theme();
    private final UiSession session;
    private final Subscription rowsSubscription;

    public ProdutoScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.vm = new ProdutoScreenViewModel(ctx);
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
        var models = new ArrayList<ProdutoModel>();
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

    private String rowsJson(List<ProdutoModel> items) {
        var rows = new ArrayList<Map<String, String>>(items.size());
        for (var item : items) {
            var row = new LinkedHashMap<String, String>();
            row.put("id", String.valueOf(item.getId()));
            row.put("imagem", value(item.getImagem()));
            row.put("codigoBarras", value(item.getCodigoBarras()));
            row.put("descricao", value(item.getDescricao()));
            row.put("categoria", item.getCategoria() == null ? "" : value(item.getCategoria().getNome()));
            row.put("fornecedor", item.getFornecedor() == null ? "" : value(item.getFornecedor().getNome()));
            row.put("cor", value(item.getCor()));
            row.put("tamanho", value(item.getTamanho()));
            row.put("modelo", value(item.getModelo()));
            row.put("unidade", value(item.getUnidade()));
            row.put("marca", value(item.getMarca()));
            row.put("estoque", value(item.getEstoque()));
            row.put("estoqueMinimo", value(item.getEstoqueMinimo()));
            row.put("precoCompra", CurrencyPack.toBRLCurrency(item.getPrecoCompra()));
            row.put("frete", CurrencyPack.toBRLCurrency(item.getFrete()));
            row.put("precoVenda", CurrencyPack.toBRLCurrency(item.getPrecoVenda()));
            row.put("ganhoLiquido", CurrencyPack.toBRLCurrency(item.getTotalLiquido()));
            row.put("garantia", value(item.getGarantia()));
            row.put("aceitaDevolucao", Boolean.TRUE.equals(item.getAceitaDevolucao()) ? "Sim" : "Não");
            row.put("validade", item.getValidade() == null ? "" : DatePack.millisToBrazilianDateTime(item.getValidade()));
            row.put("observacoes", value(item.getObservacoes()));
            row.put("dataCriacao", DatePack.localDateTimeToBrazilianDateTime(item.getDataCriacao()));
            rows.add(row);
        }
        return JSON.toJson(rows);
    }
    private static String value(Object value) { return value == null ? "" : String.valueOf(value); }

    @Override
    public SimpleTable<ProdutoModel> table() {
        var simpleTable = new SimpleTable<ProdutoModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", ProdutoModel::getId)
                .imageColumn("Imagem", ProdutoModel::getImagem)
                .column("Código", ProdutoModel::getCodigoBarras)
                .column("Descrição", ProdutoModel::getDescricao)
                .column("Categoria", it -> it.getCategoria() != null ? it.getCategoria().getNome() : "")
                .column("Fornecedor", it -> it.getFornecedor() != null ? it.getFornecedor().getNome() : "")
                .column("Cor", it -> it.getCor() != null ? it.getCor() : "")
                .column("Tamanho", it -> it.getTamanho() != null ? it.getTamanho() : "")
                .column("Modelo", it -> it.getModelo() != null ? it.getModelo() : "")
                .column("Unidade", ProdutoModel::getUnidade)
                .column("Marca", ProdutoModel::getMarca)
                .column("Estoque", ProdutoModel::getEstoque)
                .column("Est. Mínimo", ProdutoModel::getEstoqueMinimo)
                .column("Preço de compra", it -> CurrencyPack.toBRLCurrency(it.getPrecoCompra()))
                .column("Frete", it -> CurrencyPack.toBRLCurrency(it.getFrete()))
                .column("Preço de venda", it -> CurrencyPack.toBRLCurrency(it.getPrecoVenda()))
                .column("Ganho líquido", it -> CurrencyPack.toBRLCurrency(it.getTotalLiquido()))
                .column("Garantia", it -> it.getGarantia() != null ? it.getGarantia() : "")
                .column("Aceita devolução", it -> Boolean.TRUE.equals(it.getAceitaDevolucao()) ? "Sim" : "Não")
                .column("Validade", it -> it.getValidade() != null ? DatePack.millisToBrazilianDateTime(it.getValidade()) : "")
                .column("Observação", ProdutoModel::getObservacoes)
                .column("Data de criação", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.selected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), vm.getCtx(), 600));

        return simpleTable;
    }

     public Component itemDetails(ProdutoModel model) {
        var validade = model.getValidade() != null ? DatePack.millisToBrazilianDateTime(model.getValidade()) : "Sem validade";

        return new Column(new ColumnProps().paddingAll(theme.spacing().md()))
                .children(
                        new Text("Detalhes do produto", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())),
                        new SpacerVertical(20),
                        Show.when(model.getImagem()!=null, ()->new Image(model.getImagem(), new ImageProps().size(100))),
                        Components.TextWithDetails("ID: ", model.getId()),
                        Components.TextWithDetails("Código: ", model.getCodigoBarras()),
                        Components.TextWithDetails("Cor: ", model.getCor() != null ? model.getCor() : "-"),
                        Components.TextWithDetails("Tamanho: ", model.getTamanho() != null ? model.getTamanho() : "-"),
                        Components.TextWithDetails("Modelo: ", model.getModelo() != null ? model.getModelo() : "-"),
                        Components.TextWithDetails("Descrição: ", model.getDescricao()),
                        Components.TextWithDetails("Fornecedor: ", model.getFornecedor().getNome()),
                        Components.TextWithDetails("Categoria: ", model.getCategoria().getNome()),
                        Components.TextWithDetails("Tipo de unidade: ", model.getUnidade()),
                        Components.TextWithDetails("Marca: ", model.getMarca()),
                        Components.TextWithDetails("Estoque: ", model.getEstoque()),
                        Components.TextWithDetails("Estoque Mínimo: ", model.getEstoqueMinimo()),
                        Components.TextWithDetails("Preço de compra (R$): ", CurrencyPack.toBRLCurrency(model.getPrecoCompra())),
                        Components.TextWithDetails("Frete (R$): ", CurrencyPack.toBRLCurrency(model.getFrete())),
                        Components.TextWithDetails("Preço de venda (R$): ", CurrencyPack.toBRLCurrency(model.getPrecoVenda())),
                        Components.TextWithDetails("Ganho líquido estimado (R$): ", CurrencyPack.toBRLCurrency(model.getTotalLiquido())),
                        Components.TextWithDetails("Garantia: ", model.getGarantia()),
                        Components.TextWithDetails("Aceita devolução/troca: ", Boolean.TRUE.equals(model.getAceitaDevolucao()) ? "Sim" : "Não"),
                        Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())),
                        Components.TextWithDetails("Validade: ", validade),
                        Components.TextWithDetails("Observação: ", model.getObservacoes(), true)
                );
    }

    @Override
    public ViewModelScreenContract<ProdutoModel> viewModel() {
        return vm;
    }
}

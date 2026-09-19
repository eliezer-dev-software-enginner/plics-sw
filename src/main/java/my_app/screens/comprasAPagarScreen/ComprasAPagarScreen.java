package my_app.screens.comprasAPagarScreen;

import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ContasPagarModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

public class ComprasAPagarScreen implements ScreenComponent, ScreenContract<ContasPagarModel> {
    private final ComprasAPagarScreenViewModel vm;
    private final ThemeInterface theme = ThemeManager.theme();
    private final ScreenContext ctx;

    public ComprasAPagarScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new ComprasAPagarScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ScreenContract.super.onDestroy();
    }

    @Override
    public Component render() {
        var mainContent = new Container(new ContainerProps().bgColor(theme.colors().background()))
                .children(
                        Components.searchInput(viewModel().searchState, ""),
                        new SpacerVertical(30),
                        paymentSection(),
                        new SpacerVertical(30),
                        table()
                );

        return new Container(new ContainerProps().paddingAll(10).bgColor(theme.colors().background()))
                .children(
                        commonCustomMenus(vm.focusState),
                        new SpacerVertical(10),
                        Components.ScrollPaneDefault(mainContent)
                );
    }

    @Override
    public ViewModelScreenContract<ContasPagarModel> viewModel() {
        return vm;
    }


    @Override
    public Component itemDetails(ContasPagarModel model) {
        return null;
    }

    private Component paymentSection() {
        return Show.when(vm.modoPagamento, () ->
                new Card(
                        new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                                .c_child(new Text("Registrar Pagamento", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                                .c_child(
                                        new Row(new RowProps().spacingOf(12).bottomVertically())
                                                .r_child(
                                                        new Column(new ColumnProps())
                                                                .c_child(new Text("Valor do Pagamento:", new TextProps().fontSize(ThemeManager.theme().typography().body())))
                                                                .c_child(Pack.InputColumnCurrency("Valor", vm.valorPagamento))
                                                )
                                                .r_child(
                                                        new Row(new RowProps().spacingOf(8))
                                                                .r_child(
                                                                        new Button("Registrar",
                                                                                new ButtonProps()
                                                                                        .height(35)
                                                                                        .fontSize(theme.typography().small())
                                                                                        .bgColor("#10b981")
                                                                                        .textColor("white")
                                                                        ).onClick(() -> vm.registrarPagamento(ctx))
                                                                )
                                                                .r_child(
                                                                        new Button("Cancelar",
                                                                                new ButtonProps()
                                                                                        .height(35)
                                                                                        .fontSize(theme.typography().small())
                                                                                        .bgColor("#6c757d")
                                                                                        .textColor("white")
                                                                        ).onClick(() -> {
                                                                            vm.modoPagamento.set(false);
                                                                            vm.valorPagamento.set("0");
                                                                        })
                                                                )
                                                )
                                )
                )
        );
    }

    @Override
    public SimpleTable table() {
        var simpleTable = new SimpleTable<ContasPagarModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", it -> it.getId() != null ? "#" + it.getId() : "")
                .column("Descrição", ContasPagarModel::getDescricao)
                .column("Fornecedor", it -> it.getFornecedor() != null ? it.getFornecedor().getNome() : "")
                .column("Valor Original", it -> CurrencyPack.toBRLCurrency(it.getValorOriginal()))
                .column("Valor Restante", it -> CurrencyPack.toBRLCurrency(it.getValorRestante()))
                .column("Vencimento", it -> it.getDataVencimento() != null ? DatePack.millisToBrazilianDateTime(it.getDataVencimento()) : "")
                .column("Status", it -> formatStatus(it.getStatus()))
                .build()
                .onItemSelectChange(vm.selected::set)
                .onChangeFocus(vm::handleFocusChange);

        return simpleTable;
    }

    private String formatStatus(String status) {
        if ("PAGO".equals(status)) return "✅ " + status;
        if ("ATRASADO".equals(status)) return "⚠️ " + status;
        if ("PARCIAL".equals(status)) return "📊 " + status;
        return "⏳ " + (status != null ? status : "");
    }
}

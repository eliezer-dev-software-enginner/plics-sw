package my_app.screens.contasAReceberScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ContaAreceberModel;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;

public class ContasAReceberScreen implements ScreenComponent, ScreenContract<ContaAreceberModel> {
    private final ContasAReceberScreenViewModel vm;
    private final ThemeInterface theme = ThemeManager.theme();
    private final ScreenContextInterface ctx;

    public ContasAReceberScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.vm = new ContasAReceberScreenViewModel(ctx);
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
        return mainView(vm.focusState);
    }

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

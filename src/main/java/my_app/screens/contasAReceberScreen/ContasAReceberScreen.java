package my_app.screens.contasAReceberScreen;

import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.ContaAreceberModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ContasAReceberScreen implements ScreenComponent, ContratoTelaCrudV3<ContaAreceberModel> {
    private final ContasAReceberScreenViewModel vm;
    private final ThemeInterface theme = ThemeManager.theme();
    private final ScreenContext ctx;

    public ContasAReceberScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new ContasAReceberScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ContratoTelaCrudV3.super.onDestroy();
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
    public Component form() {
        ComputedState<Boolean> naoEhRecebimento = ComputedState.of(() -> !vm.modoRecebimento.get(), vm.modoRecebimento);

        // mainView() (ContratoTelaCrudV3) só chama form()/table() — não tem mais slot
        // próprio pra summarySection()/paymentSection() desde que render() passou a
        // devolver mainView() direto, então as duas entram aqui, em volta do Card do
        // formulário, pra continuar aparecendo na tela.
        return new Column(new ColumnProps().spacingOf(20)).children(
                summarySection(),
                formCard(naoEhRecebimento),
                paymentSection()
        );
    }

    private Component formCard(ComputedState<Boolean> naoEhRecebimento) {
        return new Card(
                new Column(new ColumnProps().paddingAll(20).spacingOf(15))
                        .c_child(Components.FormTitle(vm.btnText.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.InputColumn("Descrição", vm.descricao, "Descrição da conta"))
                                        .r_child(Components.InputColumnCurrency("Valor Original", vm.valorOriginal))
                                        .r_child(Components.SelectColumn("Cliente", vm.clientes, vm.clienteSelected,
                                                f -> f != null ? f.getNome() : "", true))
                                        .r_child(Components.SelectColumn("Status", vm.statusOptions, vm.status, s -> s))
                        )
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.DatePickerColumn(vm.dataVencimento, "Data Vencimento"))
                                        .r_child(Components.DatePickerColumn(vm.dataRecebimento, "Data Recebimento"))
                                        .r_child(Components.SelectColumn("Tipo Doc", vm.tipoDocumentoOptions, vm.tipoDocumento, t -> t))
                                        .r_child(Components.InputColumn("Número Doc", vm.numeroDocumento, "Número do documento"))
                        )
                        .c_child(Components.TextAreaColumn("Observação", vm.observacao, "Alguma observação sobre esta conta?"))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate))
                        .c_child(Show.when(vm.modoEdicaoState(), () -> new Row(new RowProps().spacingOf(8))
                                .r_child(
                                        Show.when(naoEhRecebimento, () -> new Button(
                                                        vm.btnRecebimentoText,
                                                        new ButtonProps()
                                                                .height(35)
                                                                .fontSize(theme.typography().small())
                                                                .bgColor("#10b981")
                                                                .textColor("white")
                                                ).onClick(() -> {
                                                    if (vm.modoRecebimento.get()) {
                                                        vm.registrarRecebimento(ctx);
                                                    } else {
                                                        vm.modoRecebimento.set(true);
                                                    }
                                                }))
                                )
                                .r_child(
                                        new Button("Quitar",
                                                new ButtonProps()
                                                        .height(35)
                                                        .fontSize(theme.typography().small())
                                                        .bgColor("#007bff")
                                                        .textColor("white")
                                        ).onClick(() -> vm.quitarConta(ctx))
                                )
                        ))
        );
    }

    @Override
    public Component itemDetails(ContaAreceberModel model) {
          return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da conta a receber", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Descricao: ", model.getDescricao()))
                .c_child(Components.TextWithDetails("Status: ", formatStatus(model.getStatus())))
                .c_child(Components.TextWithDetails("Valor original: ", Utils.toBRLCurrency(model.getValorOriginal())))
                .c_child(Components.TextWithDetails("Valor recebido: ", Utils.toBRLCurrency(model.getValorRecebido())))
                .c_child(Components.TextWithDetails("Valor restante: ", Utils.toBRLCurrency(model.getValorRestante())))
                .c_child(Show.when(model.getVenda() != null && model.getVenda().getProduto() != null, () -> new Column()
                        .c_child(Components.TextWithDetails("Produto vendido: ", model.getVenda().getProduto().getDescricao()))
                        .c_child(Components.TextWithDetails("- Id do produto: ", model.getVenda().getProduto().getId()))
                ))
                .c_child(Components.TextWithDetails("Nome do cliente: ", model.getCliente() != null ? model.getCliente().getNome() : "-"))
                .c_child(Components.TextWithDetails("- Id do cliente: ", model.getCliente() != null ? model.getCliente().getId() : "-"))
                .c_child(Components.TextWithDetails("- Id da venda: ", model.getVendaId()))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Data de vencimento: ", DateUtils.millisToBrazilianDate(model.getDataVencimento())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }

    private Component paymentSection() {
        return Show.when(vm.modoRecebimento, () ->
                new Card(
                        new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                                .c_child(new Text("Registrar Recebimento", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                                .c_child(
                                        new Row(new RowProps().spacingOf(12).bottomVertically())
                                                .r_child(
                                                        new Column(new ColumnProps())
                                                                .c_child(new Text("Valor do Recebimento:", new TextProps().fontSize(ThemeManager.theme().typography().body())))
                                                                .c_child(Components.InputColumnCurrency("Valor", vm.valorRecebimento))
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
                                                                        ).onClick(() -> vm.registrarRecebimento(ctx))
                                                                )
                                                                .r_child(
                                                                        new Button("Cancelar",
                                                                                new ButtonProps()
                                                                                        .height(35)
                                                                                        .fontSize(theme.typography().small())
                                                                                        .bgColor("#6c757d")
                                                                                        .textColor("white")
                                                                        ).onClick(() -> {
                                                                            vm.modoRecebimento.set(false);
                                                                            vm.valorRecebimento.set("0");
                                                                        })
                                                                )
                                                )
                                )
                )
        );
    }

    private Component summarySection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                        .c_child(new Text("Resumo Financeiro", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                        .c_child(
                                new Row(new RowProps().spacingOf(16))
                                        .r_child(
                                                new Column(new ColumnProps())
                                                        .c_child(new Text("Em Aberto", new TextProps().fontSize(ThemeManager.theme().typography().body())))
                                                        .c_child(
                                                                new Text(Utils.toBRLCurrency(vm.getTotalEmAberto()),
                                                                        new TextProps().fontSize(ThemeManager.theme().typography().body()).color("#ff6b6b"))
                                                        )
                                        )
                                        .r_child(
                                                new Column(new ColumnProps())
                                                        .c_child(new Text("Vencidas", new TextProps().fontSize(ThemeManager.theme().typography().body())))
                                                        .c_child(
                                                                new Text(Utils.toBRLCurrency(vm.getTotalVencidas()),
                                                                        new TextProps().fontSize(ThemeManager.theme().typography().body()).color("#dc3545"))
                                                        )
                                        )
                        )
        );
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
                .column("Valor Original", it -> Utils.toBRLCurrency(it.getValorOriginal()))
                .column("Valor Restante", it -> Utils.toBRLCurrency(it.getValorRestante()))
                .column("Vencimento", it -> it.getDataVencimento() != null ? DateUtils.millisToBrazilianDateTime(it.getDataVencimento()) : "")
                .column("Status", it -> formatStatus(it.getStatus()))
                .build()
                .onItemSelectChange(vm.contaSelected::set)
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

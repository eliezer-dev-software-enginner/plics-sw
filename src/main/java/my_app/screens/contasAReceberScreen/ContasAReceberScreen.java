package my_app.screens.contasAReceberScreen;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.Show;
import my_app.db.models.ContaAreceberModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.components.Components;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ContasAReceberScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final ContasAReceberScreenViewModel vm;
    private final Theme theme = ThemeManager.theme();
    private final ScreenContext ctx;

    public ContasAReceberScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new ContasAReceberScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadInicial();
    }

    @Override
    public Component render() {
        var mainContent = new Container(new ContainerProps().bgColor(theme.colors().background()))
                .children(
                        summarySection(),
                        new SpacerVertical(30),
                        form(),
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
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public Component form() {
        ComputedState<Boolean> naoEhRecebimento = ComputedState.of(() -> !vm.modoRecebimento.get(), vm.modoRecebimento);

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
                                        .r_child(Components.DatePickerColumn(vm.dataVencimento, "Data Vencimento", "dd/mm/yyyy"))
                                        .r_child(Components.DatePickerColumn(vm.dataRecebimento, "Data Recebimento", "dd/mm/yyyy"))
                                        .r_child(Components.SelectColumn("Tipo Doc", vm.tipoDocumentoOptions, vm.tipoDocumento, t -> t))
                                        .r_child(Components.InputColumn("Número Doc", vm.numeroDocumento, "Número do documento"))
                        )
                        .c_child(Components.TextAreaColumn("Observação", vm.observacao, ""))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm))
                        .c_child(new Row(new RowProps().spacingOf(8))
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
                        )
        );
    }

    private Component paymentSection() {
        return Show.when(vm.modoRecebimento, () ->
                new Card(
                        new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                                .c_child(new Text("Registrar Recebimento", new TextProps().variant(TextVariant.SUBTITLE)))
                                .c_child(
                                        new Row(new RowProps().spacingOf(12).bottomVertically())
                                                .r_child(
                                                        new Column(new ColumnProps())
                                                                .c_child(new Text("Valor do Recebimento:", new TextProps().variant(TextVariant.BODY)))
                                                                .c_child(Components.InputColumnCurrency("Valor", vm.valorRecebimento))
                                                )
                                                .r_child(
                                                        new Row(new RowProps().spacingOf(8))
                                                                .r_child(
                                                                        new Button("Registrar",
                                                                                (ButtonProps) new ButtonProps()
                                                                                        .height(35)
                                                                                        .fontSize(theme.typography().small())
                                                                                        .bgColor("#10b981")
                                                                                        .textColor("white")
                                                                        ).onClick(() -> vm.registrarRecebimento(ctx))
                                                                )
                                                                .r_child(
                                                                        new Button("Cancelar",
                                                                                (ButtonProps) new ButtonProps()
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
                        .c_child(new Text("Resumo Financeiro", new TextProps().variant(TextVariant.SUBTITLE)))
                        .c_child(
                                new Row(new RowProps().spacingOf(16))
                                        .r_child(
                                                new Column(new ColumnProps())
                                                        .c_child(new Text("Em Aberto", new TextProps().variant(TextVariant.BODY)))
                                                        .c_child(
                                                                new Text(Utils.toBRLCurrency(vm.getTotalEmAberto()),
                                                                        (TextProps) new TextProps().variant(TextVariant.BODY).color("#ff6b6b"))
                                                        )
                                        )
                                        .r_child(
                                                new Column(new ColumnProps())
                                                        .c_child(new Text("Vencidas", new TextProps().variant(TextVariant.BODY)))
                                                        .c_child(
                                                                new Text(Utils.toBRLCurrency(vm.getTotalVencidas()),
                                                                        (TextProps) new TextProps().variant(TextVariant.BODY).color("#dc3545"))
                                                        )
                                        )
                        )
        );
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<ContaAreceberModel>();
        simpleTable.fromData(vm.contas)
                .header()
                .columns()
                .column("ID", it -> it.getId() != null ? "#" + it.getId() : "")
                .column("Descrição", it -> it.getDescricao())
                .column("Cliente", it -> it.getCliente() != null ? it.getCliente().getNome() : "")
                .column("Valor Original", it -> Utils.toBRLCurrency(it.getValorOriginal()))
                .column("Valor Restante", it -> Utils.toBRLCurrency(it.getValorRestante()))
                .column("Vencimento", it -> it.getDataVencimento() != null ? DateUtils.millisToBrazilianDateTime(it.getDataVencimento()) : "")
                .column("Status", it -> formatStatus(it.getStatus()))
                .build()
                .onItemSelectChange(vm.contaSelected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onClickOutside(() -> vm.contaSelected.set(null));

        return simpleTable;
    }

    private String formatStatus(String status) {
        if ("PAGO".equals(status)) return "✅ " + status;
        if ("ATRASADO".equals(status)) return "⚠️ " + status;
        if ("PARCIAL".equals(status)) return "📊 " + status;
        return "⏳ " + (status != null ? status : "");
    }
}

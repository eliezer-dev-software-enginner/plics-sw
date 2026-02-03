package my_app.screens.contasAReceberScreen;

import megalodonte.ComputedState;
import megalodonte.Show;
import megalodonte.components.*;
import megalodonte.props.ButtonProps;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.styles.TextStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ContaAreceberModel;
import my_app.domain.ContratoTelaCrud;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ContasAReceberScreen implements ScreenComponent, ContratoTelaCrud {
    private final Router router;
    private final ContasAReceberScreenViewModel vm;
    private final Theme theme = ThemeManager.theme();

    public ContasAReceberScreen(Router router) {
        this.router = router;
        this.vm = new ContasAReceberScreenViewModel();
    }

    @Override
    public void onMount() {
        vm.loadInicial();
    }

    @Override
    public Component render() {
        return mainView();
    }

    @Override
    public void handleClickNew() {
        vm.modoEdicao.set(false);
        clearForm();
    }

    @Override
    public void handleClickMenuEdit() {
        vm.modoEdicao.set(true);
        vm.editar();
    }

    @Override
    public void handleClickMenuDelete() {
        vm.modoEdicao.set(false);
        
        final var selected = vm.contaSelected.get();
        if (selected != null) {
            Components.ShowAlertAdvice("Deseja excluir \"" + selected.descricao + "\"?", () -> {
                vm.excluir(router);
            });
        }
    }

    @Override
    public void handleClickMenuClone() {
        vm.modoEdicao.set(false);
        
        final var selected = vm.contaSelected.get();
        if (selected != null) {
            vm.carregarParaEdicao(selected);
            vm.modoEdicao.set(false); // Keep as new, but with data
        }
    }

    @Override
    public void handleAddOrUpdate() {
        vm.salvarOuAtualizar(router);
    }

    @Override
    public void clearForm() {
        vm.limparFormulario();
    }

    @Override
    public Component table() {
        return new SimpleTable<ContaAreceberModel>()
                .fromData(vm.contas)
                .header()
                .columns()
                .column("ID", it-> it.id, (double) 60)
                .column("Descrição", it-> it.descricao)
                .column("Cliente", it-> it.cliente != null? it.cliente.nome: "")
                .column("Valor Original", it-> Utils.toBRLCurrency(it.valorOriginal), (double) 120)
                .column("Valor Restante", it-> Utils.toBRLCurrency(it.valorRestante), (double) 120L)
                .column("Vencimento", it-> it.dataVencimento != null? DateUtils.millisToBrazilianDateTime(it.dataVencimento) : "", (double)100L)
                .column("Status", it-> {
                    String status = it.status;
                    return switch (status) {
                        case "PAGO" -> "✅ " + status;
                        case "ATRASADO" -> "⚠️ " + status;
                        case "PARCIAL" -> "📊 " + status;
                        case null, default -> "⏳ " + status;
                    };
                }, (double)120L)
                .build()
                .onItemSelectChange(vm.contaSelected::set);

        // Style table
//        table.setStyle(String.format(
//            "-fx-font-size: %spx; " +
//            "-fx-background-color: white; " +
//            "-fx-control-inner-background: %s; " +
//            "-fx-table-cell-border-color: #e9ecef; " +
//            "-fx-table-header-border-color: #dee2e6; " +
//            "-fx-selection-bar: %s; " +
//            "-fx-selection-bar-non-focused: %s;",
//            theme.typography().body(),
//            theme.colors().surface(),
//            theme.colors().primary(),
//            "#93c5fd"
//        ));
    }

    @Override
    public Component form() {
        return formSection();
    }

    @Override
    public Component mainView() {
        var mainContent = new Column()
            .c_child(form())
            .c_child(new SpacerVertical(30))
            .c_child(paymentSection())
            .c_child(new SpacerVertical(30))
            .c_child(table());

        return new Column(new ColumnProps().paddingAll(10), new ColumnStyler().bgColor(theme.colors().background()))
            .c_child(commonCustomMenus())
            .c_child(new SpacerVertical(10))
            .c_child(Components.ScrollPaneDefault(mainContent));
    }

    private Component filtersSection() {
        vm.statusOptionSelected.subscribe(status->{
            vm.loadPorStatus(status);
        });

        return new Card(
            new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                .c_child(new Text("Filtros", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(
                    new Row(new RowProps().spacingOf(8).bottomVertically())
                        .r_child(
                                Components.SelectColumn("Status",
                                        vm.statusOptions,
                                        vm.statusOptionSelected,
                                        s->s
                                        )
                        )
                        .r_child(
                            new Button("Vencidas",
                                new ButtonProps()
                                    .height(35)
                                    .fontSize(theme.typography().small())
                                    .bgColor("#ff6b6b")
                                    .textColor("white")
                                    .onClick(() -> vm.loadVencidas()))
                        )
                        .r_child(
                            new Button("Todas",
                                new ButtonProps()
                                    .height(35)
                                    .fontSize(theme.typography().small())
                                    .bgColor("#6c757d")
                                    .textColor("white")
                                    .onClick(() -> vm.loadInicial()))
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
                                        new TextProps().variant(TextVariant.BODY), 
                                        new TextStyler().color("#ff6b6b"))
                                )
                        )
                        .r_child(
                            new Column(new ColumnProps())
                                .c_child(new Text("Vencidas", new TextProps().variant(TextVariant.BODY)))
                                .c_child(
                                    new Text(Utils.toBRLCurrency(vm.getTotalVencidas()), 
                                        new TextProps().variant(TextVariant.BODY), 
                                        new TextStyler().color("#dc3545"))
                                )
                        )
                    )
        );
    }

    private Component paymentSection() {
        return Show.when(vm.modoRecebimento,()->
            new Card(
                new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                    .c_child(new Text("Registrar Recebimento", new TextProps().variant(TextVariant.SUBTITLE)))
                    .c_child(
                        new Row(new RowProps().spacingOf(12).bottomVertically())
                            .r_child(
                                new Column(new ColumnProps())
                                    .c_child(new Text("Valor do Recebimento:", new TextProps().variant(TextVariant.BODY)))
                                    .c_child(
                                        Components.InputColumnCurrency("Valor", vm.valorRecebimento)
                                    )
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
                                                .onClick(() -> vm.registrarRecebimento(router)))
                                    )
                                    .r_child(
                                        new Button("Cancelar",
                                            new ButtonProps()
                                                .height(35)
                                                .fontSize(theme.typography().small())
                                                .bgColor("#6c757d")
                                                .textColor("white")
                                                .onClick(() -> {
                                                    vm.modoRecebimento.set(false);
                                                    vm.valorRecebimento.set("0");
                                                }))
                                    )
                            )
                        )
                    )
            );
    }

    private Component formSection() {
        ComputedState<Boolean> naoEhRecebimento = ComputedState.of(()-> vm.modoRecebimento.get() == false, vm.modoRecebimento);

        return new Card(
            new Column(new ColumnProps().paddingAll(20).spacingOf(15))
                .c_child(Components.FormTitle(vm.btnText.get()))
                .c_child(new SpacerVertical(20))
                .c_child(
                    new Row(new RowProps().spacingOf(10).bottomVertically())
                        .r_child(Components.InputColumn("Descrição", vm.descricao, "Descrição da conta"))
                        .r_child(Components.InputColumnCurrency("Valor Original", vm.valorOriginal))
                )
                .c_child(
                    new Row(new RowProps().spacingOf(10).bottomVertically())
                        .r_child(Components.DatePickerColumn(vm.dataVencimento, "Data Vencimento", "dd/mm/yyyy"))
                        .r_child(Components.DatePickerColumn(vm.dataRecebimento, "Data Recebimento", "dd/mm/yyyy"))
                )
                .c_child(
                    new Row(new RowProps().spacingOf(10).bottomVertically())
                        .r_child(
                            Components.SelectColumn("Status", vm.statusOptions, vm.status, status -> status))
                        .r_child(
                            Components.SelectColumn("Tipo Doc", vm.tipoDocumentoOptions, vm.tipoDocumento, tipo -> tipo))
                )
                .c_child(
                    Components.SelectColumn("Cliente", vm.clientes, vm.clienteSelected,
                        f -> f != null ? f.nome : "", true)
                )
                .c_child(Components.InputColumn("Número Doc", vm.numeroDocumento, "Número do documento"))
                .c_child(Components.TextAreaColumn("Observação", vm.observacao, ""))
                .c_child(new SpacerVertical(20))
                .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm))
                .c_child(new Row(new RowProps().spacingOf(8))
                        .r_child(
                                Show.when(naoEhRecebimento,
                                        () -> new Button(vm.btnRecebimentoText,
                                                new ButtonProps()
                                                        .height(35)
                                                        .fontSize(theme.typography().small())
                                                        .bgColor("#10b981")
                                                        .textColor("white")
                                                        .fillWidth()
                                                        .onClick(() -> {
                                                            if (vm.modoRecebimento.get()) {
                                                                vm.registrarRecebimento(router);
                                                            } else {
                                                                vm.modoRecebimento.set(true);
                                                            }
                                                        }))
                                )
                        )
                        .r_child(
                            new Button("Quitar",
                                new ButtonProps()
                                    .height(35)
                                    .fontSize(theme.typography().small())
                                    .bgColor("#007bff")
                                    .textColor("white")
                                    .onClick(() -> vm.quitarConta(router)))
                        )
                )
        );
    }

}
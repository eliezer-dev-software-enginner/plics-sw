package my_app.screens.comprasAPagarScreen;


import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.v2.Show;
import my_app.db.models_old.ContasPagarModel;
import my_app.domain.ContratoTelaCrud;
import my_app.domain.components.Components;
//import javafx.scene.control.*;
import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.utils.related.TextVariant;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;

//TODO: REFATORAR NA V4
public class ComprasAPagarScreen implements ScreenComponent, ContratoTelaCrud {
    private final ComprasAPagarScreenViewModel vm;
    private final Theme theme = ThemeManager.theme();
    private final ScreenContext ctx;

    public ComprasAPagarScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new ComprasAPagarScreenViewModel();
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
                vm.excluir(ctx);
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
        vm.salvarOuAtualizar(ctx);
    }

    @Override
    public void clearForm() {
        vm.limparFormulario();
    }

    @Override
    public Component table() {
        return contasTable();
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

        return new Container(new ContainerProps().paddingAll(10).bgColor(theme.colors().background()))
            .c_child(commonCustomMenus())
            .c_child(new SpacerVertical(10))
            .c_child(Components.ScrollPaneDefault(mainContent));
    }

    private Component paymentSection() {
        return Show.when(vm.modoPagamento,()->
            new Card(
                new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                    .c_child(new Text("Registrar Pagamento", new TextProps().variant(TextVariant.SUBTITLE)))
                    .c_child(
                        new Row(new RowProps().spacingOf(12).bottomVertically())
                            .r_child(
                                new Column(new ColumnProps())
                                    .c_child(new Text("Valor do Pagamento:", new TextProps().variant(TextVariant.BODY)))
                                    .c_child(
                                        Components.InputColumnCurrency("Valor", vm.valorPagamento)
                                    )
                            )
                            .r_child(
                                new Row(new RowProps().spacingOf(8))
                                    .r_child(
                                        new Button("Registrar",
                                                (ButtonProps) new ButtonProps()
                                                .height(35)
                                                .fontSize(theme.typography().small())
                                                .bgColor("#10b981")
                                                .textColor("white"))
                                                .onClick(() -> vm.registrarPagamento(ctx))
                                    )
                                    .r_child(
                                        new Button("Cancelar",
                                                (ButtonProps) new ButtonProps()
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

    private Component formSection() {
        ComputedState<Boolean> naoEhPagamento = ComputedState.of(()-> vm.modoPagamento.get() == false, vm.modoPagamento);

        return new Card(
            new Column(new ColumnProps().paddingAll(20).spacingOf(15))
                .c_child(Components.FormTitle(vm.btnText.get()))
                .c_child(new SpacerVertical(20))
                .c_child(
                    new Row(new RowProps().spacingOf(10).bottomVertically())
                        .r_child(Components.InputColumn("Descrição", vm.descricao, "Descrição da conta"))
                        .r_child(Components.InputColumnCurrency("Valor Original", vm.valorOriginal))
                            .r_child( Components.SelectColumn("Fornecedor", vm.fornecedores, vm.fornecedorSelected,
                                    f -> f != null ? f.nome : "", true))
                            .r_child(
                                    Components.SelectColumn("Status", vm.statusOptions, vm.status, status -> status))
                )
                .c_child(
                    new Row(new RowProps().spacingOf(10).bottomVertically())
                        .r_child(Components.DatePickerColumn(vm.dataVencimento, "Data Vencimento", "dd/mm/yyyy"))
                        .r_child(Components.DatePickerColumn(vm.dataPagamento, "Data Pagamento", "dd/mm/yyyy"))
                            .r_child(
                                    Components.SelectColumn("Tipo Doc", vm.tipoDocumentoOptions, vm.tipoDocumento, tipo -> tipo))
                            .r_child(Components.InputColumn("Número Doc", vm.numeroDocumento, "Número do documento"))
                )
                .c_child(
                    new Row(new RowProps().spacingOf(10).bottomVertically())

                )

                .c_child(Components.TextAreaColumn("Observação", vm.observacao, ""))
                .c_child(new SpacerVertical(20))
                .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm))
                .c_child(new Row(new RowProps().spacingOf(8))
                        .r_child(
                                Show.when(naoEhPagamento, () -> new Button(
                                        vm.btnPagamentoText,
                                        new ButtonProps()
                                                .height(35)
                                                .fontSize(theme.typography().small())
                                                .bgColor("#10b981")
                                                .textColor("white")
                                                //.fillWidth()
                                              ) .onClick(() -> {
                                    if (vm.modoPagamento.get()) {
                                        vm.registrarPagamento(ctx);
                                    } else {
                                        vm.modoPagamento.set(true);
                                    }
                                }))
                        )
                        .r_child(
                            new Button("Quitar",
                                    new ButtonProps()
                                    .height(35)
                                    .fontSize(theme.typography().small())
                                    .bgColor("#007bff")
                                    .textColor("white")).onClick(() -> vm.quitarConta(ctx))
                        )
                )
        );
    }

    private Component contasTable() {
        TableView<ContasPagarModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(vm.contas);

        // ID Column
        TableColumn<ContasPagarModel, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().id != null ? String.valueOf(data.getValue().id) : ""
            )
        );
        idCol.setMaxWidth(60);

        // Description Column
        TableColumn<ContasPagarModel, String> descricaoCol = new TableColumn<>("Descrição");
        descricaoCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().descricao)
        );

        // Fornecedor Column
        TableColumn<ContasPagarModel, String> fornecedorCol = new TableColumn<>("Fornecedor");
        fornecedorCol.setCellValueFactory(data -> {
            var fornecedor = data.getValue().fornecedor;
            if (fornecedor != null) {
                return new javafx.beans.property.SimpleStringProperty(fornecedor.nome);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Valor Original Column
        TableColumn<ContasPagarModel, String> valorOriginalCol = new TableColumn<>("Valor Original");
        valorOriginalCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().valorOriginal != null ?
                    NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                        .format(data.getValue().valorOriginal) : "R$ 0,00"
            )
        );
        valorOriginalCol.setMaxWidth(120);

        // Valor Restante Column
        TableColumn<ContasPagarModel, String> valorRestanteCol = new TableColumn<>("Valor Restante");
        valorRestanteCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().valorRestante != null ?
                    NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                        .format(data.getValue().valorRestante) : "R$ 0,00"
            )
        );
        valorRestanteCol.setMaxWidth(120);

        // Data Vencimento Column
        TableColumn<ContasPagarModel, String> dataVencimentoCol = new TableColumn<>("Vencimento");
        dataVencimentoCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().dataVencimento != null ?
                        DateUtils.millisToBrazilianDateTime(data.getValue().dataVencimento) : ""
            )
        );
        dataVencimentoCol.setMaxWidth(100);

        // Status Column with color
        TableColumn<ContasPagarModel, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().status;
            if ("PAGO".equals(status)) {
                return new javafx.beans.property.SimpleStringProperty("✅ " + status);
            } else if ("ATRASADO".equals(status)) {
                return new javafx.beans.property.SimpleStringProperty("⚠️ " + status);
            } else if ("PARCIAL".equals(status)) {
                return new javafx.beans.property.SimpleStringProperty("📊 " + status);
            } else {
                return new javafx.beans.property.SimpleStringProperty("⏳ " + status);
            }
        });
        statusCol.setMaxWidth(120);

        table.getColumns().addAll(
            idCol, descricaoCol, fornecedorCol, valorOriginalCol, 
            valorRestanteCol, dataVencimentoCol, statusCol
        );

        // Style table
        table.setStyle(String.format(
            "-fx-font-size: %spx; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: %s; " +
            "-fx-table-cell-border-color: #e9ecef; " +
            "-fx-table-header-border-color: #dee2e6; " +
            "-fx-selection-bar: %s; " +
            "-fx-selection-bar-non-focused: %s;",
            theme.typography().body(),
            theme.colors().surface(),
            theme.colors().primary(),
            "#93c5fd"
        ));

        Utils.onItemTableSelectedChange(table, vm.contaSelected::set);

        return Component.CreateFromJavaFxNode(table);
    }
}
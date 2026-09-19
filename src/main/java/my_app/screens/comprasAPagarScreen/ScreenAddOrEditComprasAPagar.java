package my_app.screens.comprasAPagarScreen;

import disgust.io.br.Pack;
import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.components.Button;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ButtonProps;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.ContasPagarModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ContasPagarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ScreenAddOrEditComprasAPagar extends ScreenAddOrEdit<ContasPagarModel, ComprasAPagarScreenViewModel> {
    
    public ScreenAddOrEditComprasAPagar(ScreenContext screenContext) {
        super(screenContext);
    }
    
    @Override
    public Component render() {
        ComputedState<Boolean> naoEhPagamento = ComputedState.of(() -> !viewModel.modoPagamento.get(), viewModel.modoPagamento);

        return new Card(
                new Column(new ColumnProps().paddingAll(20).spacingOf(15))
                        .c_child(Components.FormTitle(viewModel.btnText.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(disgust.io.Pack.InputColumn("Descrição", viewModel.descricao, "Descrição da conta"))
                                        .r_child(Pack.InputColumnCurrency("Valor Original", viewModel.valorOriginal))
                                        .r_child(Components.SelectColumn("Fornecedor", viewModel.fornecedores, viewModel.fornecedorSelected,
                                                f -> f != null ? f.getNome() : "", true))
                                        .r_child(Components.SelectColumn("Status", viewModel.statusOptions, viewModel.status, s -> s))
                        )
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.DatePickerColumn(viewModel.dataVencimento, "Data Vencimento"))
                                        .r_child(Components.DatePickerColumn(viewModel.dataPagamento, "Data Pagamento"))
                                        .r_child(Components.SelectColumn("Tipo Doc", viewModel.tipoDocumentoOptions, viewModel.tipoDocumento, t -> t))
                                        .r_child(disgust.io.Pack.InputColumn("Número Doc", viewModel.numeroDocumento, "Número do documento"))
                        )
                        .c_child(Components.TextAreaColumn("Observação", viewModel.observacao, "Alguma observação sobre esta conta?"))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(viewModel.btnText, this::handleAddOrUpdate))
                        .c_child(new Row(new RowProps().spacingOf(8))
                                .r_child(
                                        Show.when(naoEhPagamento, () -> new Button(
                                                viewModel.btnPagamentoText,
                                                new ButtonProps()
                                                        .height(35)
                                                        .fontSize(theme.typography().small())
                                                        .bgColor("#10b981")
                                                        .textColor("white")
                                        ).onClick(() -> {
                                            if (viewModel.modoPagamento.get()) {
                                                viewModel.registrarPagamento(screenContext);
                                            } else {
                                                viewModel.modoPagamento.set(true);
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
                                        ).onClick(() -> viewModel.quitarConta(screenContext))
                                )
                        )
        );
    }

    @Override
    protected ComprasAPagarScreenViewModel getViewModel(ScreenContext screenContext) {
        return new ComprasAPagarScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<ContasPagarModel> getService() throws SQLException {
        return new ContasPagarService();
    }

    @Override
    protected String getTitle() {
        return "compra a pagar";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ScreenAddOrEditComprasAPagar.class);
    }
}

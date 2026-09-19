package my_app.screens.contasAReceberScreen;

import disgust.io.br.Pack;
import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Button;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ButtonProps;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.ContaAreceberModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ContaAreceberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;

import java.sql.SQLException;

public class ScrenAddOrEditContasAReceber extends ScreenAddOrEdit<ContaAreceberModel, ContasAReceberScreenViewModel> {

    public ScrenAddOrEditContasAReceber(ScreenContext screenContext) {
        super(screenContext);
    }

    @Override
    public Component render() {
        ComputedState<Boolean> naoEhRecebimento = ComputedState.of(() -> !viewModel.modoRecebimento.get(), viewModel.modoRecebimento);

        // mainView() (ScreenContract) só chama form()/table() — não tem mais slot
        // próprio pra summarySection()/paymentSection() desde que render() passou a
        // devolver mainView() direto, então as duas entram aqui, em volta do Card do
        // formulário, pra continuar aparecendo na tela.
        return Components.ScrollPaneDefault(
                new Column(new ColumnProps().spacingOf(20)).children(
                        summarySection(),
                        formCard(naoEhRecebimento),
                        paymentSection()
                )
        ) ;
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
                                                                new Text(CurrencyPack.toBRLCurrency(viewModel.getTotalEmAberto()),
                                                                        new TextProps().fontSize(ThemeManager.theme().typography().body()).textColor("#ff6b6b"))
                                                        )
                                        )
                                        .r_child(
                                                new Column(new ColumnProps())
                                                        .c_child(new Text("Vencidas", new TextProps().fontSize(ThemeManager.theme().typography().body())))
                                                        .c_child(
                                                                new Text(CurrencyPack.toBRLCurrency(viewModel.getTotalVencidas()),
                                                                        new TextProps().fontSize(ThemeManager.theme().typography().body()).textColor("#dc3545"))
                                                        )
                                        )
                        )
        );
    }

    private Component formCard(ComputedState<Boolean> naoEhRecebimento) {
        return new Card(
                new Column(new ColumnProps().paddingAll(20).spacingOf(15))
                        .c_child(new Text("Dados da conta a receber",
                                new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()))
                        .c_child(new SpacerVertical(20))
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(disgust.io.Pack.InputColumn("Descrição", viewModel.descricao, "Descrição da conta"))
                                        .r_child(Pack.InputColumnCurrency("Valor Original", viewModel.valorOriginal))
                                        .r_child(Components.SelectColumn("Cliente", viewModel.clientes, viewModel.clienteSelected,
                                                f -> f != null ? f.getNome() : "", true))
                                        .r_child(Components.SelectColumn("Status", viewModel.statusOptions, viewModel.status, s -> s))
                        )
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.DatePickerColumn(viewModel.dataVencimento, "Data Vencimento"))
                                        .r_child(Components.DatePickerColumn(viewModel.dataRecebimento, "Data Recebimento"))
                                        .r_child(Components.SelectColumn("Tipo Doc", viewModel.tipoDocumentoOptions, viewModel.tipoDocumento, t -> t))
                                        .r_child(disgust.io.Pack.InputColumn("Número Doc", viewModel.numeroDocumento, "Número do documento"))
                        )
                        .c_child(Components.TextAreaColumn("Observação", viewModel.observacao, "Alguma observação sobre esta conta?"))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(viewModel.btnText, this::handleAddOrUpdate))
                        .c_child(Show.when(viewModel.modoEdicaoState(), () -> new Row(new RowProps().spacingOf(8))
                                .r_child(
                                        Show.when(naoEhRecebimento, () -> new Button(
                                                viewModel.btnRecebimentoText,
                                                new ButtonProps()
                                                        .height(35)
                                                        .fontSize(theme.typography().small())
                                                        .bgColor("#10b981")
                                                        .textColor("white")
                                        ).onClick(() -> {
                                            if (viewModel.modoRecebimento.get()) {
                                                viewModel.registrarRecebimento(screenContext);
                                            } else {
                                                viewModel.modoRecebimento.set(true);
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
                        ))
        );
    }

    private Component paymentSection() {
        return Show.when(viewModel.modoRecebimento, () ->
                new Card(
                        new Column(new ColumnProps().paddingAll(16).spacingOf(12))
                                .c_child(new Text("Registrar Recebimento", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                                .c_child(
                                        new Row(new RowProps().spacingOf(12).bottomVertically())
                                                .r_child(
                                                        new Column(new ColumnProps())
                                                                .c_child(new Text("Valor do Recebimento:", new TextProps().fontSize(ThemeManager.theme().typography().body())))
                                                                .c_child(Pack.InputColumnCurrency("Valor", viewModel.valorRecebimento))
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
                                                                        ).onClick(() -> viewModel.registrarRecebimento(screenContext))
                                                                )
                                                                .r_child(
                                                                        new Button("Cancelar",
                                                                                new ButtonProps()
                                                                                        .height(35)
                                                                                        .fontSize(theme.typography().small())
                                                                                        .bgColor("#6c757d")
                                                                                        .textColor("white")
                                                                        ).onClick(() -> {
                                                                            viewModel.modoRecebimento.set(false);
                                                                            viewModel.valorRecebimento.set("0");
                                                                        })
                                                                )
                                                )
                                )
                )
        );
    }

    @Override
    protected ContasAReceberScreenViewModel getViewModel(ScreenContext screenContext) {
        return new ContasAReceberScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<ContaAreceberModel> getService() throws SQLException {
        return new ContaAreceberService();
    }

    @Override
    protected String getTitle() {
        return "compra a receber";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ScrenAddOrEditContasAReceber.class);
    }
}

package my_app.screens.fornecedorScreen;

import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Card;
import megalodonte.components.LineHorizontal;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.FlowRowProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.Show;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.FornecedorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ScreenAddOrEditFornecedor extends ScreenAddOrEdit<FornecedorModel, FornecedorScreenViewModel> {

    public ScreenAddOrEditFornecedor(ScreenContextInterface screenContext) {
        super(screenContext);
    }

    @Override
    public Component render() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally())
                                .r_child(new Text("Cadastro de Fornecedor", new TextProps().fontSize(ThemeManager.theme().typography().subtitle()).bold())))
                        .c_child(new SpacerVertical(20))
                        .c_child(informacoesPessoais())
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.enderecoComponent(viewModel.enderecoState.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(new LineHorizontal())
                        .c_child(Components.TextAreaColumn("Observação", viewModel.observacao, "Alguma observação sobre o fornecedor?"))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButton(getBtnActionText(), this::handleAddOrUpdate)));
    }

    private Component informacoesPessoais() {
        return new FlowRow(new FlowRowProps().spacingOf(10))
                .r_child(disgust.io.Pack.InputColumn("Nome Fantasia *", viewModel.nome, "Ex: Empresa 123"))
                .r_child(Components.SelectColumn("Tipo de pessoa", Data.tiposPessoaList, viewModel.tipoPessoaSelected, it -> it))
                .r_child(Show.when(viewModel.tipoPessoaEhFisica,
                        () -> Pack.InputColumnCpf("CPF", viewModel.cnpjCpf),
                        () -> Pack.InputColumnCnpjAlfanumerico("CNPJ", viewModel.cnpjCpf)
                ))
                .r_child(Pack.InputColumnPhone("Celular", viewModel.celular))
                .r_child(disgust.io.Pack.InputColumn("Inscrição estadual", viewModel.inscricaoEstadual, "Ex: 123.456.789.123"))
                .r_child(disgust.io.Pack.InputColumn("Email", viewModel.email, "Ex: email@teste.com"));
    }

    @Override
    protected FornecedorScreenViewModel getViewModel(ScreenContextInterface screenContext) {
        return new FornecedorScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<FornecedorModel> getService() throws SQLException {
        return new FornecedorService();
    }

    @Override
    protected String getTitle() {
        return "fornecedor";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ScreenAddOrEditFornecedor.class);
    }
}

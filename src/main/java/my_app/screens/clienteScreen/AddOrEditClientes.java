package my_app.screens.clienteScreen;

import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.components.Card;
import megalodonte.components.LineHorizontal;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.props.ColumnProps;
import megalodonte.props.FlowRowProps;
import megalodonte.router.v5.ScreenContext;
import megalodonte.v2.Show;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.ClienteService;
import org.slf4j.Logger;

import java.sql.SQLException;

public class AddOrEditClientes extends ScreenAddOrEdit<ClienteModel, ClienteViewModel> {

    public AddOrEditClientes(ScreenContext screenContext) {
        super(screenContext);
    }

    @Override
    public Component render() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(Components.FormSubtitle("Cadastrar cliente"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new FlowRow(new FlowRowProps().spacingOf(10))
                                .children(
                                        disgust.io.Pack.InputColumn("Nome *", viewModel.nome, "Ex: João"),
                                        Components.SelectColumn("Tipo de pessoa", Data.tiposPessoaList, viewModel.tipoPessoaSelected, it -> it),
                                        Show.when(viewModel.tipoPessoaEhFisica,
                                                () -> Pack.InputColumnCpf("CPF", viewModel.cnpjCpf),
                                                () -> Pack.InputColumnCnpjAlfanumerico("CNPJ", viewModel.cnpjCpf)
                                        ),
                                        Pack.InputColumnPhone("Celular", viewModel.celular),
                                        disgust.io.Pack.InputColumn("Email", viewModel.email, "Ex: email@teste.com"),
                                        Components.DatePickerColumn(viewModel.dataNascimento,"Data de nascimento"),
                                        Components.SelectColumn("É gestante?", Data.simNaoList, viewModel.isGestante, it -> it),
                                        Show.when(viewModel.isGestanteComputed,()->Components.DatePickerColumn(viewModel.dataNascimentoBebe,"Data de nascimento do bebê") )
                                )
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(Components.enderecoComponent(viewModel.enderecoState.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(new LineHorizontal())
                        .c_child(Components.TextAreaColumn("Observação", viewModel.observacao, "Alguma observação sobre o cliente?", 60, 160))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(viewModel.btnText, this::handleAddOrUpdate))
        );
    }

    @Override
    protected ClienteViewModel getViewModel(ScreenContext screenContext) {
        return new ClienteViewModel(screenContext);
    }

    @Override
    protected BaseService<ClienteModel> getService() throws SQLException {
        return new ClienteService();
    }

    @Override
    protected String getTitle() {
        return "cliente";
    }

    @Override
    protected Logger getLogger() {
        return null;
    }
}

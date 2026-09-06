package my_app.screens.clienteScreen;

import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.props.ColumnProps;
import megalodonte.props.FlowRowProps;
import megalodonte.props.TextProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.ClienteModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import pack.utilities.DatePack;
import pack.utilities.FormatterPack;

public class ClienteScreen implements ScreenComponent, ContratoTelaCrudV3<ClienteModel> {
    private final ClienteViewModel vm;
    private final ScreenContext screenContext;

    public ClienteScreen(ScreenContext ctx) {
        this.screenContext = ctx;
        this.vm = new ClienteViewModel(ctx);
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
    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(Components.FormTitle("Cadastrar cliente"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new FlowRow(new FlowRowProps().spacingOf(10))
                                .children(
                                        Components.InputColumn("Nome", vm.nome, "Ex: João"),
                                        Components.SelectColumn("Tipo de pessoa", Data.tiposPessoaList, vm.tipoPessoaSelected, it -> it),
                                        Show.when(vm.tipoPessoaEhFisica,
                                                () -> Pack.InputColumnCpf("CPF", vm.cnpjCpf),
                                                () -> Pack.InputColumnCnpjAlfanumerico("CNPJ", vm.cnpjCpf)
                                        ),
                                        Pack.InputColumnPhone("Celular", vm.celular),
                                        Components.InputColumn("Email", vm.email, "Ex: email@teste.com"),
                                        Components.DatePickerColumn(vm.dataNascimento,"Data de nascimento"),
                                        Components.SelectColumn("É gestante?", Data.simNaoList, vm.isGestante, it -> it),
                                        Show.when(vm.isGestanteComputed,()->Components.DatePickerColumn(vm.dataNascimentoBebe,"Data de nascimento do bebê") )
                                )
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(Components.enderecoComponent(vm.enderecoState.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(new LineHorizontal())
                        .c_child(Components.TextAreaColumn("Observação", vm.observacao, "Alguma observação sobre o cliente?", 60, 160))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate))
        );
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public SimpleTable<ClienteModel> table() {
        var simpleTable = new SimpleTable<ClienteModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", ClienteModel::getId)
                .column("Nome", ClienteModel::getNome)
                .column("Celular/Telefone",          it -> FormatterPack.formatPhone(it.getCelular()))
                .column("Email", ClienteModel::getEmail)
                .column("CPF/CNPJ",      it -> it.getCpfCnpj().length() == 11
                        ? FormatterPack.formatCpf(it.getCpfCnpj())
                        : FormatterPack.formatCnpj(it.getCpfCnpj()))
                .column("Data de criação", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.clienteSelecionado::set)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), this.screenContext, 400));

        return simpleTable;
    }

    public Component itemDetails(ClienteModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do cliente", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Nome: ", model.getNome()))
                .c_child(Components.TextWithDetails("CPF/CNPJ: ", model.getCpfCnpj()))
                .c_child(Components.TextWithDetails("Email: ", model.getEmail()))
                .c_child(Components.TextWithDetails("Telefone: ",FormatterPack.formatPhone(model.getCelular())))
                .c_child(Components.TextWithDetails("Data de nascimento: ", DatePack.millisToBrazilianDate(model.getDataNascimento())))
                .c_child(Components.TextWithDetails("É gestante: ", model.getGestanteText()))
                .c_child(Show.when(model.getGestante()!=null && model.getGestante(), ()-> new Container().children(
                        Components.TextWithDetails("Data de nascimento do bebê: ", DatePack.millisToBrazilianDate(model.getDataNascimentoBebe()))
                )))
                .c_child(Components.ItemDetailEndereco(model.getEndereco()))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }
}

//
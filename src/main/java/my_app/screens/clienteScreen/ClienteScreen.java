package my_app.screens.clienteScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.Show;
import my_app.db.models.ClienteModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ClienteScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final ClienteViewModel vm;
    private final ScreenContext screenContext;

    public ClienteScreen(ScreenContext ctx) {
        this.screenContext = ctx;
        this.vm = new ClienteViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadClientes();
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
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome", vm.nome, "Ex: João"))
                                .r_child(Components.SelectColumn("Tipo de pessoa", Data.tiposPessoaList, vm.tipoPessoaSelected, it -> it))
                                .r_child(Show.when(vm.tipoPessoaEhFisica,
                                        () -> Components.InputColumnCpf("CPF", vm.cnpjCpf),
                                        () -> Components.InputColumnCnpjAlfanumerico("CNPJ", vm.cnpjCpf)
                                ))
                                .r_child(Components.InputColumnPhone("Celular", vm.celular))
                                .r_child(Components.InputColumn("Email", vm.email, ""))
                                .r_child(Components.DatePickerColumn(vm.dataNascimento,"Data de nascimento"))
                        )
                        .c_child(
                                new Row(new RowProps().spacingOf(10)).children(
                                        Components.SelectColumn("É gestante?", Data.simNaoList, vm.isGestante, it -> it),
                                        Show.when(vm.isGestanteComputed,()->Components.DatePickerColumn(vm.dataNascimentoBebe,"Data de nascimento do bebê") )
                                )
                        )
                        .c_child(new SpacerVertical(10))
                        .c_child(Components.enderecoComponent(vm.enderecoState.get()))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, vm::clearForm))
        );
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<ClienteModel>();
        simpleTable.fromData(vm.clientes)
                .header()
                .columns()
                .column("ID", ClienteModel::getId)
                .column("Nome", ClienteModel::getNome)
                .column("Celular/Telefone",          it -> Utils.formatPhone(it.getCelular()))
                .column("Email", ClienteModel::getEmail)
                .column("CPF/CNPJ",      it -> it.getCpfCnpj().length() == 11
                        ? Utils.formatCpf(it.getCpfCnpj())
                        : Utils.formatCnpj(it.getCpfCnpj()))
                .column("Data de criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.clienteSelecionado::set)
                .onItemDoubleClick(it -> Components.ShowModal(ItemDetails(it), this.screenContext, 400));

        return simpleTable;
    }

    Component ItemDetails(ClienteModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do cliente", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Nome: ", model.getNome()))
                .c_child(Components.TextWithDetails("CPF/CNPJ: ", model.getCpfCnpj()))
                .c_child(Components.TextWithDetails("Email: ", model.getEmail()))
                .c_child(Components.TextWithDetails("Telefone: ",Utils.formatPhone(model.getCelular())))
                .c_child(Components.TextWithDetails("Data de nascimento: ", DateUtils.millisToBrazilianDate(model.getDataNascimento())))
                .c_child(Components.TextWithDetails("É gestante: ", model.getGestanteText()))
                .c_child(Show.when(model.getGestante()!=null && model.getGestante(), ()-> new Container().children(
                        Components.TextWithDetails("Data de nascimento do bebê: ", DateUtils.millisToBrazilianDate(model.getDataNascimentoBebe()))
                )))
                .c_child(Components.ItemDetailEndereco(model.getEndereco()))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }
}

//
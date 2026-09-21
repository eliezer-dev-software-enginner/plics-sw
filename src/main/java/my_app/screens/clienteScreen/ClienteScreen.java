package my_app.screens.clienteScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.Show;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.ClienteModel;
import pack.utilities.DatePack;
import pack.utilities.FormatterPack;

public class ClienteScreen implements ScreenComponent, ScreenContract<ClienteModel> {
    private final ClienteViewModel vm;
    private final ScreenContextInterface screenContext;

    public ClienteScreen(ScreenContextInterface ctx) {
        this.screenContext = ctx;
        this.vm = new ClienteViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ScreenContract.super.onDestroy();
    }

    @Override
    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public ViewModelScreenContract<ClienteModel> viewModel() {
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
                .column("Data de nascimento", it -> it.getDataNascimento() != null ? DatePack.millisToBrazilianDate(it.getDataNascimento()) : "")
                .column("Gestante", ClienteModel::getGestanteText)
                .column("Data nasc. bebê", it -> it.getDataNascimentoBebe() != null ? DatePack.millisToBrazilianDate(it.getDataNascimentoBebe()) : "")
                .column("UF", it -> it.getUf() != null ? it.getUf() : "")
                .column("CEP", it -> it.getCep() != null ? FormatterPack.formatCep(it.getCep()) : "")
                .column("Cidade", it -> it.getCidade() != null ? it.getCidade() : "")
                .column("Bairro", it -> it.getBairro() != null ? it.getBairro() : "")
                .column("Rua", it -> it.getRua() != null ? it.getRua() : "")
                .column("Número", it -> it.getNumero() != null ? it.getNumero() : "")
                .column("Observação", ClienteModel::getObservacao)
                .column("Data de criação", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.selected::set)
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
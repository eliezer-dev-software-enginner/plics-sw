package my_app.screens.fornecedorScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.props.TextProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.FornecedorModel;
import pack.utilities.DatePack;
import pack.utilities.FormatterPack;

public class FornecedorScreen implements ScreenComponent, ScreenContract<FornecedorModel> {
    private final FornecedorScreenViewModel vm;
    private final ScreenContextInterface ctx;

    public FornecedorScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.vm = new FornecedorScreenViewModel(ctx);
    }

    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ScreenContract.super.onDestroy();
    }

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public ViewModelScreenContract<FornecedorModel> viewModel() {
        return vm;
    }

    @Override
    public SimpleTable<FornecedorModel> table() {
        return new SimpleTable<FornecedorModel>()
                .fromData(vm.filteredList)
                .header().columns()
                .column("ID", FornecedorModel::getId)
                .column("Nome", FornecedorModel::getNome)
                .column("Telefone", it -> FormatterPack.formatPhone(it.getCelular()))
                .column("CPF/CNPJ",      it -> it.getCpfCnpj().length() == 11
                        ? FormatterPack.formatCpf(it.getCpfCnpj())
                        : FormatterPack.formatCnpj(it.getCpfCnpj()))
                .column("Email", FornecedorModel::getEmail)
                .column("Insc. Estadual", it -> it.getInscricaoEstadual() != null ? it.getInscricaoEstadual() : "")
                .column("UF", it -> it.getUfSelected() != null ? it.getUfSelected() : "")
                .column("CEP", it -> it.getCep() != null ? FormatterPack.formatCep(it.getCep()) : "")
                .column("Cidade", it -> it.getCidade() != null ? it.getCidade() : "")
                .column("Bairro", it -> it.getBairro() != null ? it.getBairro() : "")
                .column("Rua", it -> it.getRua() != null ? it.getRua() : "")
                .column("Número", it -> it.getNumero() != null ? it.getNumero() : "")
                .column("Observação", FornecedorModel::getObservacao)
                .column("Data de Criação", it -> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .end()
                .build()
                .onItemSelectChange(vm.selected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), this.ctx, 550));
    }

    public Component itemDetails(FornecedorModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do fornecedor", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Nome: ", model.getNome()))
                .c_child(Components.TextWithDetails("CPF/CNPJ: ", model.getCpfCnpj()))
                .c_child(Components.TextWithDetails("Telefone: ",FormatterPack.formatPhone(model.getCelular())))
                .c_child(Components.TextWithDetails("Inscrição estadual: ", model.getInscricaoEstadual()))
                .c_child(Components.TextWithDetails("Email: ", model.getEmail()))
                .c_child(Components.ItemDetailEndereco(model.getEndereco()))
                .c_child(Components.TextWithDetails("Data de criação: ", DatePack.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }
}

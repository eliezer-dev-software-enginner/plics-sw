package my_app.screens.clienteScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.ClienteModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class ClienteScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final ClienteViewModel vm;

    public ClienteScreen(ScreenContext ctx) {
        this.vm = new ClienteViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadClientes();
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
                                        () -> Components.InputColumnCnpj("CNPJ", vm.cnpjCpf)
                                ))
                                .r_child(Components.InputColumnPhone("Celular", vm.celular))
                                .r_child(Components.InputColumn("Email", vm.email, ""))
                        )
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
                .column("ID",            it -> it.getId())
                .column("Nome",          it -> it.getNome())
                .column("CPF/CNPJ",      it -> it.getCpfCnpj().length() == 11
                        ? Utils.formatCpf(it.getCpfCnpj())
                        : Utils.formatCnpj(it.getCpfCnpj()))
                .column("Data de criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(it -> vm.clienteSelecionado.set(it));

        return simpleTable;
    }
}
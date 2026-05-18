package my_app.screens.clienteScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.ClienteModel;
import my_app.domain.ContratoTelaCrud;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class PDVScreen implements ScreenComponent {

    private final PDVScreenViewModel vm;

    public PDVScreen(ScreenContext ctx) {
        this.vm = new PDVScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadClientes();
    }

    @Override
    public Component render() {
        return new Container()
                .children(Components.FormTitle("Caixa Aberto"),
                        new Row().children(
                                produtoForm(),
                                table()
                        )
                );
    }


    Component produtoForm(){
        return new Card(
                new Row().children(
                        //imagem,
                        new Column().children(
                                //TODO: a medida que for digitando deverá ir exeibindo a lista de produtos encontrados abaixo
                                Components.InputColumn("Código de barras", vm.nome, "Ex: João"),
                                Components.InputColumn("Preço unitário", vm.nome, "Ex: João"),
                                Components.InputColumn("Total do item", vm.nome, "Ex: João")
                        )
                )


        );
    }

    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(Components.FormTitle("Cadastrar cliente"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome", vm.nome, "Ex: João"))
                                .r_child(Components.SelectColumn("Tipo de pessoa", vm.tipoPessoaList, vm.tipoPessoaSelected, it -> it))
                                .r_child(Show.when(vm.tipoPessoaEhFisica,
                                        () -> Components.InputColumnCpf("CPF", vm.cnpjCpf),
                                        () -> Components.InputColumnCnpj("CNPJ", vm.cnpjCpf)
                                ))
                                .r_child(Components.InputColumnPhone("Celular", vm.celular))
                                .r_child(Components.InputColumn("Email", vm.email, ""))
                        )
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, vm::handleAddOrUpdate, vm::clearForm))
        );
    }


    public Component table() {
        var simpleTable = new SimpleTable<ClienteModel>();
        simpleTable.fromData(vm.clientes)
                .header()
                .columns()
                .column("ID",            it -> it.id)
                .column("Nome",          it -> it.nome)
                .column("CPF/CNPJ",      it -> it.cpfCnpj.length() == 11
                        ? Utils.formatCpf(it.cpfCnpj)
                        : Utils.formatCnpj(it.cpfCnpj))
                .column("Data de criação", it -> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(it -> vm.clienteSelecionado.set(it));

        return simpleTable;
    }
}
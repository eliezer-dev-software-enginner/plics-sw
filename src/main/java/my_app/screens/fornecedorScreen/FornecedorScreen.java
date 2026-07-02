package my_app.screens.fornecedorScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.Show;
import my_app.db.models.FornecedorModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

public class FornecedorScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final FornecedorScreenViewModel vm;
    private final ScreenContext ctx;

    public FornecedorScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new FornecedorScreenViewModel(ctx);
    }

    public void onMount() {
        vm.loadFornecedores();
    }

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally())
                                .r_child(new Text("Cadastro de Fornecedor", (TextProps) new TextProps().variant(TextVariant.SUBTITLE).bold())))
                        .c_child(new SpacerVertical(20))
                        .c_child(informacoesPessoais())
                        .c_child(new SpacerVertical(20))
                        .c_child(new Column().c_child(Components.FormTitle("Endereço")))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.SelectColumn("UF", Data.ufList, vm.ufSelected, it -> it))
                                .r_child(Components.InputColumn("Cidade", vm.cidade, ""))
                                .r_child(Components.InputColumn("Bairro", vm.bairro, ""))
                                .r_child(Components.InputColumn("Rua", vm.rua, ""))
                                .r_child(Components.InputColumnNumeric("Número", vm.numero, ""))
                        )
                        .c_child(new SpacerVertical(20))
                        .c_child(new LineHorizontal())
                        .c_child(Components.TextAreaColumn("Observação", vm.observacao, ""))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm)));
    }

    private Row informacoesPessoais() {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.InputColumn("Nome Fantasia", vm.nome, "Ex: Empresa 123"))
                .r_child(Components.SelectColumn("Tipo de pessoa", Data.tiposPessoaList, vm.tipoPessoaSelected, it -> it))
                .r_child(Show.when(vm.tipoPessoaEhFisica,
                        () -> Components.InputColumnCpf("CPF", vm.cnpjCpf),
                        () -> Components.InputColumnCnpj("CNPJ", vm.cnpjCpf)
                ))
                .r_child(Components.InputColumnPhone("Celular", vm.celular))
                .r_child(Components.InputColumn("Inscrição estadual", vm.inscricaoEstadual, ""))
                .r_child(Components.InputColumn("Email", vm.email, "Ex: email@teste.com"));
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public Component table() {
        return new SimpleTable<FornecedorModel>()
                .fromData(vm.fornecedores)
                .header().columns()
                .column("ID", it -> it.getId())
                .column("Nome", it -> it.getNome())
                .column("Telefone", it -> Utils.formatPhone(it.getCelular()))
                .column("CPF/CNPJ",      it -> it.getCpfCnpj().length() == 11
                        ? Utils.formatCpf(it.getCpfCnpj())
                        : Utils.formatCnpj(it.getCpfCnpj()))
                .column("Email", it -> it.getEmail())
                .column("Data de Criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .end()
                .build()
                .onItemSelectChange(vm.fornecedorSelected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it -> {
                    Components.ShowModal(ItemDetails(it), this.ctx, 550);
                });
    }

    Component ItemDetails(FornecedorModel model) {
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do fornecedor", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Nome: ", model.getNome()))
                .c_child(Components.TextWithDetails("CNPJ: ", model.getCpfCnpj()))
                .c_child(Components.TextWithDetails("Telefone: ", model.getCelular()))
                .c_child(Components.TextWithDetails("Inscrição estadual: ", model.getInscricaoEstadual()))
                .c_child(Components.TextWithDetails("Email: ", model.getEmail()))
                .c_child(Components.TextWithDetails("UF: ", model.getUfSelected()))
                .c_child(Components.TextWithDetails("Cidade: ", model.getCidade()))
                .c_child(Components.TextWithDetails("Bairro: ", model.getBairro()))
                .c_child(Components.TextWithDetails("Rua: ", model.getRua()))
                .c_child(Components.TextWithDetails("Número: ", model.getNumero()))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true));
    }
}

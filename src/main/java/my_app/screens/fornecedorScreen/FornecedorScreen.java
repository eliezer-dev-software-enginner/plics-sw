package my_app.screens.fornecedorScreen;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.FornecedorDto;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.FornecedorRepository;
import my_app.domain.ContratoTelaCrud;
import my_app.domain.ContratoTelaCrudV2;
import my_app.screens.components.Components;
//import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.utils.related.TextVariant;
import my_app.utils.DateUtils;

import java.util.List;


import static my_app.utils.Utils.*;

public class FornecedorScreen implements ScreenComponent, ContratoTelaCrudV2 {
    private final FornecedorScreenViewModel vm;
    private final ScreenContext ctx;

    public FornecedorScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new FornecedorScreenViewModel(ctx);}

    public void onMount(){
        vm.loadFornecedores();
    }

    public Component render() {
        return mainView(vm.fornecedorSelected);
    }

    @Override
    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally())
                                .r_child(new Text("Cadastro de Fornecedor", (TextProps) new TextProps().variant(TextVariant.SUBTITLE).bold())))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome Fantasia", vm.nome,"Ex: Empresa 123"))
                                .r_child(Components.InputColumnNumeric("CNPJ", vm.cnpj, ""))
                                .r_child(Components.InputColumnPhone("Celular", vm.celular))
                                .r_child(Components.InputColumn("Inscrição estadual", vm.inscricaoEstadual, ""))
                        )
                        .c_child(Components.InputColumn("Email", vm.email, "Ex: email@teste.com"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Column().c_child(Components.FormTitle("Endereço")))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Cidade", vm.cidade,""))
                                .r_child(Components.InputColumn("Bairro", vm.bairro, ""))
                                .r_child(Components.InputColumn("Rua", vm.rua, ""))
                                .r_child(Components.InputColumnNumeric("Número", vm.numero, ""))
                        )
                        .c_child(Components.SelectColumn("UF", vm.ufList, vm.ufSelected, it->it))
                        .c_child(new SpacerVertical(20))
                        .c_child(new LineHorizontal())
                        .c_child(Components.TextAreaColumn("Observação", vm.observacao,""))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm)));
    }

    @Override
    public void handleClickNew() {this.vm.handleClickNew();}
    @Override
    public void handleClickMenuEdit() {vm.handleClickMenuEdit();}
    @Override
    public void handleClickMenuDelete() {vm.handleClickMenuDelete();}
    @Override
    public void handleClickMenuClone() {vm.handleClickMenuClone();}
    @Override
    public void clearForm(){vm.clearForm();}
    @Override
    public void handleAddOrUpdate() {vm.handleAddOrUpdate();}

    @Override
    public Component table() {
        return new SimpleTable<FornecedorModel>()
                .fromData(vm.fornecedores)
                .header().columns()
                    .column("ID", it-> it.id)
                    .column("Nome", it->it.nome)
                    .column("Telefone", it->it.celular)
                    .column("CNPJ", it->it.cpfCnpj)
                    .column("Email", it->it.email)
                    .column("Data de Criação", it->DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                    .end()
                .build()
                .onItemSelectChange(vm.fornecedorSelected::set)
                .onItemDoubleClick(it-> {
                    Components.ShowModal( ItemDetails(it), this.ctx, 550);
                });
    }

    Component ItemDetails(FornecedorModel model){
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes do fornecedor", new TextProps().variant(TextVariant.SUBTITLE)))
                .c_child(new SpacerVertical(20))
                .c_child(Components.TextWithDetails("ID: ", model.id))
                .c_child(Components.TextWithDetails("Nome: ", model.nome))
                .c_child(Components.TextWithDetails("CNPJ: ", model.cpfCnpj))
                .c_child(Components.TextWithDetails("Telefone: ", model.celular))
                .c_child(Components.TextWithDetails("Inscrição estadual: ", model.inscricaoEstadual))
                .c_child(Components.TextWithDetails("Email: ", model.email))
                .c_child(Components.TextWithDetails("UF: ", model.ufSelected))
                .c_child(Components.TextWithDetails("Cidade: ", model.cidade))
                .c_child(Components.TextWithDetails("Bairro: ", model.bairro))
                .c_child(Components.TextWithDetails("Rua: ", model.rua))
                .c_child(Components.TextWithDetails("Número: ", model.numero))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.millisToBrazilianDateTime(model.dataCriacao)))
                .c_child(Components.TextWithDetails("Observação: ", model.observacao,true));
    }
}
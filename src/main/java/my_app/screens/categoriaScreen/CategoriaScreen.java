package my_app.screens.categoriaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.CategoriaModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;

public class CategoriaScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final CategoriaScreenViewModel vm;

    public CategoriaScreen(ScreenContext ctx) {this.vm = new CategoriaScreenViewModel(ctx);}

    public void onMount() {vm.loadCategorias();}

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public Component form() {
        return new Card(new Column(new ColumnProps().spacingOf(20))
                .children(
                        Components.FormTitle("Cadastrar Nova Categoria"),
                        new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(
                                        Components.InputColumn("Nome", vm.nome, "Ex: Eletrônicos")),
                        Components.actionButtons(vm.btnText, this::handleAddOrUpdate, this::clearForm)
                )
        );
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<CategoriaModel>();
        simpleTable.fromData(vm.categorias)
                .header()
                .columns()
                .column("ID", it-> it.getId(), 70.0)
                .column("Nome", it-> it.getNome())
                .column("Data de criação", it-> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.categoriaSelecionada::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it-> {
                  //  Components.ShowModal( ItemDetails(it), ctx, 550);
                });

        return simpleTable;
    }


}
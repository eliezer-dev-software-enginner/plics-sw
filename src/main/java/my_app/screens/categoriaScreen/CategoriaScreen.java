package my_app.screens.categoriaScreen;

import disgust.io.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import my_app.core.ScreenContract;
import my_app.core.db.models.CategoriaModel;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import pack.utilities.DatePack;

public class CategoriaScreen implements ScreenComponent, ScreenContract<CategoriaModel> {
    private final CategoriaScreenViewModel vm;

    public CategoriaScreen(ScreenContext ctx) {this.vm = new CategoriaScreenViewModel(ctx);}

    public void onMount() {vm.fetchListData();}

    @Override
    public void onDestroy() {
        ScreenContract.super.onDestroy();
    }

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public Component form() {
        return new Card(new Column(new ColumnProps().spacingOf(20))
                .children(
                        Components.FormSubtitle("Cadastrar Nova Categoria"),
                        new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(
                                        Pack.InputColumn("Nome *", vm.nome, "Ex: Eletrônicos")),
                        Components.actionButtons(vm.btnText, this::handleAddOrUpdate)
                )
        );
    }

    @Override
    public Component itemDetails(CategoriaModel model) {
        return null;
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }

    @Override
    public SimpleTable table() {
        var simpleTable = new SimpleTable<CategoriaModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", CategoriaModel::getId, 70.0)
                .column("Nome", CategoriaModel::getNome)
                .column("Data de criação", it-> DatePack.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.categoriaSelecionada::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it-> {
                  //  Components.ShowModal( ItemDetails(it), ctx, 550);
                });

        return simpleTable;
    }


}
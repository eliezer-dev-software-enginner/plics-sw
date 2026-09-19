package my_app.screens.categoriaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.SimpleTable;
import megalodonte.router.v5.ScreenContext;
import my_app.core.ScreenContract;
import my_app.core.ViewModelScreenContract;
import my_app.core.db.models.CategoriaModel;
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
                .onItemSelectChange(vm.selected::set)
                .onChangeFocus(vm::handleFocusChange)
                .onItemDoubleClick(it-> {
                  //  Components.ShowModal( ItemDetails(it), ctx, 550);
                });

        return simpleTable;
    }
}
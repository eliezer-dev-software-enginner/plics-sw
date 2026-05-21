package my_app.screens.categoriaScreen;

import megalodonte.*;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.ProdutoModel;
import my_app.domain.ContratoTelaCrudV2;
import my_app.domain.ContratoTelaCrudV3;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;
import my_app.utils.DateUtils;

import java.sql.SQLException;

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
    public ViewModelv2 viewModel() {
        return vm;
    }

    @Override
    public Component table() {
        var simpleTable = new SimpleTable<CategoriaModel>();
        simpleTable.fromData(vm.categorias)
                .header()
                .columns()
                .column("ID", it-> it.id, 70.0)
                .column("Nome", it-> it.nome)
                .column("Data de criação", it-> DateUtils.millisToBrazilianDateTime(it.dataCriacao))
                .build()
                .onItemSelectChange(vm.categoriaSelecionada::set)
                .onItemDoubleClick(it-> {
                  //  Components.ShowModal( ItemDetails(it), ctx, 550);
                });

        return simpleTable;
    }


}
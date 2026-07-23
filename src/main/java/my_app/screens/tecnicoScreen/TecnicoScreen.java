package my_app.screens.tecnicoScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.CardProps;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.TecnicoModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import my_app.utils.DateUtils;

public class TecnicoScreen implements ScreenComponent, ContratoTelaCrudV3 {
    private final TecnicoScreenViewModel vm;

    public TecnicoScreen(ScreenContext ctx) {
        this.vm = new TecnicoScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.fetchListData();
    }

    @Override
    public void onDestroy() {
        ContratoTelaCrudV3.super.onDestroy();
    }

    public Component render() {
        return mainView(vm.focusState);
    }

    @Override
    public Component form() {
        return new Card(
                new Column(new ColumnProps().paddingAll(5))
                        .c_child(Components.FormTitle("Cadastrar Novo Técnico"))
                        .c_child(new SpacerVertical(20))
                        .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(Components.InputColumn("Nome", vm.nome, "Ex: Matias")))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.actionButtons(vm.btnText, this::handleAddOrUpdate)),
                new CardProps()
                        .padding(10)
                        .borderRadius(12)
        );
    }

    @Override
    public SimpleTable table() {
        var simpleTable = new SimpleTable<TecnicoModel>();
        simpleTable.fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", TecnicoModel::getId, 90.0)
                .column("Nome", TecnicoModel::getNome)
                .column("Data criação", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onItemSelectChange(vm.tecnicoSelected::set)
                .onChangeFocus(vm::handleFocusChange);

        return simpleTable;
    }

    @Override
    public ViewModelScreenContract viewModel() {
        return vm;
    }
}

package my_app.core;

import megalodonte.base.components.Component;
import megalodonte.base.state.State;
import megalodonte.ComputedState;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import my_app.core.components.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ScreenContract<T extends Identifier> {

    Logger log = LoggerFactory.getLogger(ScreenContract.class);

    ViewModelScreenContract<T> viewModel();

    default void handleClickNew() {
        viewModel().ctx.spawnWindow(viewModel().screenNameSpawn+"/-1/add/");
    }

    default void handleClickMenuDelete() {
        if(viewModel().selectedItemsOrCurrent().isEmpty())throw new IllegalArgumentException("Selecione ao menos um item na tabela antes!");
        viewModel().handleClickMenuDelete();
    }

    default void handleClickMenuClone() {
        if(viewModel().selectedItemsOrCurrent().size() != 1)throw new IllegalArgumentException("Selecione exatamente um item na tabela antes!");

        long id = viewModel().selectedItemsOrCurrent().getFirst().getId();
        viewModel().ctx.spawnWindow(viewModel().screenNameSpawn+"/"+id+"/clone/");
    }

    default void handleClickMenuEdit() {
        if(viewModel().selectedItemsOrCurrent().size() != 1)throw new IllegalArgumentException("Selecione exatamente um item na tabela antes!");

        long id = viewModel().selectedItemsOrCurrent().getFirst().getId();
        viewModel().ctx.spawnWindow(viewModel().screenNameSpawn+"/"+id+"/edit/");
    }

    default Component commonCustomMenus(State<Boolean> focusState) {
        var actionsVisible = ComputedState.of(
                () -> focusState.get() || !viewModel().selectedItems.get().isEmpty(),
                focusState,
                viewModel().selectedItems
        );
        return Components.commonCustomMenusv3(
                actionsVisible,
                this::handleClickNew,
                this::handleClickMenuEdit,
                this::handleClickMenuDelete,
                this::handleClickMenuClone
        );
    }

    SimpleTable<T> table();

    @Deprecated(forRemoval = true)
    default Component form(){
        return new Container();
    }
    Component itemDetails(T model);

    default Component mainView(State<Boolean> focusState) {
        var tableInstance = table().horizontalScroll().paginate(25);
        tableInstance.enableMultipleSelection(items -> {
            viewModel().selectedItems.set(items);
            viewModel().selected.set(items.isEmpty() ? null : items.getLast());
        });

        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(focusState),
                        new SpacerVertical(10),
                        new Container(new ContainerProps().bgColor("#fff").fillHeight())
                                .children(
                                        new Container(new ContainerProps().paddingLeft(20)
                                                .paddingRight(20).fillHeight().spacingOf(15))
                                                .children(Components.searchInput(viewModel().searchState, "Pesquisar"),
                                                        tableInstance,
                                                        new Row(new RowProps().fillWidth().rightHorizontally()).children(
                                                                tableInstance.paginationControls()
                                                        )

                                                )
                                )
                );
    }

    default void clearForm() {
        viewModel().clearForm();
    }

    default void onDestroy() {
        try {
            viewModel().onDestroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

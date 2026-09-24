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
import my_app.services.exports.TableExportActions;
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

    default Component commonCustomMenus(State<Boolean> focusState, SimpleTable<T> table) {
        var hasSelection = ComputedState.of(
                () -> !viewModel().selectedItems.get().isEmpty()
                        || (focusState.get() && viewModel().selected.get() != null),
                viewModel().selectedItems,
                focusState,
                viewModel().selected
        );
        var hasSingleSelection = ComputedState.of(
                () -> viewModel().selectedItems.get().size() == 1
                        || (viewModel().selectedItems.get().isEmpty()
                        && focusState.get() && viewModel().selected.get() != null),
                viewModel().selectedItems,
                focusState,
                viewModel().selected
        );
        var hasMultipleSelection = ComputedState.of(
                () -> viewModel().selectedItems.get().size() > 1,
                viewModel().selectedItems
        );
        var canShowRegularActions = ComputedState.of(
                () -> !hasMultipleSelection.get(),
                hasMultipleSelection
        );
        return Components.commonCustomMenusv3(
                hasSelection,
                hasSingleSelection,
                canShowRegularActions,
                this::handleClickNew,
                this::handleClickMenuEdit,
                this::handleClickMenuDelete,
                this::handleClickMenuClone,
                () -> TableExportActions.csv(viewModel().ctx, exportTitle(), table),
                () -> TableExportActions.pdf(viewModel().ctx, exportTitle(), table)
        );
    }

    default String exportTitle() {
        var stage = viewModel().ctx.selfStage();
        return stage != null && stage.getTitle() != null && !stage.getTitle().isBlank()
                ? stage.getTitle() : "Tabela";
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
            viewModel().selected.set(items.isEmpty() ? tableInstance.getSelectedItem() : items.getLast());
        });

        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(focusState, tableInstance),
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

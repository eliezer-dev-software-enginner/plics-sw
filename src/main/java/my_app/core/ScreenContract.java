package my_app.core;

import megalodonte.base.components.Component;
import megalodonte.base.state.State;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
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
        if(viewModel().selected.get() == null)throw new IllegalArgumentException("Selecione o item na tabela antes!");
        viewModel().handleClickMenuDelete();
    }

    default void handleClickMenuClone() {
        if(viewModel().selected.get() == null)throw new IllegalArgumentException("Selecione o item na tabela antes!");

        long id = viewModel().selected.get().getId();
        viewModel().ctx.spawnWindow(viewModel().screenNameSpawn+"/"+id+"/clone/");
    }

    default void handleClickMenuEdit() {
        if(viewModel().selected.get() == null)throw new IllegalArgumentException("Selecione o item na tabela antes!");

        long id = viewModel().selected.get().getId();
        viewModel().ctx.spawnWindow(viewModel().screenNameSpawn+"/"+id+"/edit/");
    }

    default Component commonCustomMenus(State<Boolean> focusState) {
        return Components.commonCustomMenusv3(
                focusState,
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
        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(focusState),
                        new SpacerVertical(10),
                        new Container(new ContainerProps().bgColor("#fff").fillHeight())
                                .children(
                                        new Container(new ContainerProps().paddingLeft(20)
                                                .paddingRight(20).fillHeight().spacingOf(15))
                                                .children(Components.searchInput(viewModel().searchState, "Pesquisar"),
                                                        table())
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

package my_app.domain;

import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import my_app.screens.components.Components;

public interface ContratoTelaCrudV3 {

    State<Boolean> modoEdicao();

    default void handleClickNew(){
        modoEdicao().set(false);
        clearForm();
    }

    void handleClickMenuDelete();

    default void handleClickMenuClone(){
        populateFromModel();
        modoEdicao().set(false);
    }

    default void handleClickMenuEdit(){
        populateFromModel();
        modoEdicao().set(true);
    }

     default <T> Component commonCustomMenus(State<Boolean> focusState){
        return Components.commonCustomMenusv3(
                focusState,
                this::handleClickNew,
                this::handleClickMenuEdit,
                this::handleClickMenuDelete,
                this::handleClickMenuClone
        );
    }

    void handleAddOrUpdate();
    void clearForm();
    Component table();
    Component form();

    default <T> Component mainView(State<Boolean> focusState) {
        var mainContent = new Container(new ContainerProps().bgColor("#fff"))
                .children(
                        form(), new SpacerVertical(30), table()
        );

        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(focusState),
                        new SpacerVertical(10),
                        Components.ScrollPaneDefault(mainContent)
                );
    }

   void populateFromModel();

}

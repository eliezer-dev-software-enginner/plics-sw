package my_app.domain;

import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.state.State;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import my_app.domain.components.Components;

public interface ContratoTelaCrudV3 {

    ViewModelScreenContract viewModel();

    default void handleClickNew(){
       viewModel().modoEdicaoState().set(false);
       clearForm();
    }

    default void handleClickMenuDelete(){
        viewModel().modoEdicaoState().set(false);
        viewModel().handleClickMenuDelete();
    }

    default void handleClickMenuClone(){
        populateFromModel();
        viewModel().modoEdicaoState().set(false);
    }

    default void handleClickMenuEdit(){
        populateFromModel();
        viewModel().modoEdicaoState().set(true);
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

   default void populateFromModel(){
       viewModel().populateFromModel();
   }

    default void clearForm() {
        viewModel().clearForm();
    }

    default void handleAddOrUpdate() {
        try{
            viewModel().handleAddOrUpdate();
        }catch(Exception e){
            e.printStackTrace();
            UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
        }

    }

}

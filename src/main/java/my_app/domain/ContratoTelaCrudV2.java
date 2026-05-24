package my_app.domain;

import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import my_app.domain.components.Components;


@Deprecated
public interface ContratoTelaCrudV2 {
    void handleClickNew();
    void handleClickMenuEdit();
    void handleClickMenuDelete();
    void handleClickMenuClone();

     default <T> Component commonCustomMenus(State<T> itemSelectedInTable){
        return Components.commonCustomMenus(
                itemSelectedInTable,
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

    default <T> Component mainView(State<T> itemSelectedInTable) {
        var mainContent = new Container(new ContainerProps().bgColor("#fff"))
                .children(
                        form(), new SpacerVertical(30), table()
        );

        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(itemSelectedInTable),
                        new SpacerVertical(10),
                        Components.ScrollPaneDefault(mainContent)
                );
    }


}

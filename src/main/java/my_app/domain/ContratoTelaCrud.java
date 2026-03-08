package my_app.domain;

import megalodonte.components.layout_components.Column;
import megalodonte.components.Component;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import my_app.screens.components.Components;

public interface ContratoTelaCrud {
    void handleClickNew();
    void handleClickMenuEdit();
    void handleClickMenuDelete();
    void handleClickMenuClone();

    default Row commonCustomMenus(){
        return Components.commonCustomMenus(this::handleClickNew,
                this::handleClickMenuEdit,
                this::handleClickMenuDelete,
                this::handleClickMenuClone
        );
    }

    void handleAddOrUpdate();
    void clearForm();
    Component table();
    Component form();

    default Component mainView() {
        var mainContent = new Column()
                .c_child(form())
                .c_child(new SpacerVertical(30))
                .c_child(table());

        return new Column(new ColumnProps().paddingAll(10))
                .c_child(commonCustomMenus())
                .c_child(new SpacerVertical(10))
                .c_child(Components.ScrollPaneDefault(mainContent));
    }
}

package my_app.domain;

import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import megalodonte.base.Animations;
import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.state.State;
import megalodonte.components.Button;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.v2.Show;
import my_app.domain.components.Components;
import org.kordamp.ikonli.entypo.Entypo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ContratoTelaCrudV3 {

    Logger log = LoggerFactory.getLogger(ContratoTelaCrudV3.class);

    ViewModelScreenContract viewModel();

    default void handleClickNew() {
        viewModel().modoEdicaoState().set(false);
        clearForm();
    }

    default void handleClickMenuDelete() {
        viewModel().modoEdicaoState().set(false);
        viewModel().handleClickMenuDelete();
    }

    default void handleClickMenuClone() {
        populateFromModel();
        viewModel().modoEdicaoState().set(false);
    }

    default void handleClickMenuEdit() {
        populateFromModel();
        viewModel().modoEdicaoState().set(true);
    }

    default <T> Component commonCustomMenus(State<Boolean> focusState) {
        return Components.commonCustomMenusv3(
                focusState,
                this::handleClickNew,
                this::handleClickMenuEdit,
                this::handleClickMenuDelete,
                this::handleClickMenuClone
        );
    }

    SimpleTable table();

    Component form();

    default <T> Component mainView(State<Boolean> focusState) {
        var mainContent = new Container(new ContainerProps().bgColor("#fff"))
                .children(
                        new Row(new RowProps().fillWidth().centerHorizontally()).children(
                                new Button(viewModel().formIsVisibleTextComputed)
                                        .onClick(() -> viewModel().handleToggleFormVisible())
                                        .icon(viewModel().createToggleIcon())
                        ),
                        new SpacerVertical(20),
                        Show.when(viewModel().formIsVisible, () -> new Row(new RowProps().
                                fillWidth().centerHorizontally())
                                .children(form())
                        ).withTransition((c, entering) -> {
                            if (entering) {
                                var anim = Animations.pop(c, true, Duration.millis(100));
                                anim.setOnFinished(e -> {
                                    var n = c.getNode().getParent();
                                    while (n != null) {
                                        if (n instanceof ScrollPane sp) {
                                            sp.setVvalue(0);
                                            break;
                                        }
                                        n = n.getParent();
                                    }
                                });
                                return anim;
                            } else {
                                return Animations.fadeScale(c, false, Duration.millis(250));
                            }
                        }),
                        new SpacerVertical(30),
                        new Container(new ContainerProps().paddingLeft(20).paddingRight(20).fillHeight())
                                .children(
                                        Components.searchInput(viewModel().searchState, "Pesquisar"),
                                        table()
                                )

                );

        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(focusState),
                        new SpacerVertical(10),
                        Components.ScrollPaneDefault(mainContent)
                );
    }

    default void populateFromModel() {
        viewModel().populateFromModel();
    }

    default void clearForm() {
        viewModel().clearForm();
    }

    default void handleAddOrUpdate() {
        try {
            viewModel().handleAddOrUpdate();
            viewModel().modoEdicaoState().set(false);
        } catch (Exception e) {
            log.error("Erro em handleAddOrUpdate", e);
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
        }

    }

    default void onDestroy() {
        try {
            viewModel().onDestroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package my_app.domain;

import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import megalodonte.base.Animations;
import megalodonte.base.UI;
import megalodonte.base.components.Component;
import megalodonte.base.state.State;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Button;
import megalodonte.components.SimpleTable;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.v2.Show;
import my_app.db.models.ProdutoModel;
import my_app.domain.components.Components;
import my_app.utils.DateUtils;
import org.kordamp.ikonli.entypo.Entypo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ContratoTelaCrudV3<T> {

    Logger log = LoggerFactory.getLogger(ContratoTelaCrudV3.class);

    ViewModelScreenContract<T> viewModel();

    default void handleClickNew() {
        viewModel().formIsVisible.set(true);
        viewModel().modoEdicaoState().set(false);
        clearForm();
    }

    default void handleClickMenuDelete() {
        viewModel().modoEdicaoState().set(false);
        viewModel().handleClickMenuDelete();
    }

    default void handleClickMenuClone() {
        viewModel().formIsVisible.set(true);
        populateFieldsFromModel();
        viewModel().modoEdicaoState().set(false);
    }

    default void handleClickMenuEdit() {
        viewModel().formIsVisible.set(true);
        populateFieldsFromModel();
        viewModel().modoEdicaoState().set(true);
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
    Component form();
    Component itemDetails(T model);

    default Component mainView(State<Boolean> focusState) {
        return new Container(new ContainerProps().paddingAll(10).bgColor("#fff"))
                .children(
                        commonCustomMenus(focusState),
                        new SpacerVertical(10),
                        Components.ScrollPaneDefault(
                                new Container(new ContainerProps().bgColor("#fff").fillHeight())
                                        .children(
                                                buttonExpandMinimizeWrapper(),
                                                new SpacerVertical(ThemeManager.theme().spacing().md()),
                                                Show.when(viewModel().formIsVisible, () -> form()
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

                                        )
                        )
                );
    }

    private Row buttonExpandMinimizeWrapper() {
        return new Row(new RowProps().fillWidth().centerHorizontally()).children(
                new Button(viewModel().formIsVisibleTextComputed)
                        .onClick(() -> viewModel().handleToggleFormVisible())
                        .icon(viewModel().createToggleIcon())
        );
    }

    default void populateFieldsFromModel() {
        viewModel().populateFieldsFromModel();
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

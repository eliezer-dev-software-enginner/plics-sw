package my_app.screens.categoriaScreen;

import megalodonte.base.components.Component;
import megalodonte.components.Card;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.CategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AddOrEditCategorias extends ScreenAddOrEdit<CategoriaModel, CategoriaScreenViewModel> {

    public AddOrEditCategorias(ScreenContextInterface screenContext) {
        super(screenContext);
    }

    @Override
    public Component render() {
        return new Card(new Column(new ColumnProps().spacingOf(20))
                .children(
                        Components.FormSubtitle("Dados da categoria"),
                        new Row(new RowProps().bottomVertically().spacingOf(10))
                                .r_child(
                                        disgust.io.Pack.InputColumn("Nome *", viewModel.nome, "Ex: Eletrônicos")),
                        Components.actionButton(getBtnActionText(), this::handleAddOrUpdate)
                )
        );
    }

    @Override
    protected CategoriaScreenViewModel getViewModel(ScreenContextInterface screenContext) {
        return new CategoriaScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<CategoriaModel> getService() throws SQLException {
        return new CategoriaService();
    }

    @Override
    protected String getTitle() {
        return "categoria";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(AddOrEditCategorias.class);
    }
}

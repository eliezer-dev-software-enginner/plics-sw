package my_app.screens.contasAReceberScreen;

import megalodonte.base.components.Component;
import megalodonte.components.Card;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.v5.ScreenContext;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.CategoriaService;
import my_app.screens.categoriaScreen.CategoriaScreenViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ScrenAddOrEditContasAReceber extends ScreenAddOrEdit<CategoriaModel, CategoriaScreenViewModel> {

    public ScrenAddOrEditContasAReceber(ScreenContext screenContext) {
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
                        Components.actionButtons(viewModel.btnText, this::handleAddOrUpdate)
                )
        );
    }

    @Override
    protected CategoriaScreenViewModel getViewModel(ScreenContext screenContext) {
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
        return LoggerFactory.getLogger(ScrenAddOrEditContasAReceber.class);
    }
}

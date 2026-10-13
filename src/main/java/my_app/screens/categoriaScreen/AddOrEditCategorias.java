package my_app.screens.categoriaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.CategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/** Category form is declared in JSONC; the existing view model owns persistence. */
public final class AddOrEditCategorias extends ScreenAddOrEdit<CategoriaModel, CategoriaScreenViewModel> {
    private static final UiDefinition VIEW = UiDefinition.fromResource(
            AddOrEditCategorias.class, "/views/categoria-form.view.jsonc");
    private UiSession session;
    private Subscription nameUpdates;
    private Subscription buttonUpdates;

    public AddOrEditCategorias(ScreenContextInterface context) {
        super(context);
        session = VIEW.open(new Actions().register("salvarCategoria", screen -> {
            var errors = screen.validate();
            if (!errors.isEmpty()) {
                Components.ShowAlertError(errors.values().iterator().next());
                return;
            }
            viewModel.nome.set(screen.states().string("nome").get());
            handleAddOrUpdate();
        }));
        nameUpdates = viewModel.nome.observe(value -> session.states().string("nome").set(value));
        buttonUpdates = viewModel.btnText.observe(value -> session.states().string("acao").set(value));
    }

    @Override public Component render() { return session.render(); }

    @Override public void onDestroy() {
        if (nameUpdates != null) nameUpdates.close();
        if (buttonUpdates != null) buttonUpdates.close();
        if (session != null) session.close();
        super.onDestroy();
    }

    @Override protected CategoriaScreenViewModel getViewModel(ScreenContextInterface context) {
        return new CategoriaScreenViewModel(context);
    }

    @Override protected BaseService<CategoriaModel> getService() throws SQLException {
        return new CategoriaService();
    }

    @Override protected String getTitle() { return "categoria"; }

    @Override protected Logger getLogger() { return LoggerFactory.getLogger(AddOrEditCategorias.class); }
}

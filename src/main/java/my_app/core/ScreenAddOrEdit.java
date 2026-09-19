package my_app.core;

import megalodonte.application.ErrorReporter;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.router.v5.ScreenContext;
import megalodonte.utils.ThrowingSupplier;
import my_app.core.components.Components;
import my_app.core.db.services.BaseService;
import org.slf4j.Logger;

import java.sql.SQLException;


public abstract class ScreenAddOrEdit<Model extends Identifier, VM extends ViewModelScreenContract<Model>> implements ScreenComponent {
    protected VM viewModel;

    private final BaseService<Model> service;
    protected final ScreenContext screenContext;

    protected Long id;

    State<String> titleState = new State<>("");
    protected ThemeInterface theme = ThemeManager.theme();

    protected Logger log;

    protected abstract VM getViewModel(ScreenContext screenContext);
    protected abstract BaseService<Model> getService() throws SQLException;
    protected abstract String getTitle();
    protected abstract Logger getLogger();


    public ScreenAddOrEdit(ScreenContext screenContext) {
        this.screenContext = screenContext;

        log = getLogger();

        viewModel =  getViewModel(screenContext);
        service = createOrReport(this::getService);
        String type = screenContext.getParams().get("type");
        String idParam = screenContext.getParams().get("id");

        try {
            id = Long.parseLong(idParam);
        } catch (RuntimeException e) {
            log.error("Parâmetro 'id' inválido na rota de edição/inclusão: {}", idParam, e);
            try {
                onDestroy();
            } catch (Exception cleanup) {
                log.warn("Erro ao limpar recursos após rota inválida", cleanup);
            }
            UI.runOnUi(() -> Components.ShowAlertError("ID inválido na rota de edição/inclusão."));
            return;
        }

        Async.Run(()->{
            var model = service.buscarById(id);
            UI.runOnUi(()-> {
                viewModel.selected.set(model);
                titleState.set("Incluir " + getTitle());
                screenContext.selfStage().setTitle("Inclusão de " +getTitle());

                if(type.equals("edit")){
                    viewModel.modoEdicaoState().set(true);
                    viewModel.populateFieldsFromModel();
                    titleState.set("Editar produto com Id: " + id);
                    screenContext.selfStage().setTitle("Edição de " + getTitle());
                }
            });
        });
    }

    protected <T> T createOrReport(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            ErrorReporter.handle(e);
            throw new IllegalStateException(e); // interrompe a construção da tela de forma previsível
        }
    }

    @Override
    public void onDestroy() {
        try {
            viewModel.onDestroy();
            service.close();
        } catch (Exception e) {
            log.warn("Erro ao destruir {}", "ScreenAddOrEdit_"+getTitle(), e);
        }
    }

    protected void handleAddOrUpdate() {
        try {
            viewModel.handleAddOrUpdate();
            viewModel.modoEdicaoState().set(false);
        } catch (Exception e) {
            log.error("Erro em handleAddOrUpdate", e);
            UI.runOnUi(() -> Components.ShowAlertError("Não foi possível salvar. Tente novamente."));
        }
    }

}

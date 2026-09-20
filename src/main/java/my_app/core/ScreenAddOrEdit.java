package my_app.core;

import megalodonte.application.ErrorReporter;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.utils.ThrowingSupplier;
import my_app.core.components.Components;
import my_app.core.db.services.BaseService;
import org.slf4j.Logger;

import java.sql.SQLException;


public abstract class ScreenAddOrEdit<Model extends Identifier, VM extends ViewModelScreenContract<Model>> implements ScreenComponent {
    protected VM viewModel;

    private final BaseService<Model> service;
    protected final ScreenContextInterface screenContext;

    protected Long id;
    protected String type;

    State<String> titleState = new State<>("");
    protected ThemeInterface theme = ThemeManager.theme();

    protected Logger log;

    protected abstract VM getViewModel(ScreenContextInterface screenContext);
    protected abstract BaseService<Model> getService() throws SQLException;
    protected abstract String getTitle();
    protected abstract Logger getLogger();


    public ScreenAddOrEdit(ScreenContextInterface screenContext) {
        this.screenContext = screenContext;

        log = getLogger();

        viewModel =  getViewModel(screenContext);
        service = createOrReport(this::getService);
        this.type = screenContext.getParams().get("type");
        String idParam = screenContext.getParams().get("id");

        if(isEdit()){
            viewModel.isEditing();
        }

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
            var model = !id.equals(-1L)? service.buscarById(id): null;
            UI.runOnUi(()-> {
                viewModel.selected.set(model);
                titleState.set("Incluir " + getTitle());
                screenContext.selfStage().setTitle("Inclusão de " +getTitle());

                if(type.equals("edit")){
                    viewModel.populateFieldsFromModel();
                    titleState.set("Editar produto com Id: " + id);
                    screenContext.selfStage().setTitle("Edição de " + getTitle());
                }

                if(type.equals("clone")){
                    viewModel.populateFieldsFromModel();
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
            viewModel.finishEditing();
        } catch (Exception e) {
            log.error("Erro em handleAddOrUpdate", e);
            UI.runOnUi(() -> Components.ShowAlertError("Não foi possível salvar. Tente novamente."));
        }
    }

    protected String getBtnActionText(){
        if(type.equals("edit"))return "Atualizar";
        return "Cadastrar";
    }

    protected boolean isEdit(){
        return type.equals("edit");
    }

}

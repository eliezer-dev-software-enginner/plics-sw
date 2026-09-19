package my_app.screens.categoriaScreen;

import megalodonte.application.ErrorReporter;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.components.Card;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.ThrowingSupplier;
import my_app.core.components.Components;
import my_app.core.db.services.CategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOrEditCategorias implements ScreenComponent {
    Long id;
    CategoriaScreenViewModel viewModel;
    Logger log = LoggerFactory.getLogger(AddOrEditCategorias.class);
    CategoriaService service;

    State<String> titleState = new State<>("");

    public AddOrEditCategorias(ScreenContext screenContext) {
        viewModel = new CategoriaScreenViewModel(screenContext);
        service = createOrReport(CategoriaService::new);
        String type = screenContext.getParams().get("type");
        String idParam = screenContext.getParams().get("id");

        try {
            id = Long.parseLong(idParam);
        } catch (RuntimeException e) {
            log.error("Parâmetro 'id' inválido na rota de edição/inclusão: {}", idParam, e);
            try {
                viewModel.onDestroy();
                service.close();
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
                titleState.set("Incluir categoria");
                screenContext.selfStage().setTitle("Inclusão de categoria");

                if(type.equals("edit")){
                    viewModel.modoEdicaoState().set(true);
                    viewModel.populateFieldsFromModel();
                    titleState.set("Editar categoria com Id: " + id);
                    screenContext.selfStage().setTitle("Edição de categoria");
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
    public void onMount() {
        ScreenComponent.super.onMount();
    }

    @Override
    public void onDestroy() {
        try {
            viewModel.onDestroy();
            service.close();
        } catch (Exception e) {
            log.warn("Erro ao destruir AddOrEditCategoriasScreen", e);
        }
    }

    void handleAddOrUpdate() {
        try {
            viewModel.handleAddOrUpdate();
            viewModel.modoEdicaoState().set(false);
        } catch (Exception e) {
            log.error("Erro em handleAddOrUpdate", e);
            UI.runOnUi(() -> Components.ShowAlertError("Não foi possível salvar. Tente novamente."));
        }
    }
}

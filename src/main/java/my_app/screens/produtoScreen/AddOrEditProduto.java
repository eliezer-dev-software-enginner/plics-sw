package my_app.screens.produtoScreen;

import megalodonte.application.ErrorReporter;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.state.State;
import megalodonte.components.layout_components.Container;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.ThrowingSupplier;
import my_app.core.components.Components;
import my_app.core.db.services.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOrEditProduto implements ScreenComponent {
    Long id;
    ProdutoScreenViewModel viewModel;
    Logger log = LoggerFactory.getLogger(AddOrEditProduto.class);
    ProdutoService produtoService;

    State<String> titleState = new State<>("");

    public AddOrEditProduto(ScreenContext screenContext) {
        viewModel = new ProdutoScreenViewModel(screenContext);
        produtoService = createOrReport(ProdutoService::new);
        String type = screenContext.getParams().get("type");

        String idParam = screenContext.getParams().get("id");

        try {
            id = Long.parseLong(idParam);
        } catch (RuntimeException e) {
            log.error("Parâmetro 'id' inválido na rota de edição/inclusão: {}", idParam, e);
            try {
                viewModel.onDestroy();
                produtoService.close();
            } catch (Exception cleanup) {
                log.warn("Erro ao limpar recursos após rota inválida", cleanup);
            }
            UI.runOnUi(() -> Components.ShowAlertError("ID inválido na rota de edição/inclusão."));
            return;
        }

        Async.Run(()->{
            var model = produtoService.buscarById(id);
            UI.runOnUi(()-> {
                viewModel.selected.set(model);
                titleState.set("Incluir produto");
                screenContext.selfStage().setTitle("Inclusão de produto");

                if(type.equals("edit")){
                    viewModel.modoEdicaoState().set(true);
                    viewModel.populateFieldsFromModel();
                    titleState.set("Editar produto com Id: " + id);
                    screenContext.selfStage().setTitle("Edição de produto");
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
        return new Container();
    }

    @Override
    public void onMount() {
        ScreenComponent.super.onMount();
    }

    @Override
    public void onDestroy() {
        try {
            viewModel.onDestroy();
            produtoService.close();
        } catch (Exception e) {
            log.warn("Erro ao destruir AddOrEditProdutoScreen", e);
        }
    }
}

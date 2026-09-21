package my_app.screens.categoriaScreen;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import my_app.core.AppRoutes;
import my_app.core.ViewModelScreenContract;
import my_app.core.components.Components;
import my_app.core.db.models.CategoriaModel;
import my_app.core.db.services.CategoriaService;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.Consumer;

public class CategoriaScreenViewModel extends ViewModelScreenContract<CategoriaModel> {
    private static final Logger log = LoggerFactory.getLogger(CategoriaScreenViewModel.class);

    private final CategoriaService categoriaService;

    final State<String> nome = new State<>("");
    private final Consumer<Object> eventListener = this::onEntityEvent;

    public CategoriaScreenViewModel(ScreenContextInterface ctx) {
        super(ctx);
        screenNameSpawn = AppRoutes.Screens.ADD_OR_EDIT_CATEGORIAS.name();
        this.categoriaService = createOrReport(CategoriaService::new);
        EventBus.getInstance().subscribe(eventListener);
    }

    private void onEntityEvent(Object event) {
        if (event instanceof EntityEvent<?> ee && ee.entity() instanceof CategoriaModel) {
            fetchListData();
        }
    }

    @Override
    protected boolean matchesSearch(CategoriaModel model, String query) {
        return contains(model.getNome(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var list = categoriaService.listar();
                UI.runOnUi(() -> allDataList.set(list));
            } catch (Exception e) {
                log.error("Erro ao buscar categorias", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar categorias"));
            }
        });
    }

    @Override
    public void populateFieldsFromModel(CategoriaModel model) {
        nome.set(model.getNome());
    }

    @Override
    public CategoriaModel populateModelFromFields() {
        var model = isEditing && selected.get() != null
                ? selected.get()
                : new CategoriaModel();
        model.setNome(nome.get().trim());
        return model;
    }

    @Override
    public void handleClickMenuDelete() {
        var model = selected.get();
        if (model == null) return;

        Components.ShowAlertAdvice("Deseja excluir categoria " + model.getNome(), () ->
                Async.Run(() -> {
                    try {
                        categoriaService.excluirById(model.getId());
                        UI.runOnUi(() -> {
                            allDataList.removeIf(it -> it.getId().equals(model.getId()));
                            Components.ShowPopup(ctx, "Categoria excluída com sucesso");
                            EventBus.getInstance().publish(EntityEvent.excluido(model.getId()));
                        });
                    } catch (Exception e) {
                        log.error("Erro ao excluir categoria id={}", model.getId(), e);
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                    }
                })
        );
    }

    @Override
    public void handleAddOrUpdate() {
        var model = populateModelFromFields();
        Async.Run(() -> {
            try {
                if (isEditing) {
                    if (model == null) return;
                    categoriaService.atualizar(model);
                    CategoriaModel atualizada = new CategoriaModel();
                    atualizada.setId(model.getId());
                    atualizada.setNome(model.getNome());
                    atualizada.setDataCriacao(model.getDataCriacao());
                    UI.runOnUi(() -> {
                        allDataList.updateIf(it -> it.getId().equals(atualizada.getId()), it -> atualizada);
                        Components.ShowPopup(ctx, "Categoria atualizada com sucesso");
                        clearForm();
                        EventBus.getInstance().publish(EntityEvent.editado(atualizada));
                    });
                } else {
                    var salvo = categoriaService.salvar(model);
                    UI.runOnUi(() -> {
                        allDataList.add(salvo);
                        Components.ShowPopup(ctx, "Categoria cadastrada com sucesso");
                        clearForm();
                        EventBus.getInstance().publish(EntityEvent.criado(salvo));
                    });
                }
            } catch (IllegalArgumentException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            } catch (Exception e) {
                log.error("Erro inesperado ao salvar categoria", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro inesperado: " + e.getMessage()));
            }
        });
    }

    @Override
    public void clearForm() {
        nome.set("");
    }

    @Override
    public void onDestroy() throws Exception {
        this.categoriaService.close();
        EventBus.getInstance().unsubscribe(eventListener);
    }

    @Override
    public CategoriaModel findById(Long id) throws SQLException {
        return categoriaService.buscarById(id);
    }
}

package my_app.screens.categoriaScreen;

import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.CategoriaModel;
import my_app.db.services.CategoriaService;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.domain.components.Components;

import java.sql.SQLException;
import java.util.List;

public class CategoriaScreenViewModel extends ViewModelScreenContract {

    private final CategoriaService categoriaService;

    final ListState<CategoriaModel> categorias = ListState.of(List.of());
    final State<CategoriaModel> categoriaSelecionada = State.of(null);
    final State<String> nome = new State<>("");

    public CategoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            categoriaService = new CategoriaService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
        this.onInit();
    }

    @Override
    public void populateFromModel() {
        var data = categoriaSelecionada.get();
        if (data != null) nome.set(data.getNome());
    }

    void loadCategorias() {
        Async.Run(() -> {
            try {
                var list = categoriaService.listar();
                UI.runOnUi(() -> {
                    categorias.clear();
                    categorias.addAll(list);
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar categorias"));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        var model = categoriaSelecionada.get();
        if (model == null) return;

        Components.ShowAlertAdvice("Deseja excluir categoria " + model.getNome(), () ->
                Async.Run(() -> {
                    try {
                        categoriaService.excluirById(model.getId());
                        UI.runOnUi(() -> {
                            categorias.removeIf(it -> it.getId().equals(model.getId()));
                            Components.ShowPopup(ctx, "Categoria excluída com sucesso");
                        });
                    } catch (Exception e) {
                        UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                    }
                })
        );
    }

    @Override
    public void handleAddOrUpdate() {
        Async.Run(() -> {
            try {
                if (modoEdicao.get()) {
                    var model = categoriaSelecionada.get();
                    if (model == null) return;
                    model.setNome(nome.get().trim());
                    categoriaService.atualizar(model);
                    UI.runOnUi(() -> {
                        categorias.updateIf(it -> it.getId().equals(model.getId()), it -> model);
                        Components.ShowPopup(ctx, "Categoria atualizada com sucesso");
                        clearForm();
                    });
                } else {
                    var model = new CategoriaModel();
                    model.setNome(nome.get().trim());
                    categoriaService.salvar(model);
                    UI.runOnUi(() -> {
                        categorias.add(model);
                        Components.ShowPopup(ctx, "Categoria cadastrada com sucesso");
                        clearForm();
                    });
                }
            } catch (IllegalArgumentException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro inesperado: " + e.getMessage()));
            }
        });
    }

    @Override
    public void clearForm() {
        nome.set("");
    }
}

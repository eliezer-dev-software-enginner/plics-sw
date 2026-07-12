package my_app.screens.categoriaScreen;

import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.CategoriaModel;
import my_app.db.services.CategoriaService;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;

import java.sql.SQLException;

public class CategoriaScreenViewModel extends ViewModelScreenContract<CategoriaModel> {

    private final CategoriaService categoriaService;

    final State<CategoriaModel> categoriaSelecionada = State.of(null);
    final State<String> nome = new State<>("");

    public CategoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.categoriaService = createOrReport(CategoriaService::new);
        this.onInit();
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
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar categorias"));
            }
        });
    }

    @Override
    public void populateFromModel() {
        var data = categoriaSelecionada.get();
        if (data != null) nome.set(data.getNome());
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
                            allDataList.removeIf(it -> it.getId().equals(model.getId()));
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
        boolean editando = modoEdicao.get();
        Async.Run(() -> {
            try {
                if (editando) {
                    var model = categoriaSelecionada.get();
                    if (model == null) return;
                    model.setNome(nome.get().trim());
                    categoriaService.atualizar(model);
                    CategoriaModel atualizada = new CategoriaModel();
                    atualizada.setId(model.getId());
                    atualizada.setNome(model.getNome());
                    atualizada.setDataCriacao(model.getDataCriacao());
                    UI.runOnUi(() -> {
                        allDataList.updateIf(it -> it.getId().equals(atualizada.getId()), it -> atualizada);
                        Components.ShowPopup(ctx, "Categoria atualizada com sucesso");
                        clearForm();
                    });
                } else {
                    var model = new CategoriaModel();
                    model.setNome(nome.get().trim());
                    var salvo = categoriaService.salvar(model);
                    UI.runOnUi(() -> {
                        allDataList.add(salvo);
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
        modoEdicao.set(false);
    }

    @Override
    public void onDestroy() throws Exception {
        this.categoriaService.close();
    }
}

package my_app.screens.categoriaScreen;

import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.lifecycle.viewmodel.component.ViewModelScreenContract;
import my_app.screens.components.Components;

import java.util.List;

public class CategoriaScreenViewModel extends ViewModelScreenContract {
    private final CategoriaRepository categoriaRepository;

    final ListState<CategoriaModel> categorias = ListState.of(List.of());
    final State<CategoriaModel> categoriaSelecionada = State.of(null);
    final State<String> nome = new State<>("");

    public CategoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        categoriaRepository = new CategoriaRepository();
        this.onInit();
    }

    @Override
    public void populateFromModel() {
        final var data = categoriaSelecionada.get();
        if (data != null) nome.set(data.nome);
    }

    void loadCategorias() {
        Async.Run(() -> {
            try {
                var list = categoriaRepository.listar();
                UI.runOnUi(() -> categorias.addAll(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao buscar clientes"));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        final var categoriaModel = categoriaSelecionada.get();
        if (categoriaModel == null) return;

        Components.ShowAlertAdvice("Deseja excluir categoria " + categoriaModel.nome, () -> {
            Async.Run(() -> {
                try {
                    categoriaRepository.excluirById(categoriaModel.id);
                    UI.runOnUi(() -> {
                        categorias.removeIf(it -> it.id.equals(categoriaModel.id));
                        Components.ShowPopup(ctx, "Categoria excluída com sucesso");
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao tentar excluir: " + e.getMessage()));
                }
            });
        });
    }

    @Override
    public void handleAddOrUpdate() {
        String nomeValue = nome.get().trim();

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }

        if (modoEdicao.get()) {
            if (categoriaSelecionada.get() == null) return;
            asyncUpdate(categoriaSelecionada.get().id, nomeValue);
        } else {
            asyncSalvar(nomeValue);
        }
    }

    @Override
    public void clearForm() {
        nome.set("");
    }

    private void asyncUpdate(long id, String nome) {
        Async.Run(() -> {
            try {
                var model = new CategoriaModel().fromIdAndDto(id, new CategoriaDto(nome));
                categoriaRepository.atualizar((CategoriaModel) model);
                UI.runOnUi(() -> {
                    categorias.updateIf(it -> it.id.equals(id), it -> (CategoriaModel) model);
                    Components.ShowPopup(ctx, "Categoria atualizado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void asyncSalvar(String nome) {
        Async.Run(() -> {
            try {
                var model = categoriaRepository.salvar(new CategoriaDto(nome));
                UI.runOnUi(() -> {
                    categorias.add(model);
                    Components.ShowPopup(ctx, "Categoria cadastrado com sucesso");
                    clearForm();
                    //TODO: SPAWNAR EVENT
                    //EventBus.getInstance().publish(new ClienteEvents.Criado(model));
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }
}
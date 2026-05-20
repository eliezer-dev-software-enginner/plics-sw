package my_app.screens.categoriaScreen;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.dto.CategoriaDto;
import my_app.db.dto.ClienteDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.ClienteModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.events.ClienteEvents;
import my_app.events.EventBus;
import my_app.lifecycle.viewmodel.component.ViewModelv2;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.util.List;

import static my_app.utils.Utils.*;

public class CategoriaScreenViewModel extends ViewModelv2 {
    private final ScreenContext ctx;
    private final CategoriaRepository categoriaRepository;

    State<Boolean> modoEdicao = State.of(false);

    ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);

    final ListState<CategoriaModel> categorias = ListState.of(List.of());
    final State<CategoriaModel> categoriaSelecionada = State.of(null);

    final State<String> nome = new State<>("");
    final State<Boolean> editMode = State.of(false);

    public CategoriaScreenViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        categoriaRepository = new CategoriaRepository();
        this.onInit();
    }

    @Override
    protected void onInit() {}

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

    void handleClickNew() {
        editMode.set(false);
        clearForm();
    }

    void handleClickMenuEdit() {
        editMode.set(true);
        populateFromCategoria();
    }

    void handleClickMenuClone() {
        editMode.set(false);
        populateFromCategoria();
    }

    void handleClickMenuDelete() {
        final var categoriaModel = categoriaSelecionada.get();
        if (categoriaModel == null) return;

        editMode.set(false);
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

    void handleAddOrUpdate() {
        String nomeValue = nome.get().trim();

        if (nomeValue.isEmpty()) {
            Components.ShowAlertError("Nome é obrigatório");
            return;
        }

        if (editMode.get()) {
            if (categoriaSelecionada.get() == null) return;
            asyncUpdate(categoriaSelecionada.get().id, nomeValue);
        } else {
            asyncSalvar(nomeValue);
        }
    }

    void clearForm() {
        nome.set("");
    }

    private void populateFromCategoria() {
        final var data = categoriaSelecionada.get();
        if (data != null) nome.set(data.nome);
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
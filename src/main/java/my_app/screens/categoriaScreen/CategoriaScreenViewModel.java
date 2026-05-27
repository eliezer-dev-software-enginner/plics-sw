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
    private CategoriaService categoriaService;

    final ListState<CategoriaModel> categorias = ListState.of(List.of());
    final State<CategoriaModel> categoriaSelecionada = State.of(null);
    final State<String> nome = new State<>("");

    public CategoriaScreenViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            categoriaService = new CategoriaService(getPersismSession());
        } catch (SQLException e) {
           UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
            return;
        }
        this.onInit();
    }

    @Override
    public void populateFromModel() {
        final var data = categoriaSelecionada.get();
        if (data != null) nome.set(data.getNome());
    }

    void loadCategorias() {
        Async.Run(() -> {
            try {
                var list = categoriaService.listar();
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

        Components.ShowAlertAdvice("Deseja excluir categoria " + categoriaModel.getNome(), () -> {
            Async.Run(() -> {
                try {
                    categoriaService.excluirById(categoriaModel.getId());
                    UI.runOnUi(() -> {
                        categorias.removeIf(it -> it.getId().equals(categoriaModel.getId()));
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
            asyncUpdate(categoriaSelecionada.get(), nomeValue);
        } else {
            asyncSalvar(nomeValue);
        }
    }

    @Override
    public void clearForm() {
        nome.set("");
    }

    private void asyncUpdate(CategoriaModel model, String nome) {
        try{
            validarNome(nome);
            Async.Run(() -> {
                try {
                    model.setNome(nome);
                    categoriaService.atualizar(model);
                    UI.runOnUi(() -> {
                        categorias.updateIf(it -> it.getId().equals(model.getId()), it -> model);
                        Components.ShowPopup(ctx, "Categoria atualizado com sucesso");
                        clearForm();
                    });
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
            });

        }catch (Exception e){
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
        }
    }

    private void asyncSalvar(String nome) {
        try{
            validarNome(nome.trim());

            Async.Run(() -> {
                try {
                    var model = new CategoriaModel();
                    model.setNome(nome);

                    categoriaService.salvar(model);
                    UI.runOnUi(() -> {
                        categorias.add(model);
                        Components.ShowPopup(ctx, "Categoria cadastrado com sucesso");
                        clearForm();
                        //TODO: SPAWNAR EVENT
                        //EventBus.getInstance().publish(new ClienteEvents.Criado(model));
                    });
                } catch (Exception e) {
                   throw new RuntimeException(e);
                }
            });
        }catch (Exception e){
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
        }
    }

    private void validarNome(String nome) {
        for (CategoriaModel categoriaModel : categorias.get()) {
            if(categoriaModel.getNome().equals(nome.trim())) {
                throw new RuntimeException("Esse nome já existe, use outro nome");
            }
        }
    }
}
package my_app.screens.tecnicoScreen;

import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.TecnicoModel;
import my_app.db.services.TecnicoService;
import my_app.domain.components.Components;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;

import java.sql.SQLException;
import java.util.List;

public class TecnicoScreenViewModel extends ViewModelScreenContract {

    private final TecnicoService tecnicoService;

    public final ListState<TecnicoModel> tecnicos = ListState.of(List.of());
    public final State<TecnicoModel> tecnicoSelected = new State<>(null);
    public final State<String> nome = new State<>("");

    public TecnicoScreenViewModel(ScreenContext ctx) {
        this(ctx, createTecnicoService());
    }

    public TecnicoScreenViewModel(ScreenContext ctx, TecnicoService tecnicoService) {
        super(ctx);
        this.tecnicoService = tecnicoService;
    }

    private static TecnicoService createTecnicoService() {
        try {
            return new TecnicoService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    public void loadTecnicos() {
        Async.Run(() -> {
            try {
                var list = tecnicoService.listar();
                UI.runOnUi(() -> tecnicos.addAll(list));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao carregar técnicos: " + e.getMessage()));
            }
        });
    }

    @Override
    public void handleClickMenuDelete() {
        var model = tecnicoSelected.get();
        if (model == null) return;

        var bodyMessage = "Tem certeza que deseja excluir o técnico: %s?".formatted(model.getNome());
        Components.ShowAlertAdvice(bodyMessage, () -> {
            Async.Run(() -> {
                try {
                    tecnicoService.excluirById(model.getId());

                    EventBus.getInstance().publish(EntityEvent.excluido(model.getId().longValue()));

                    UI.runOnUi(() -> {
                        tecnicos.removeIf(it -> it.getId().equals(model.getId()));
                        Components.ShowPopup(ctx, "Técnico excluído com sucesso");
                        clearForm();
                    });
                } catch (Exception e) {
                    UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir técnico: " + e.getMessage()));
                }
            });
        });
    }

    @Override
    public void handleAddOrUpdate() {
        var value = nome.get().trim();

        if (value.isEmpty()) {
            Components.ShowAlertError("Preencha o nome do técnico");
            return;
        }

        if (modoEdicao.get()) {
            asyncAtualizar(value);
        } else {
            asyncSalvar(value);
        }
    }

    private void asyncSalvar(String value) {
        Async.Run(() -> {
            try {
                var model = new TecnicoModel();
                model.setNome(value);
                var salvo = tecnicoService.salvar(model);

                EventBus.getInstance().publish(EntityEvent.criado(salvo));

                UI.runOnUi(() -> {
                    tecnicos.add(salvo);
                    Components.ShowPopup(ctx, "Técnico '" + salvo.getNome() + "' cadastrado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao cadastrar técnico: " + e.getMessage()));
            }
        });
    }

    private void asyncAtualizar(String value) {
        Async.Run(() -> {
            try {
                TecnicoModel original = tecnicoSelected.get();
                if (original == null) return;

                original.setNome(value);
                tecnicoService.atualizar(original);

                EventBus.getInstance().publish(EntityEvent.editado(original));

                TecnicoModel atualizado = new TecnicoModel();
                atualizado.setId(original.getId());
                atualizado.setNome(original.getNome());
                atualizado.setDataCriacao(original.getDataCriacao());

                UI.runOnUi(() -> {
                    tecnicos.updateIf(it -> it.getId().equals(atualizado.getId()), it -> atualizado);
                    Components.ShowPopup(ctx, "Técnico atualizado com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao atualizar técnico: " + e.getMessage()));
            }
        });
    }

    @Override
    public void clearForm() {
        nome.set("");
        modoEdicao.set(false);
    }

    @Override
    public void populateFromModel() {
        if (tecnicoSelected.get() == null) return;
        nome.set(tecnicoSelected.get().getNome());
    }
}

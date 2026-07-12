package my_app.screens.tecnicoScreen;

import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.TecnicoModel;
import my_app.db.services.TecnicoService;
import my_app.domain.components.Components;
import my_app.core.events.EntityEvent;
import my_app.core.events.EventBus;
import my_app.domain.ViewModelScreenContract;

import java.sql.SQLException;

public class TecnicoScreenViewModel extends ViewModelScreenContract<TecnicoModel> {

    private final TecnicoService tecnicoService;

    public final State<TecnicoModel> tecnicoSelected = new State<>(null);
    public final State<String> nome = new State<>("");

    public TecnicoScreenViewModel(ScreenContext ctx) {
        super(ctx);
        this.tecnicoService = createOrReport(TecnicoService::new);
    }

    @Override
    protected boolean matchesSearch(TecnicoModel model, String query) {
        return contains(model.getNome(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    @Override
    public void fetchListData() {
        Async.Run(() -> {
            try {
                var list = tecnicoService.listar();
                UI.runOnUi(() -> allDataList.set(list));
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
        Components.ShowAlertAdvice(bodyMessage, () -> Async.Run(() -> {
            try {
                tecnicoService.excluirById(model.getId());

                EventBus.getInstance().publish(EntityEvent.excluido(model.getId().longValue()));

                UI.runOnUi(() -> {
                    allDataList.removeIf(it -> it.getId().equals(model.getId()));
                    Components.ShowPopup(ctx, "Técnico excluído com sucesso");
                    clearForm();
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao excluir técnico: " + e.getMessage()));
            }
        }));
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
                    allDataList.add(salvo);
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
                    allDataList.updateIf(it -> it.getId().equals(atualizado.getId()), it -> atualizado);
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

    @Override
    public void onDestroy() throws Exception {
        this.tecnicoService.close();
    }
}

package my_app.screens.preferenciasScreen;

import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;

import java.sql.SQLException;

public class PreferenciasViewModel extends ViewModelScreenContract {

    private final PreferenciasService preferenciasService;

    final State<String> habilitarCredenciaisSelected = State.of("Não");
    final State<String> loginState = State.of("");
    final State<String> passwordState = State.of("");

    private PreferenciasModel prefLoaded;

    public PreferenciasViewModel(ScreenContext ctx) {
        super(ctx);
        try {
            preferenciasService = new PreferenciasService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
        this.onInit();
    }

    void load() {
        Async.Run(() -> {
            try {
                var prefs = preferenciasService.listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    UI.runOnUi(() -> {
                        prefLoaded = pref;
                        habilitarCredenciaisSelected.set(pref.getCredenciaisHabilitadas() == 1 ? "Sim" : "Não");
                        loginState.set(pref.getLogin());
                        passwordState.set(pref.getSenha());
                    });
                }
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    void salvar() {
        Async.Run(() -> {
            try {
                prefLoaded.setCredenciaisHabilitadas(habilitarCredenciaisSelected.get().equals("Sim") ? 1 : 0);
                prefLoaded.setLogin(loginState.get());
                prefLoaded.setSenha(passwordState.get());
                preferenciasService.atualizar(prefLoaded);
                UI.runOnUi(() -> Components.ShowPopup(ctx, "Preferências salvas com sucesso!"));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    @Override
    public void populateFromModel() {}

    @Override
    public void clearForm() {}

    @Override
    public void handleAddOrUpdate() {
        salvar();
    }

    @Override
    public void handleClickMenuDelete() {}
}

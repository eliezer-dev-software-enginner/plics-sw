package my_app.screens.authScreen;

import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.domain.components.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AuthScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(AuthScreenViewModel.class);
    private final PreferenciasService preferenciasService;

    final State<Boolean> showLicensaState = State.of(true);

    final State<String> licensaState = State.of("");
    final State<String> loginState = State.of("");
    final State<String> passwordState = State.of("");

    private PreferenciasModel prefRecuperada;

    public AuthScreenViewModel() {
        this(createPreferenciasService());
    }

    public AuthScreenViewModel(PreferenciasService preferenciasService) {
        this.preferenciasService = preferenciasService;
    }

    private static PreferenciasService createPreferenciasService() {
        try {
            return new PreferenciasService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void load() {
        Async.Run(() -> {
            try {
                var prefs = preferenciasService.listar();
                if (!prefs.isEmpty()) {
                    prefRecuperada = prefs.getFirst();
                    UI.runOnUi(() -> showLicensaState.set(prefRecuperada.isFirstAccess()));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    void entrar(ScreenContext ctx) {
        var licensaValue = licensaState.get().trim();
        var licensaBase = "984e2bb76c7b627641b6b7dc080f8e23";

        if (showLicensaState.get() && (licensaValue.isEmpty() || !licensaValue.equals(licensaBase))) {
            Components.ShowAlertError("Licença inválida");
            return;
        }

        String loginValue = loginState.get().trim();
        String senhaValue = passwordState.get().trim();

        if (!prefRecuperada.getLogin().trim().equals(loginValue) || !prefRecuperada.getSenha().trim().equals(senhaValue)) {
            Components.ShowAlertError("Login inválido");
            return;
        }

        Components.ShowPopup(ctx, "Login efetuado com sucesso!");
        Async.Run(() -> {
            try {
                prefRecuperada.setPrimeiroAcesso(0);
                prefRecuperada.setCredenciaisHabilitadas(1);
                preferenciasService.atualizar(prefRecuperada);
                UI.runOnUi(() -> ctx.navigate("home"));
            } catch (Exception e) {
                log.error("Erro ao fazer login", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao entrar: " + e.getMessage()));
            }
        });
    }
}

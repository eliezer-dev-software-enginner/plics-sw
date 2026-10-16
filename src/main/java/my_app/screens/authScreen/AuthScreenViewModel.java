package my_app.screens.authScreen;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.State;
import my_app.core.AppRoutes;
import my_app.core.components.Components;
import my_app.core.db.models.PreferenciasModel;
import my_app.core.db.services.PreferenciasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AuthScreenViewModel {
    private static final Logger log = LoggerFactory.getLogger(AuthScreenViewModel.class);

    private final PreferenciasService preferenciasService;
    final State<Boolean> loadingState = State.of(true);
    final State<String> loginState = State.of("");
    final State<String> passwordState = State.of("");

    private PreferenciasModel prefRecuperada;

    public AuthScreenViewModel() {
        this.preferenciasService = createOrReport(PreferenciasService::new);
    }

    private static <T> T createOrReport(megalodonte.utils.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            megalodonte.application.ErrorReporter.handle(e);
            throw new IllegalStateException(e);
        }
    }

    void load() {
        Async.Run(() -> {
            try {
                var prefs = preferenciasService.listar();
                prefRecuperada = prefs.isEmpty() ? null : prefs.getFirst();
                UI.runOnUi(() -> {
                    log.info("first access: {}", prefRecuperada != null && prefRecuperada.isFirstAccess());
                    loadingState.set(false);
                });
            } catch (SQLException e) {
                log.error("Erro ao carregar preferências de acesso", e);
                UI.runOnUi(() -> {
                    loadingState.set(false);
                    Components.ShowAlertError("Não foi possível carregar as preferências de acesso.");
                });
            }
        });
    }

    void entrar(ScreenContextInterface ctx) {
        if (prefRecuperada == null) {
            Components.ShowAlertError("Preferências de acesso indisponíveis.");
            return;
        }
        String loginValue = loginState.get().trim();
        String senhaValue = passwordState.get().trim();
        if (!loginValue.equals(prefRecuperada.getLogin()) || !senhaValue.equals(prefRecuperada.getSenha())) {
            Components.ShowAlertError("Login inválido");
            return;
        }

        Components.ShowPopup(ctx, "Login efetuado com sucesso!");
        Async.Run(() -> {
            try {
                prefRecuperada.setPrimeiroAcesso(0);
                prefRecuperada.setCredenciaisHabilitadas(1);
                preferenciasService.atualizar(prefRecuperada);
                UI.runOnUi(() -> ctx.navigate(AppRoutes.Screens.HOME.name()));
            } catch (Exception e) {
                log.error("Erro ao fazer login", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao entrar: " + e.getMessage()));
            }
        });
    }

    public void onDestroy() throws Exception {
        preferenciasService.close();
    }
}

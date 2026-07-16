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
import java.util.List;

public class AuthScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(AuthScreenViewModel.class);
    public static final List<String> LICENCAS_PRODUCAO = List.of(
            "5fZl2OI7f2ksjc8YRzBRR0ycjsCzycXyrX",
            "Fh0OofMRVVE30elP6KvES4AuBBEYcK8qZIyXzL",
            "5Zlix2GJbCqM2vYcxWSHk3E5YI8bf2ePHAxaWK",
            "fBiO4l1qmTj3WdwhbU5At7UTAWiBuoBfk1FMXo"
    );

    private final PreferenciasService preferenciasService;

    final State<Boolean> showLicensaState = State.of(true);

    final State<String> licensaState = State.of("");
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
                if (!prefs.isEmpty()) {
                    prefRecuperada = prefs.getFirst();
                    UI.runOnUi(() -> {
                        boolean invalid = isLicenseInvalid(prefRecuperada.getLicensa());
                        boolean firstAccess = prefRecuperada.isFirstAccess();
                        log.info("first access: {}", firstAccess);
                        showLicensaState.set(firstAccess || invalid);
                    });
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static boolean isLicenseInvalid(String license) {
        return !LICENCAS_PRODUCAO.contains(license);
    }

    void entrar(ScreenContext ctx) {
        var licenseValue = licensaState.get().trim();

        if (showLicensaState.get()) {
            if (licenseValue.isEmpty()) {
                Components.ShowAlertError("Licença inválida");
                return;
            }

            if (isLicenseInvalid(licenseValue)) {
                Components.ShowAlertError("Licença inválida");
                return;
            }

            prefRecuperada.setLicensa(licenseValue);
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

    public void onDestroy() throws Exception {
        preferenciasService.close();
    }
}

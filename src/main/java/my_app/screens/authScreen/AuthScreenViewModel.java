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
import java.time.LocalDate;
import java.util.List;

public class AuthScreenViewModel {

    private static final Logger log = LoggerFactory.getLogger(AuthScreenViewModel.class);
    public static final List<String> LICENCAS_PRODUCAO = List.of(
            "984e2bb76c7b627641b6b7dc080f8e23",//TODO: REMOVER NA 1.0.7
            "5fZl2OI7f2ksjc8YRzBRR0ycjsCzycXyrX",
            "o2OJI2OJ7EOSGXiUju6WE8zUZef1pZcfGSiqMk",
            "Fh0OofMRVVE30elP6KvES4AuBBEYcK8qZIyXzL",
            "5Zlix2GJbCqM2vYcxWSHk3E5YI8bf2ePHAxaWK",
            "fBiO4l1qmTj3WdwhbU5At7UTAWiBuoBfk1FMXo"
    );
    static final String LICENSA_TESTE = "QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ";

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
                    UI.runOnUi(() -> {
                        boolean expiredTest = isLicensaTesteExpirada(prefRecuperada.getLicensa());
                        showLicensaState.set(prefRecuperada.isFirstAccess() || expiredTest);
                    });
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    boolean isLicensaTesteExpirada(String licensa) {
        return LICENSA_TESTE.equals(licensa) && LocalDate.now().getDayOfMonth() > 11;
    }

    void entrar(ScreenContext ctx) {
        var licensaValue = licensaState.get().trim();

        if (showLicensaState.get()) {
            if (licensaValue.isEmpty()) {
                Components.ShowAlertError("Licença inválida");
                return;
            }
            boolean isProducao = LICENCAS_PRODUCAO.contains(licensaValue);
            boolean isTeste = licensaValue.equals(LICENSA_TESTE);
            if (!isProducao && !isTeste) {
                Components.ShowAlertError("Licença inválida");
                return;
            }
            if (isTeste && LocalDate.now().getDayOfMonth() > 11) {
                Components.ShowAlertError("Licença de teste expirada");
                return;
            }
            prefRecuperada.setLicensa(licensaValue);
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

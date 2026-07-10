package my_app.screens.preferenciasScreen;

import jssc.SerialPortList;

import javax.print.PrintServiceLookup;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class PreferenciasViewModel extends ViewModelScreenContract {
    private static final Logger log = LoggerFactory.getLogger(PreferenciasViewModel.class);

    private final PreferenciasService preferenciasService;

    final State<String> habilitarCredenciaisSelected = State.of("Não");
    final State<String> loginState = State.of("");
    final State<String> passwordState = State.of("");
    final ListState<String> comportsState = new ListState<>(List.of("N/D"));
    final State<String> comportsStateSelected = State.of("N/D");

    private PreferenciasModel prefLoaded;

    public PreferenciasViewModel(ScreenContext ctx) {
        this(ctx, createPreferenciasService());
    }

    public PreferenciasViewModel(ScreenContext ctx, PreferenciasService preferenciasService) {
        super(ctx);
        this.preferenciasService = preferenciasService;
        this.onInit();
    }

    private static PreferenciasService createPreferenciasService() {
        try {
            return new PreferenciasService();
        } catch (SQLException e) {
            UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    void load() {
        Async.Run(() -> {
            try {
                try {
                    String[] portNames = SerialPortList.getPortNames();
                    for (String name : portNames) {
                        UI.runOnUi(()-> comportsState.add(name + " - Serial"));
                    }
                } catch (Throwable e) {
                    log.error("Erro ao carregar portas seriais: {}", e.getMessage(), e);
                    UI.runOnUi(() -> Components.ShowAlertError(
                            "Não foi possível carregar portas seriais.\n" +
                            "O recurso de impressão em porta COM será desabilitado.\n" +
                            "Erro: " + e.getMessage()));
                }

                try {
                    javax.print.PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
                    for (var ps : printServices) {
                        String printerName = ps.getName();
                        UI.runOnUi(() -> comportsState.add(printerName + " - Spooler"));
                    }
                } catch (Throwable e) {
                    log.error("Erro ao carregar impressoras Windows: {}", e.getMessage(), e);
                }

                var prefs = preferenciasService.listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    prefLoaded = pref;
                UI.runOnUi(() -> {
                        habilitarCredenciaisSelected.set(pref.getCredenciaisHabilitadas() == 1 ? "Sim" : "Não");
                        loginState.set(pref.getLogin());
                        passwordState.set(pref.getSenha());
                        var savedPort = pref.getPortaImpressora();
                        if (savedPort != null && !savedPort.isBlank()) {
                            comportsState.get().stream()
                                    .filter(name -> name.startsWith(savedPort))
                                    .findFirst()
                                    .ifPresent(comportsStateSelected::set);
                        }
                    });
                }
            } catch (SQLException e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    void salvar() {
        var erro = validar();
        if (erro != null) {
            UI.runOnUi(() -> Components.ShowAlertError(erro));
            return;
        }

        var habilitar = habilitarCredenciaisSelected.get().equals("Sim");
        var login = loginState.get();
        var senha = passwordState.get();

        Async.Run(() -> {
            try {
                prefLoaded.setCredenciaisHabilitadas(habilitar ? 1 : 0);
                prefLoaded.setLogin(login);
                prefLoaded.setSenha(senha);
                savePrinterPort();
                preferenciasService.atualizar(prefLoaded);
                UI.runOnUi(() -> Components.ShowPopup(ctx, "Preferências salvas com sucesso!"));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    private void savePrinterPort() {
        var selected = comportsStateSelected.get();
        if (selected != null && !selected.equals("N/D")) {
            var systemPortName = selected.split(" - ")[0];
            prefLoaded.setPortaImpressora(systemPortName);
        } else {
            prefLoaded.setPortaImpressora(null);
        }
    }

    String validar() {
        var habilitar = habilitarCredenciaisSelected.get().equals("Sim");
        if (!habilitar) return null;
        if (loginState.get().isBlank()) return "Login é obrigatório";
        if (passwordState.get().isBlank()) return "Senha é obrigatória";
        return null;
    }

    void deletarTodosDados() {
      Components.ShowAlertError("Opção temporariamente indisponível!");
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

    @Override
    public void onDestroy() throws Exception {
        this.preferenciasService.close();
    }
}

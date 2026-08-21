package my_app.screens.preferenciasScreen;

import jssc.SerialPortList;

import javax.print.PrintServiceLookup;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.ListState;
import my_app.core.AppRoutes;
import my_app.db.models.PreferenciasModel;
import my_app.db.services.PreferenciasService;
import my_app.domain.components.Components;
import my_app.domain.ViewModelScreenContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class PreferenciasViewModel extends ViewModelScreenContract<PreferenciasModel> {
    private static final Logger log = LoggerFactory.getLogger(PreferenciasViewModel.class);

    private final PreferenciasService preferenciasService;

    final State<String> habilitarCredenciaisSelected = State.of("Não");
    final State<String> habilitarPopupInstagramSelected = State.of("Não");
    final State<String> loginState = State.of("");
    final State<String> passwordState = State.of("");
    final ListState<String> comportsState = new ListState<>(List.of("N/D"));
    final State<String> comportsStateSelected = State.of("N/D");

    private PreferenciasModel prefLoaded;

    public PreferenciasViewModel(ScreenContext ctx) {
        super(ctx);
        this.preferenciasService = createOrReport(PreferenciasService::new);
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
                        habilitarPopupInstagramSelected.set(pref.getPopupInstagramHabilitado() == 1 ? "Sim" : "Não");
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
        var model = populateModelFromFields();

        Async.Run(() -> {
            try {
                preferenciasService.atualizar(model);
                UI.runOnUi(() -> Components.ShowPopup(ctx, "Preferências salvas com sucesso!"));
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    public void signOut() throws SQLException {
       prefLoaded.setCredenciaisHabilitadas(1);
       prefLoaded.setPopupInstagramHabilitado(1);
       prefLoaded.setPrimeiroAcesso(1);
       prefLoaded.setLogin("admin");
       prefLoaded.setSenha("1234");

        preferenciasService.atualizar(prefLoaded);

        ctx.navigateAndCloseOthers(AppRoutes.Screens.WELCOME.name());
    }

    @Override
    protected boolean matchesSearch(PreferenciasModel model, String query) {
        return false;
    }

    @Override
    public void fetchListData() {
        load();
    }

    @Override
    public void populateFieldsFromModel() {}

    @Override
    public PreferenciasModel populateModelFromFields() {
        prefLoaded.setCredenciaisHabilitadas(habilitarCredenciaisSelected.get().equals("Sim") ? 1 : 0);
        prefLoaded.setPopupInstagramHabilitado(habilitarPopupInstagramSelected.get().equals("Sim") ? 1 : 0);
        prefLoaded.setLogin(loginState.get());
        prefLoaded.setSenha(passwordState.get());

        var selectedPort = comportsStateSelected.get();
        prefLoaded.setPortaImpressora(selectedPort != null && !selectedPort.equals("N/D")
                ? selectedPort.split(" - ")[0]
                : null);

        return prefLoaded;
    }

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

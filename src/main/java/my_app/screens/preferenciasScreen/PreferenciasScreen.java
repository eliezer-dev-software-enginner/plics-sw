package my_app.screens.preferenciasScreen;

import com.google.gson.Gson;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.core.components.Components;

import java.sql.SQLException;

/** Preferences layout and confirmation live in JSONC; persistence stays in the view model. */
public final class PreferenciasScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(PreferenciasScreen.class, "/views/preferencias.view.jsonc");
    private static final Gson JSON = new Gson();
    private final PreferenciasViewModel viewModel;
    private final UiSession session;
    private final Subscription printerUpdates;
    private final Subscription credentialVisibilityUpdates;

    public PreferenciasScreen(ScreenContextInterface context) {
        viewModel = new PreferenciasViewModel(context);
        session = VIEW.open(new Actions()
                .register("save", ignored -> viewModel.salvar())
                .register("askSignOut", screen -> screen.states().state("confirmSignOut", Boolean.class).set(true))
                .register("cancelSignOut", screen -> screen.states().state("confirmSignOut", Boolean.class).set(false))
                .register("signOut", ignored -> {
                    try { viewModel.signOut(); }
                    catch (SQLException error) { Components.ShowAlertError(error.getMessage()); }
                }));
        session.bind("credentialsEnabled", viewModel.habilitarCredenciaisSelected, String.class)
                .bind("instagramPopup", viewModel.habilitarPopupInstagramSelected, String.class)
                .bind("printer", viewModel.comportsStateSelected, String.class)
                .bind("login", viewModel.loginState, String.class)
                .bind("password", viewModel.passwordState, String.class);
        printerUpdates = viewModel.comportsState.observe(items -> session.states().string("printers").set(JSON.toJson(items)));
        credentialVisibilityUpdates = viewModel.habilitarCredenciaisSelected.observe(value ->
                session.states().state("showCredentials", Boolean.class).set("Sim".equals(value)));
    }

    @Override public Component render() { return session.render(); }
    @Override public void onMount() { viewModel.load(); }
    @Override public void onDestroy() {
        printerUpdates.close();
        credentialVisibilityUpdates.close();
        session.close();
        try { viewModel.onDestroy(); }
        catch (Exception error) { throw new IllegalStateException("Could not close preferences", error); }
    }
}

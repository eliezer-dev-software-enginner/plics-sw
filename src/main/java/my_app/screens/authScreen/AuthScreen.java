package my_app.screens.authScreen;

import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.state.Subscription;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.Main;
import my_app.core.Data;

/** Authentication view and controls are declared in JSONC; this adapter owns domain actions. */
public final class AuthScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(AuthScreen.class, "/views/auth.view.jsonc");
    private final ScreenContextInterface context;
    private final AuthScreenViewModel viewModel = new AuthScreenViewModel();
    private final UiSession session;
    private final Subscription licenseVisibility;
    private final Subscription loading;

    public AuthScreen(ScreenContextInterface context) {
        this.context = context;
        session = VIEW.open(new Actions()
            .register("entrar", screen -> {
                viewModel.licensaState.set(screen.states().string("licenca").get());
                viewModel.loginState.set(screen.states().string("login").get());
                viewModel.passwordState.set(screen.states().string("senha").get());
                viewModel.entrar(context);
            })
            .register("abrirSuporte", ignored -> Redirect.to(Data.linkWhatsappSupport))
            .register("abrirSite", ignored -> Redirect.to(Data.linkWebsiteOfficial)));
        licenseVisibility = viewModel.showLicensaState.observe(
                visible -> session.states().state("mostrarLicenca", Boolean.class).set(visible));
        loading = viewModel.loadingState.observe(
                value -> session.states().state("carregando", Boolean.class).set(value));
        session.states().string("versao").set("Plics - SW " + Main.APP_VERSION);
        session.states().string("suporte").set(Data.getNumberWhatsappSupportFormatted() + " - Suporte garantido.");
    }

    @Override public void onMount() {
        context.selfStage().getIcons().add(Main.loadIcon());
        viewModel.load();
    }

    @Override public Component render() { return session.render(); }

    @Override public void onDestroy() {
        licenseVisibility.close();
        loading.close();
        session.close();
        try { viewModel.onDestroy(); }
        catch (Exception e) { throw new IllegalStateException("Could not close authentication services", e); }
    }
}

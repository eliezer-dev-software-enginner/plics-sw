package my_app.screens.homeScreen;

import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import megalodonte.jsonc.UiSession;
import my_app.Main;
import my_app.core.AppRoutes;

/** Home layout, navigation and promotion are declared in JSONC. */
public final class HomeScreen implements ScreenComponent {
    private static final UiDefinition VIEW = UiDefinition.fromResource(HomeScreen.class, "/views/home.view.jsonc");
    private final HomeScreenViewModel viewModel;
    private final UiSession session;
    private final HomeAdsPane adsPane;

    public HomeScreen(ScreenContextInterface ctx) {
        viewModel = new HomeScreenViewModel(ctx);
        adsPane = new HomeAdsPane();
        session = VIEW.open(new Actions()
                .register("sales", ignored -> open(ctx, AppRoutes.Screens.VENDAS))
                .register("products", ignored -> open(ctx, AppRoutes.Screens.PRODUTOS))
                .register("customers", ignored -> open(ctx, AppRoutes.Screens.CLIENTES))
                .register("receivables", ignored -> open(ctx, AppRoutes.Screens.COMPRAS_A_RECEBER))
                .register("pos", ignored -> open(ctx, AppRoutes.Screens.PDV))
                .register("categories", ignored -> open(ctx, AppRoutes.Screens.CATEGORIAS))
                .register("suppliers", ignored -> open(ctx, AppRoutes.Screens.FORNECEDORES))
                .register("orders", ignored -> open(ctx, AppRoutes.Screens.PEDIDOS))
                .register("company", ignored -> open(ctx, AppRoutes.Screens.EMPRESA))
                .register("reports", ignored -> open(ctx, AppRoutes.Screens.RELATORIOS))
                .register("preferences", ignored -> open(ctx, AppRoutes.Screens.PREFERENCIAS))
                .register("feedback", ignored -> open(ctx, AppRoutes.Screens.SUGERIR_MELHORIA))
                .register("updates", ignored -> open(ctx, AppRoutes.Screens.INFO_UPDATE))
                .register("logs", ignored -> open(ctx, AppRoutes.Screens.LOGS))
                .register("terms", ignored -> open(ctx, AppRoutes.Screens.TERMOS_DE_USO))
                .register("checkUpdates", ignored -> viewModel.verificarAtualizacao())
                .register("videos", ignored -> Redirect.to("https://youtube.com/playlist?list=PLG06evrpS2RlBbxDBYcDkVKpWrEoSy8tq&si=2-MbcHrUrJsHJxyK")));
        session.mount("urgentAds", adsPane.urgentComponent());
        session.mount("regularAds", adsPane.regularComponent());
        session.display("receitas", viewModel.receitas, String.class);
        session.display("despesas", viewModel.despesas, String.class);
        session.display("lucroLiquido", viewModel.lucroLiquido, String.class);
        session.display("mesAtual", viewModel.mesAtual, String.class);
        session.display("vendasHoje", viewModel.vendasHoje, String.class);
        session.states().set("canCheckUpdates", !Main.isMicrosoftStore);
    }

    private static void open(ScreenContextInterface ctx, AppRoutes.Screens screen) {
        ctx.spawnWindow(screen.name());
    }

    @Override public Component render() { return session.render(); }
    @Override public void onDestroy() {
        adsPane.close();
        session.close();
        try { viewModel.onDestroy(); }
        catch (Exception error) { throw new IllegalStateException("Could not close home services", error); }
    }
}

package my_app.screens.authScreen;

import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.Main;
import my_app.domain.Data;
import my_app.domain.components.Components;

public class AuthScreen implements ScreenComponent {
    private final ScreenContext ctx;
    private final AuthScreenViewModel vm;

    public AuthScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.vm = new AuthScreenViewModel();
    }

    @Override
    public void onMount() {
        ctx.selfStage().getIcons().add(Main.loadIcon());
        vm.load();
    }

    @Override
    public void onDestroy() {
        try {
            vm.onDestroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Component render() {
        return new Container(new ContainerProps().paddingAll(20).bgImage("/assets/bgAuth.jpg")).children(
                new Row(new RowProps()).children(Components.FormSubtitle("Plics - SW " + Main.APP_VERSION, "white")),
                new SpacerVertical(20),
                new Row().children(
                        new Column().children(
                                Components.FormSubtitle("Realize já seu login na Plics SW", "white"),
                                new SpacerVertical(ThemeManager.theme().spacing().md()),
                                new Card(
                                        new Column(new ColumnProps().maxWidth(185)).children(
                                                Show.when(vm.showLicensaState, () -> Components.InputColumnAuthFill("Licença",
                                                        vm.licensaState, "Ex: ABC123")),
                                                new SpacerVertical(ThemeManager.theme().spacing().sm()),
                                                Components.InputColumnAuthFill("Login", vm.loginState, "Ex: admin"),
                                                new SpacerVertical(ThemeManager.theme().spacing().sm()),
                                                Components.InputColumnAuthFill("Senha", vm.passwordState, "Digite sua senha"),
                                                new SpacerVertical(ThemeManager.theme().spacing().md()),
                                                Components.ButtonCadastro("Entrar", () -> vm.entrar(ctx))
                                        ),
                                        new CardProps().paddingAll(10).borderRadius(10)
                                )
                        ),
                        new Row(new RowProps().fillWidth()),
                        new Column(new ColumnProps().maxWidth(170)).children(
                                new Column(new ColumnProps().centerHorizontally()).children(
                                        new Image("/assets/qrcode2.png", new ImageProps().size(170)),
                                        new Text("Plics - SW", new TextProps().color("white").bold())
                                ),
                                new SpacerVertical(10),
                                new TextFlow(new Text("Scaneie o QRCode para ir para o suporte no WhatsApp.",
                                        new TextProps().textColor("#fff").fontSize(13)))
                        )
                ),
                new Column(new ColumnProps().fillHeight()),
                Components.imageWithTextRow("/assets/whatsapp.png", Data.getNumberWhatsappSupportFormatted() + " - Suporte garantido."),
                new Button("Ir para o Suporte (24h)", new ButtonProps().bgColor("#25D366").textColor("black")).onClick(() -> Redirect.to(Data.linkWhatsappSupport)),
                new SpacerVertical(15),
                new Button("Ir para o Site Oficial").onClick(() -> Redirect.to(Data.linkWebsiteOfficial))
        );
    }
}

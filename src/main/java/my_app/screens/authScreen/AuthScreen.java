package my_app.screens.authScreen;

import megalodonte.base.Redirect;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
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
        ctx.selfStage().getIcons().add(Main.loadIcon());
        this.vm = new AuthScreenViewModel();
    }

    @Override
    public void onMount() {
        ctx.selfStage().getIcons().add(Main.loadIcon());
        vm.load();
    }

    @Override
    public Component render() {
        return new Container(new ContainerProps().paddingAll(20).bgImage("/assets/bgAuth.jpg")).children(
                new Row(new RowProps()).children(new Text("Plics - SW " + Main.APP_VERSION, new TextProps().color("white").bold())),
                new SpacerVertical(20),
                new Row().children(
                        new Column().children(
                                new Text("Realize já seu login na Plics SW", new TextProps().color("white").fontSize(14)),
                                Show.when(vm.showLicensaState, () -> Components.InputColumn("Licença", vm.licensaState, "")),
                                Components.InputColumn("Login", vm.loginState, ""),
                                Components.InputColumn("Senha", vm.passwordState, ""),
                                new SpacerVertical(10),
                                Components.ButtonCadastro("Entrar", () -> vm.entrar(ctx))
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

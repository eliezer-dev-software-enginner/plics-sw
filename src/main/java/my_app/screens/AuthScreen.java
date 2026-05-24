package my_app.screens;

import megalodonte.base.Redirect;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.Main;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.domain.Data;
import my_app.domain.components.Components;
//import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;

import java.sql.SQLException;

public class AuthScreen implements ScreenComponent {
    private final ScreenContext ctx;
    private final PreferenciasRepository preferenciasRepository;

    State<Boolean> showLicensaState = State.of(true);

    State<String> licensaState = State.of("");
    State<String> loginState = State.of("");
    State<String> passwordState = State.of("");

    PreferenciasModel prefRecuperada;

    public AuthScreen(ScreenContext ctx) {
        this.ctx = ctx;
        preferenciasRepository = new PreferenciasRepository();
    }

    @Override
    public void onMount() {
        Async.Run(()->{
            try{
                var prefs = preferenciasRepository.listar();
                if(!prefs.isEmpty()){
                    prefRecuperada = prefs.getFirst();
                        UI.runOnUi(()-> showLicensaState.set(prefRecuperada.isFirstAccess()));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Component render() {
        return new Container(new ContainerProps().paddingAll(20).bgImage("/assets/bgAuth.jpg")).children(
                new Row(new RowProps()).children(new Text("Plics - SW " + Main.APP_VERSION, new TextProps().color("white").bold())),
                new SpacerVertical(20),
                new Row().children(
                        new Column().children(
                                new Text("Realize já seu login na Plics SW", new TextProps().color("white").fontSize(14)),
                                Show.when(showLicensaState, () -> Components.InputColumn("Licença", licensaState, "")),
                                Components.InputColumn("Login", loginState, ""),
                                Components.InputColumn("Senha", passwordState, ""),
                                new SpacerVertical(10),
                                Components.ButtonCadastro("Entrar", this::entrar)
                        ),
                        new Row(new RowProps().fillWidth()),
                        //qrCode
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
                new Column(new ColumnProps().fillHeight()),//TODO: TROCAR PARA SPACERVERTICAL.FILL
                //TODO: TROCAR QRCODE
               Components.imageWithTextRow("/assets/whatsapp.png",Data.getNumberWhatsappSupportFormatted() + " - Suporte garantido." ),
                new Button("Ir para o Suporte (24h)", new ButtonProps().bgColor("#25D366").textColor("black")).onClick(()-> Redirect.to(Data.linkWhatsappSupport)),
                new SpacerVertical(15),
                new Button("Ir para o Site Oficial").onClick(()-> Redirect.to(Data.linkWebsiteOfficial))
        );
    }

    void entrar(){
        var licensaValue = licensaState.get().trim();
        var licensaBase = "984e2bb76c7b627641b6b7dc080f8e23";

        if(showLicensaState.get() && (licensaValue.isEmpty() || !licensaValue.equals(licensaBase))){
            Components.ShowAlertError("Licença inválida");
            return;
        }

        String loginValue = loginState.get().trim();
        String senhaValue = passwordState.get().trim();

        if(!prefRecuperada.login.trim().equals(loginValue) || !prefRecuperada.senha.trim().equals(senhaValue)){
            Components.ShowAlertError("Login inválido");
            return;
        }

        Components.ShowPopup(ctx, "Login efetuado com sucesso!");
            try {
                var prefs = new PreferenciasRepository().listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    pref.primeiroAcesso = 0;
                    pref.credenciaisHabilitadas = 1;
                    new PreferenciasRepository().atualizar(pref);
                    //router.navigateTo("home");
                    ctx.navigate("home");
                }
            } catch (Exception e) {
                e.printStackTrace();
                UI.runOnUi(()->   Components.ShowAlertError("Erro ao entrar: " + e.getMessage()));
            }

    }
}
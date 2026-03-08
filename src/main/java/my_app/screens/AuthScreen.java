package my_app.screens;

import megalodonte.Show;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.PreferenciasModel;
import my_app.db.repositories.PreferenciasRepository;
import my_app.screens.components.Components;

import java.sql.SQLException;

public class AuthScreen implements ScreenComponent {
    private final Router router;
    private final PreferenciasRepository preferenciasRepository;

    State<Boolean> showLicensaState = State.of(true);

    State<String> licensaState = State.of("");
    State<String> loginState = State.of("");
    State<String> passwordState = State.of("");

    PreferenciasModel prefRecuperada;

    public AuthScreen(Router router) {
        this.router = router;
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
                new Row(new RowProps()).children(new Text("Plics - SW", new TextProps().color("white").bold())),
                new SpacerVertical(20),
                new Row().children(
                        new Column().children(
                                new Text("Realiza já seu login na Plics SW", new TextProps().color("white").fontSize(14)),
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
                                        new Image("/assets/qrcode_suporte.jpg", new ImageProps().size(140)),
                                        new Text("Plics - SW", new TextProps().color("white").bold())
                                ),
                                new SpacerVertical(10),
                                new TextFlow(new Text("Scaneie o QRCode para ir para o suporte no WhatsApp.",
                                        new TextProps().textColor("#fff").fontSize(13)))
                        )
                ),
                new Column(new ColumnProps().fillHeight()),
               Components.imageWithTextRow("/assets/whatsapp.png","(96) 99167-8306 - Suporte garantido." )
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

        Components.ShowPopup(router, "Login efetuado com sucesso!");
            try {
                var prefs = new PreferenciasRepository().listar();
                if (!prefs.isEmpty()) {
                    var pref = prefs.getFirst();
                    pref.primeiroAcesso = 0;
                    pref.credenciaisHabilitadas = 1;
                    new PreferenciasRepository().atualizar(pref);
                    router.navigateTo("home");
                }
            } catch (Exception e) {
                e.printStackTrace();
                UI.runOnUi(()->   Components.ShowAlertError("Erro ao entrar: " + e.getMessage()));
            }

    }
}
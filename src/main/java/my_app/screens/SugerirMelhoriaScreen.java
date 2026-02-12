package my_app.screens;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import my_app.screens.components.Components;
import my_app.services.TelegramNotifier;

public class SugerirMelhoriaScreen implements ScreenComponent {
    private final Router router;

    State<Boolean> isSending = State.of(false);
    ComputedState<String> btnText = ComputedState.of(()-> getBtnText(isSending.get()), isSending);

    State<String> content = State.of("");

    String getBtnText(Boolean s){
        return s.equals(true)? "Enviando":"Enviar";
    }

    public SugerirMelhoriaScreen(Router router) {
        this.router = router;
    }

    public Component render() {

        Runnable handleSend = ()->{
            if(isSending.get() || content.isNull() || content.get().trim().isEmpty())return;
            isSending.set(true);

            Async.Run(()->{
                try{
                    TelegramNotifier.enviarMensagemParaTelegram(content.get());
                    UI.runOnUi(()->{
                        Components.ShowPopup(router, "Enviado com sucesso");
                        content.set("");
                    });
                } catch (Exception e) {
                    UI.runOnUi(()-> Components.ShowAlertError("Erro ao enviar: " + e.getMessage()));
                }finally {
                    UI.runOnUi(()-> isSending.set(false));
                }
            });
        };

        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally()))
                        .c_child(Components.TextAreaColumn("Diga abaixo sua sugestão de melhoria ou de funcionalidade", content,"", 300))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.ButtonCadastro(btnText, handleSend)));
    }

}
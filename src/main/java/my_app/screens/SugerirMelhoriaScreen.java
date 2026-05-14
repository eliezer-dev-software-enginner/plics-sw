package my_app.screens;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.screens.components.Components;
//import javafx.scene.control.*;
import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;

import my_app.services.TelegramNotifier;

public class SugerirMelhoriaScreen implements ScreenComponent {
    private final ScreenContext ctx;

    State<Boolean> isSending = State.of(false);
    ComputedState<String> btnText = ComputedState.of(()-> getBtnText(isSending.get()), isSending);

    State<String> content = State.of("");

    String getBtnText(Boolean s){
        return s.equals(true)? "Enviando":"Enviar";
    }

    public SugerirMelhoriaScreen(ScreenContext ctx) {
        this.ctx = ctx;
    }

    public Component render() {

        Runnable handleSend = ()->{
            if(isSending.get() || content.isNull() || content.get().trim().isEmpty())return;
            isSending.set(true);

            Async.Run(()->{
                try{
                    TelegramNotifier.enviarMensagemParaTelegram(content.get());
                    UI.runOnUi(()->{
                        Components.ShowPopup(ctx, "Enviado com sucesso");
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
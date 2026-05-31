package my_app.screens;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import my_app.domain.components.Components;
import my_app.domain.telegram.TelegramNotifierFactory;

public class FeedbackViewModel {

    public final State<Boolean> isSending = new State<>(false);
    public final State<String> content = new State<>("");
    public final ComputedState<String> btnText;

    public FeedbackViewModel() {
        btnText = ComputedState.of(() -> isSending.get() ? "Enviando" : "Enviar", isSending);
    }

    public void send(Runnable onSuccess) {
        if (isSending.get() || content.get() == null || content.get().trim().isEmpty()) return;
        isSending.set(true);

        Async.Run(() -> {
            try {
                TelegramNotifierFactory.create().enviarMensagem(content.get());
                UI.runOnUi(() -> {
                    onSuccess.run();
                    content.set("");
                });
            } catch (Exception e) {
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao enviar: " + e.getMessage()));
            } finally {
                UI.runOnUi(() -> isSending.set(false));
            }
        });
    }
}

package my_app.screens.feedbackScreen;

import megalodonte.ComputedState;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import my_app.domain.components.Components;
import my_app.domain.telegram.TelegramNotifierFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedbackViewModel {
    private static final Logger log = LoggerFactory.getLogger(FeedbackViewModel.class);

    public final State<Boolean> isSending = new State<>(false);
    public final State<String> content = new State<>("");
    public final ComputedState<String> btnText;

    public FeedbackViewModel() {
        btnText = ComputedState.of(() -> isSending.get() ? "Enviando" : "Enviar", isSending);
    }

    public void send(Runnable onSuccess) {
        if (isSending.get() || content.get() == null || content.get().trim().isEmpty()) return;

        if (content.get().trim().length() > 300) {
            Components.ShowAlertError("Erro ao enviar, texto muito longo. Seu texto deve possuir no máximo 300 caracteres!");
            return;
        };
        isSending.set(true);

        Async.Run(() -> {
            try {
                TelegramNotifierFactory.create().enviarMensagem(content.get());
                UI.runOnUi(() -> {
                    onSuccess.run();
                    content.set("");
                });
            } catch (Exception e) {
                log.error("Erro ao enviar feedback via Telegram", e);
                UI.runOnUi(() -> Components.ShowAlertError("Erro ao enviar: " + e.getMessage()));
            } finally {
                UI.runOnUi(() -> isSending.set(false));
            }
        });
    }

    public void onDestroy(){

    }
}

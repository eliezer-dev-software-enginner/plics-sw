package my_app.screens.feedbackScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.components.Components;
import megalodonte.components.layout_components.Row;
import megalodonte.props.RowProps;

public class SugerirMelhoriaScreen implements ScreenComponent {
    private final ScreenContextInterface ctx;
    private final FeedbackViewModel vm;

    public SugerirMelhoriaScreen(ScreenContextInterface ctx) {
        this.ctx = ctx;
        this.vm = new FeedbackViewModel();
    }

    public Component render() {
        return new Card(
                new Column(new ColumnProps().paddingAll(20))
                        .c_child(new Row(new RowProps().centerHorizontally()))
                        .c_child(Components.TextAreaColumn("Diga abaixo sua sugestão de melhoria ou de funcionalidade", vm.content, "Descreva sua ideia com o máximo de detalhes possível", 300))
                        .c_child(new SpacerVertical(20))
                        .c_child(Components.ButtonCadastro(vm.btnText, () -> vm.send(() ->
                                Components.ShowPopup(ctx, "Enviado com sucesso")))));
    }
}

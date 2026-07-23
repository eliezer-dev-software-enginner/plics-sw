package my_app.screens.infoUpdateScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.components.Components;
import my_app.screens.infoUpdateScreen.InfoUpdateScreenViewModel.NotaAtualizacao;

public class InfoUpdateScreen implements ScreenComponent {
    private final InfoUpdateScreenViewModel vm;

    public InfoUpdateScreen(ScreenContext ctx) {
        this.vm = new InfoUpdateScreenViewModel();
    }

    public Component render() {
        return new Scroll(new Container(new ContainerProps().paddingAll(10)).children(
                new Card(new Column().items(vm.getNotas(), InfoUpdateScreen::notaColumn))
        ));
    }

    private static Component notaColumn(NotaAtualizacao nota) {
        return new Column()
                .c_child(Components.FormTitle(nota.version()))
                .c_child(new LineHorizontal())
                .c_child(new SpacerHorizontal(10))
                .items(nota.notes(), Text::new);
    }
}

package my_app.screens.infoUpdateScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.components.Components;

public class InfoUpdateScreen implements ScreenComponent {
    private final InfoUpdateScreenViewModel vm;

    public InfoUpdateScreen(ScreenContext ctx) {
        this.vm = new InfoUpdateScreenViewModel();
    }

    public Component render() {
        var container = new Container();

        vm.getNotas().forEach(it -> {
            var columnInsideCard = new Column();
            columnInsideCard.c_child(Components.FormTitle(it.version()));
            columnInsideCard.c_child(new LineHorizontal());
            columnInsideCard.c_child(new SpacerHorizontal(10));

            for (String note : it.notes()) {
                columnInsideCard.c_child(new Text(note));
            }
            container.c_child(columnInsideCard);
        });

        return new Scroll(new Container(new ContainerProps().paddingAll(10)).children(
                new Card(container)
        ));
    }
}

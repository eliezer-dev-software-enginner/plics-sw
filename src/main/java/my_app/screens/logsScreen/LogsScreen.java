package my_app.screens.logsScreen;

import disgust.io.ButtonsPack;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SpacerHorizontal;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.base.route.v2.ScreenContextInterface;

public class LogsScreen implements ScreenComponent {
    private final LogsScreenViewModel vm;

    public LogsScreen(ScreenContextInterface ctx) {
        this.vm = new LogsScreenViewModel();
    }

    @Override
    public Component render() {
        var textoLogs = new TextAreaInput(vm.conteudoLogs,
                new InputProps()
                        .fontSize(ThemeManager.theme().typography().small())
                        .fontFamily("'Courier New', monospace")
                        .editable(false)
                        .fillHeight());

        return new Container(new ContainerProps().paddingAll(20).fillHeight())
                .children(
                        new Column(new ColumnProps().fillWidth().fillHeight().spacingOf(10))
                                .children(
                                        new Text("Logs da aplicação", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())),
                                        new Row(new RowProps().fillWidth().spacingOf(10))
                                                .children(
                                                        ButtonsPack.ContainedButton("Atualizar", ButtonVariant.PRIMARY, vm::carregarLogs),
                                                        ButtonsPack.OutlinedButton("Abrir pasta de logs", ButtonVariant.PRIMARY, vm::abrirPastaDeLogs),
                                                        new SpacerHorizontal().fill()
                                                ),
                                        new SpacerVertical(5),
                                        textoLogs
                                )
                );
    }
}

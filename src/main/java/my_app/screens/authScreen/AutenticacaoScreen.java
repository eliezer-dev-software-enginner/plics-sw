package my_app.screens.authScreen;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.CardProps;
import megalodonte.props.TextProps;
import megalodonte.props.TextTone;
import megalodonte.props.TextVariant;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;

public class AutenticacaoScreen {
    private final Router router;

    State<String> licensa = new State<>("");
    State<String> login = new State<>("");
    State<String> senha = new State<>("");
    public AutenticacaoScreen(Router router) {
        this.router = router;
    }

    private Theme theme = ThemeManager.theme();

    public Component render (){
        IO.println("[bg] "+theme.colors().background());
        return new Column(new ColumnProps().centerHorizontally().paddingAll(20), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(
                        new Card(new Column(
                                new ColumnProps().spacingOf(10).paddingAll(20), new ColumnStyler().bgColor(theme
                                .colors().surface()))
                                .c_child(new Text("BR Nation", new TextProps().tone(TextTone.PRIMARY).variant(TextVariant.TITLE)))
                                .c_child(new Text("Bem vindo ao BR Nation, mais que um gerenciador de estoque",
                                        new TextProps().fontSize(20).tone(TextTone.PRIMARY).variant(TextVariant.SUBTITLE)))//"#94a3b8"
                                .c_child(new SpacerVertical(10))
                                .c_child(columnImponent("Chave de Licença", licensa, "XXXX-XXXX-XXXX-XXXX"))
                                .c_child(columnImponent("Usuário / Login", login, "Seu usuário"))
                                .c_child(columnImponent("Senha", senha,"••••••••"))
                                .c_child(new Button("Entrar no Sistema",
                                        new ButtonProps().fillWidth().height(45).bgColor("#2563eb")
                                                .fontSize(20).textColor("white"))),
                                new CardProps().padding(0)
                        )
                );
    }

    /*
    Cor das bordas do input:

Normal: #334155 (definido pela classe border-slate-700).

Ao focar (clicar): #3b82f6 (azul, definido pela classe focus:ring-blue-500).

Cor do placeholder:

Hexadecimal: #64748b (definido pela classe placeholder-slate-500).

Cor do fundo do input:

Hexadecimal: #1e293b (definido pela classe bg-slate-800).
     */

    Component columnImponent(String label, State<String> inputState, String placeholder){
        return new Column(new ColumnProps().spacingOf(5))
                .c_child(new Text(label, new TextProps().fontSize(20), new TextStyler().color("#cbd5e1")))
                .c_child(new Input(inputState, new InputProps().height(45)
                        .fontSize(18)
                        .placeHolder(placeholder)));
    }
}

package my_app.screens.DetailScreen;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.CardProps;
import megalodonte.router.Router;
import megalodonte.styles.CardStyler;

public class AutenticacaoScreen {
    Router router;

    State<String> licensa = new State<>("");
    State<String> login = new State<>("");
    State<String> senha = new State<>("");
    public AutenticacaoScreen(Router router) {
        this.router = router;
    }

    public Component render (){
        return new Column(new ColumnProps().centerHorizontally().paddingAll(20), new ColumnStyler().bgColor("#1e293b"))
                .c_child(
                        new Card(new Column(
                                new ColumnProps().spacingOf(10).paddingAll(20), new ColumnStyler().bgColor("#1a2235"))
                                .c_child(new Text("BR Nation", new TextProps().fontSize(35).color("white").bold()))
                                .c_child(new Text("Bem vindo ao BR Nation, mais que um gerenciador de estoque",
                                        new TextProps().color("#94a3b8").fontSize(20)))
                                .c_child(new SpacerVertical(10))
                                .c_child(columnImponent("Chave de Licença", licensa, "XXXX-XXXX-XXXX-XXXX"))
                                .c_child(columnImponent("Usuário / Login", licensa, "Seu usuário"))
                                .c_child(columnImponent("Senha", licensa,"••••••••"))
                                .c_child(new Button("Entrar no Sistema",
                                        new ButtonProps().fillWidth().height(45).bgColor("#2563eb")
                                                .fontSize(20).textColor("white"))),
                                new CardProps().padding(0)
                        )
                );
    }

    Component columnImponent(String label, State<String> inputState, String placeholder){
        return new Column(new ColumnProps().spacingOf(5))
                .c_child(new Text(label, new TextProps().fontSize(20).color("#cbd5e1")))
                .c_child(new Input(inputState, new InputProps().height(45)
                        .fontSize(18)
                        .placeHolder(placeholder)));
    }
}

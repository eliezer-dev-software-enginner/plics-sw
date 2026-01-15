package my_app.screens.components;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.*;
import megalodonte.styles.ColumnStyler;
import megalodonte.styles.DatePickerStyler;
import megalodonte.styles.InputStyler;
import megalodonte.styles.TextStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class Components {

    static Theme theme = ThemeManager.theme();

    private static final NumberFormat BRL =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));



    //TODO: adicionar campo editavel:false
    public static Component DatePickerColumn(State<LocalDate> localDateState, String label, String placeholder) {

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new DatePicker(localDateState,
                                new DatePickerProps().fontSize(theme.typography().small()).height(35)
                                        .placeHolder(placeholder)
                                        .locale(new Locale("pt", "BR"))
                                        .pattern("dd/MM/yyyy")
                                        .editable(false)
                                        .placeHolder("dd/MM/yyyy"),
                                new DatePickerStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        )
                );
    }

    public static Column ImageSelector(String title,State<String> imageState,
                                       ImageProps props,
                                       Runnable callback){
        return new Column()
                .c_child(new Image(imageState, props))
                .c_child(new SpacerVertical(10))
                .c_child(ButtonCadastro(title, callback));
    }

    public static Component FormTitle(String title){
        return new Text(title, new TextProps().bold().variant(TextVariant.BODY));
    }

    public static Component ButtonCadastro(String textState, Runnable handleAdd){
        return new Button(textState, new ButtonProps().fillWidth().height(35).bgColor("#2563eb")
                .fontSize(theme.typography().small())
                .textColor("white")
                .onClick(handleAdd));
    }

    public static Component ButtonCadastro(State<String> textState, Runnable handleAdd){
        return new Button(textState, new ButtonProps().fillWidth().height(35).bgColor("#2563eb")
                .fontSize(theme.typography().small())
                .textColor("white")
                .onClick(handleAdd));
    }



    public static <T> Component SelectColumn(String label, List<T> list, State<T> stateSelected, Function<T, String> display){
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new Select<T>(new SelectProps().height(40))
                        .items(list)
                        .value(stateSelected)
                        .displayText(display)
                );
    }

    public static Component TextWithValue(String label, ReadableState<String> valueState) {
        return new Row()
                .r_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .r_child(new Text(valueState, new TextProps().fontSize(theme.typography().small())));
    }

    //rawState é só pra visualização formatada
    public static Component InputColumnCurrency(String label, State<String> inputState, State<String> rawState){
        var icon =  Entypo.CREDIT;
        var fonticon = FontIcon.of(icon, 15, Color.web("green"));

        var inputProps = new InputProps().fontSize(theme.typography().small()).height(35)
                .placeHolder("R$ 0,00");

        var inputStyler = new InputStyler().
                borderWidth(theme.border().width())
                .borderColor(theme.colors().primary());

        // Atualiza o state com o valor bruto (em centavos)
        var input = new Input(inputState, inputProps, inputStyler)
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");
                    if (numeric.isEmpty()) {
                        numeric = "0";
                        rawState.set("0");
                        return "R$ 0,00";
                    }

                    // Atualiza o state com o valor bruto (em centavos)
                    rawState.set(numeric);

                    BigDecimal raw = new BigDecimal(numeric).movePointLeft(2);
                    return BRL.format(raw);
                })
                .left(fonticon);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(input);
    }

    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new Input((State<String>) inputState,
                        new InputProps().fontSize(theme.typography().small()).height(35)
                                .placeHolder(placeholder),
                        new InputStyler().
                                borderWidth(theme.border().width())
                                .borderColor(theme.colors().primary())
                        )
                );
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new TextAreaInput(inputState,
                                new InputProps().fontSize(theme.typography().small()).height(35)
                                        .placeHolder(placeholder)
                                        .height(100),
                                new InputStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        )
                );
    }
    
    public static Component errorText(String message){
        return new Column(new ColumnProps(), new ColumnStyler().bgColor("white")).c_child(new SpacerVertical(5))
                .c_child(new Text(message, new TextProps().variant(TextVariant.SUBTITLE), new TextStyler().color("red")));
    }

    public static Row commonCustomMenus(Runnable onClickNew ,Runnable onEdit, Runnable onDelete){
        return new Row(new RowProps().spacingOf(20))
                .r_child(MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", () -> executar(onClickNew::run)))
                .r_child(MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(onEdit::run)))
                .r_child(MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(onDelete::run)))
                .r_child(new SpacerHorizontal().fill())
                //.r_child(MenuItem("Sair", Entypo.REPLY, "red", () -> router.closeSpawn("cad-produtos/"+id)));
                ;
    }

    public static Component MenuItem(String title, Ikon ikon, String color, Runnable onClick){

        var icon = Component.CreateFromJavaFxNode(FontIcon.of(ikon, 25, Color.web(color)));

        return new Clickable(new Card(
                new Column(new ColumnProps().centerHorizontally())
                        .c_child(icon)
                        .c_child(new SpacerVertical(6))
                        .c_child(new Text(title, new TextProps().variant(TextVariant.SMALL)))
        ), onClick
        );
    }


    public static Component searchInput(State<String> stateInput, String placeholder){
        var icon = FontIcon.of(AntDesignIconsOutlined.SEARCH, 20, Color.web(theme.colors().secondary()));
        return new Input(stateInput,
                new InputProps().placeHolder(placeholder)
                        .width(300)
                        .height(35))
                .left(icon);
    }

    /**
     * Exemplo de uso do componente Clickable com efeito TouchableOpacity.
     */
    public static Component clickableButton(String text, Runnable onClick) {
        return new Clickable(
            new Text(text, new TextProps().variant(TextVariant.BODY)),
            onClick,
            new ClickableProps()
                .padding(12)
                .borderRadius(8)
                .backgroundColor(theme.colors().primary())
                .hoverColor("rgba(0,0,0,0.05)")
                .activeColor("rgba(0,0,0,0.1)")
        );
    }

    /**
     * Exemplo de Clickable com ícone e texto.
     */
    public static Component clickableButtonWithIcon(String text, Ikon icon, String color, Runnable onClick) {
        var iconNode = Component.CreateFromJavaFxNode(FontIcon.of(icon, 16, Color.web(color)));
        
        return new Clickable(
            new Row(new RowProps().spacingOf(8))
                .r_child(iconNode)
                .r_child(new Text(text, new TextProps().variant(TextVariant.BODY))),
            onClick,
            new ClickableProps()
                .padding(10)
                .borderRadius(6)
                .backgroundColor("white")
                .hoverColor("rgba(0,0,0,0.03)")
                .activeColor("rgba(0,0,0,0.06)")
        );
    }

    /**
     * Exemplo de formulário com larguras controladas.
     */
    public static Component formRowWithConstraints(String labelText, Component inputComponent) {
        return new Row(new RowProps()
            .spacingOf(10)
            .width(400)
            .paddingAll(5))
            .r_child(new Text(labelText, new TextProps().variant(TextVariant.BODY)))
            .r_child(inputComponent);
    }

    /**
     * Exemplo de coluna com largura e espaçamento controlados.
     */
    public static Component columnWithWidthConstraints() {
        return new Column(new ColumnProps()
            .width(300)
            .maxWidth(500)
            .spacingOf(15)
            .paddingAll(20))
            .c_child(new Text("Título da Coluna", new TextProps().variant(TextVariant.SUBTITLE)))
            .c_child(new SpacerVertical(10))
            .c_child(new Text("Conteúdo da coluna com largura fixa."))
            .c_child(clickableButton("Ação Principal", () -> IO.println("Botão principal clicado")));
    }

    /**
     * Exemplo de alinhamento horizontal centralizado.
     */
    public static Component centeredRow() {
        return new Row(new RowProps()
            .width(400)
            .spacingOf(15)
            .centerHorizontally()
            .paddingAll(10))
            .r_child(new Text("Item 1"))
            .r_child(new Text("Item 2"))
            .r_child(new Text("Item 3"));
    }

    /**
     * Exemplo de alinhamento vertical centralizado.
     */
    public static Component centeredColumn() {
        return new Column(new ColumnProps()
            .width(300)
            .spacingOf(20)
            .centerVertically()
            .paddingAll(15))
            .c_child(new Text("Topo"))
            .c_child(new Text("Meio"))
            .c_child(new Text("Fundo"));
    }

    /**
     * Exemplo de card com alinhamento controlado.
     */
    public static Component alignedCard() {
        return new Card(new Column(new ColumnProps()
            .width(350)
            .spacingOf(10))
            .c_child(new Row(new RowProps()
                .width(320)
                .centerHorizontally())
                .r_child(new Text("Título Alinhado", new TextProps().variant(TextVariant.SUBTITLE))))
            .c_child(new Row(new RowProps()
                .width(320)
                .centerHorizontally())
                .r_child(new Button("Botão Centralizado", new ButtonProps().primary().onClick(() -> IO.println("Botão centralizado clicado"))))));
    }


    /**
     * Exemplo de botões com variantes de tema.
     */
    public static Component themeButtons() {
        return new Column(new ColumnProps()
            .spacingOf(15)
            .width(400))
            .c_child(new Text("Botões com Temas", new TextProps().variant(TextVariant.TITLE)))
            .c_child(new Row(new RowProps()
                .spacingOf(10))
                .r_child(new Button("Primary", new ButtonProps().primary().height(45).onClick(() -> IO.println("Primary clicked"))))
                .r_child(new Button("Secondary", new ButtonProps().secondary().height(45).onClick(() -> IO.println("Secondary clicked"))))
                .r_child(new Button("Success", new ButtonProps().success().height(45).onClick(() -> IO.println("Success clicked")))))
            .c_child(new Button("Warning", new ButtonProps().warning().height(45).onClick(() -> IO.println("Warning clicked"))));
    }



@FunctionalInterface
interface Action {
    void run() throws Exception;
}


private static void executar(Action action) {
        try {
            action.run();
            IO.println("Operation completed successfully");
        } catch (Exception e) {
            IO.println("Error: " + e.getMessage());
        }
    }
}
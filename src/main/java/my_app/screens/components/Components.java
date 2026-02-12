package my_app.screens.components;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.DatePicker;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.components.inputs.OnChangeResult;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.styles.*;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import megalodonte.utils.related.TextVariant;
import my_app.db.models.ClienteModel;
import my_app.domain.Parcela;
import my_app.screens.ComprasScreen;
import my_app.utils.DateUtils;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import static my_app.utils.Utils.formatPhone;

public class Components {

    static Theme theme = ThemeManager.theme();

    public static Row TextWithDetails(String label, Object value) {
        return new Row()
                .r_child(new Text(label, new TextProps().fontSize(theme.typography().body()).bold()))
                .r_child(new Text(value.toString(), new TextProps().fontSize(theme.typography().body())));
    }

    public static Component aPrazoForm(
            State<List<Parcela>> parcelas,
            ComputedState<Boolean> tipoPagamentoSelectedIsAPrazo,
            ComputedState<String> totalLiquido) {
        var dtPrimeiraParcela = State.of(LocalDate.now().plusMonths(1).minusDays(1));
        var qtdParcelas = State.of("1");

        Runnable handleGerarParcelas = () -> {
            var list = Parcela.gerarParcelas(dtPrimeiraParcela.get(), Integer.parseInt(qtdParcelas.get()), Double.parseDouble(totalLiquido.get()));
            parcelas.set(list);
        };

        ForEachState<Parcela, Component> parcelaComponentForEachState = ForEachState.of(parcelas, Components::parcelaItem);

        return Show.when(tipoPagamentoSelectedIsAPrazo,
                () -> new Column(new ColumnProps())
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.DatePickerColumn(dtPrimeiraParcela, "Data primeira parcela", ""))
                                        .r_child(Components.InputColumn("Quantidade de parcelas", qtdParcelas, "Ex: 1"))
                                        .r_child(Components.ButtonCadastro("Gerar parcelas", handleGerarParcelas)))
                        .items(parcelaComponentForEachState)
        );
    }

    public static Component parcelaItem(Parcela parcela) {
        return new Row(new RowProps())
                .r_child(Components.TextColumn("PARCELA", String.valueOf(parcela.numero())))
                .r_child(Components.TextColumn("VENCIMENTO", DateUtils.millisToBrazilianDateTime(parcela.dataVencimento())))
                .r_child(Components.TextColumn("VALOR", String.format("R$ %.2f", parcela.valor())));
    }

    public static Component actionButtons(ComputedState<String> btnText, Runnable onClick, Runnable onClearForm) {
        return new Row(new RowProps().spacingOf(10))
                .r_child(new Button(btnText,
                        new ButtonProps()
                                .fillWidth()
                                .height(35)
                                .bgColor("#10b981")
                                .textColor("white")
                                .fontSize(16)
                                .onClick(onClick)))
                .r_child(new Button("Limpar",
                        new ButtonProps()
                                .fillWidth()
                                .height(35)
                                .bgColor("#6b7280")
                                .textColor("white")
                                .fontSize(16)
                                .onClick(onClearForm))
                );
    }

    public static Component ScrollPaneDefault(Component child){
        var scroll = new ScrollPane();
        scroll.setContent(child.getJavaFxNode());
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;-fx-border-color: transparent;");

        return Component.CreateFromJavaFxNode(scroll);
    }

    public static void ShowPopup(Router router, String message) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.setStyle("""
                    -fx-background-color: #333;
                    -fx-text-fill: white;
                    -fx-padding: 10 16;
                    -fx-background-radius: 6;
                """);

        popup.getContent().add(label);
        popup.setAutoHide(true);
        popup.show(router.getCurrentActiveStage());
    }

    public static void ShowModal(Component ui, Router router){
        Stage stage = new Stage();
        stage.setScene(new Scene((Parent) ui.getJavaFxNode(), 700, 500));
        stage.setTitle("Detalhes");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(router.getCurrentActiveStage());
        stage.show();
    }

    public static void ShowAlertAdvice(String bodyMessage, Runnable handleSuccessEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(bodyMessage);
        alert.setContentText("Essa ação não poderá ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleSuccessEvent.run();
        }
    }

    public static Card CardImageSelector(State<String> imagemState, Runnable handleChangeImage) {

        return new Card(
                new Column(new ColumnProps().centerHorizontally().spacingOf(15))
                        .c_child(new Text("Foto do produto", new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new Image(imagemState, new ImageProps().size(120)))
                        .c_child(new SpacerVertical().fill())
                        .c_child(new Button("Inserir imagem",
                                new ButtonProps().fontSize(theme.typography().small()).bgColor("#A6B1E1")
                                        .onClick(handleChangeImage))),
                new CardProps().height(300).padding(20)
        );
    }

    public static void ShowAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Erro");

        ButtonType okButton = new ButtonType("Fechar", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().add(okButton);
        alert.setOnCloseRequest(event -> alert.close());
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Component DatePickerColumn(State<LocalDate> localDateState, String label) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new DatePicker(localDateState,
                                new DatePickerProps().fontSize(theme.typography().small()).height(35)
                                        .placeHolder("dd/mm/yyyy")
                                        .locale(new Locale("pt", "BR"))
                                        .pattern("dd/MM/yyyy")
                                        .width(140)
                                        .editable(false),
                                new DatePickerStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        )
                );
    }
    //TODO: adicionar campo editavel:false
    @Deprecated
    public static Component DatePickerColumn(State<LocalDate> localDateState, String label, String placeholder) {

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new DatePicker(localDateState,
                                new DatePickerProps().fontSize(theme.typography().small()).height(35)
                                        .placeHolder(placeholder)
                                        .locale(new Locale("pt", "BR"))
                                        .pattern("dd/MM/yyyy")
                                        .editable(false)
                                        .width(140)
                                        .placeHolder("dd/MM/yyyy"),
                                new DatePickerStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        )
                );
    }

    public static Column ImageSelector(String title, State<String> imageState,
                                       ImageProps props,
                                       Runnable callback) {
        return new Column()
                .c_child(new Image(imageState, props))
                .c_child(new SpacerVertical(10))
                .c_child(ButtonCadastro(title, callback));
    }

    public static Component FormTitle(String title) {
        return new Text(title, new TextProps().bold().variant(TextVariant.BODY));
    }

    public static Component ButtonCadastro(String textState, Runnable handleAdd) {
        return new Button(textState, new ButtonProps().fillWidth().height(35).bgColor("#2563eb")
                .fontSize(theme.typography().small())
                .textColor("white")
                .onClick(handleAdd));
    }

    public static Component ButtonCadastro(ComputedState<String> textState, Runnable handleAdd) {
        return new Button(textState, new ButtonProps().fillWidth().height(35).bgColor("#2563eb")
                .fontSize(theme.typography().small())
                .textColor("white")
                .onClick(handleAdd));
    }

    @Deprecated(forRemoval = true)
    public static Component ButtonCadastro(State<String> textState, Runnable handleAdd) {
        return new Button(textState, new ButtonProps().fillWidth().height(35).bgColor("#2563eb")
                .fontSize(theme.typography().small())
                .textColor("white")
                .onClick(handleAdd));
    }

    private final static SelectProps selectProps = new SelectProps()
            .minWidth(100)
            .height(35);

    public static <T> Component SelectColumn(String label, State<List<T>> listState, State<T> stateSelected, Function<T, String> display) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new Select<T>(selectProps)
                        .items(listState)
                        .value(stateSelected)
                        .displayText(display)
                );
    }

    public static <T> Component SelectColumn(String label, List<T> list, State<T> stateSelected, Function<T, String> display) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new Select<T>(selectProps)
                        .items(list)
                        .value(stateSelected)
                        .displayText(display)
                );
    }

    public static <T> Component SelectColumn(String label, State<List<T>> list, State<T> stateSelected, Function<T, String> display, boolean compareById) {
        var select = new Select<T>(selectProps)
                .items(list)
                .value(stateSelected)
                .displayText(display);

        if (compareById) {
            select.compareById();
        }

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(select);
    }

    public static <T> Component SelectColumn(String label, List<T> list, State<T> stateSelected, Function<T, String> display, boolean compareById) {
        var select = new Select<T>(selectProps)
                .items(list)
                .value(stateSelected)
                .displayText(display);
        
        if (compareById) {
            select.compareById();
        }
        
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(select);
    }

    public static <T> Component SelectColumn(String label, ListState<T> list, State<T> stateSelected,Function<T, String> display, boolean compareById) {
        var select = new Select<T>(selectProps)
                .items(list)
                .value(stateSelected)
                .displayText(display);

        if (compareById) {
            select.compareById();
        }

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(select);
    }

    public static <T> Component SelectColumnWithButton(
            String label, ListState<T> list, State<T> stateSelected,
            Function<T, String> display, boolean compareById,
            String btnText, Runnable handleClick) {

        var rowProps = new RowProps().spacingOf(2)
                .bottomVertically();

        return new Row(rowProps)
                        .r_child(Components.SelectColumn(  label,  list,  stateSelected, display,compareById))
                        .r_child(new Button(btnText, new ButtonProps().height(35)
                                .textColor("#FFF")
                                .marginBottom(2)
                                .onClick(handleClick))
                        );
    }

    public static Column TextColumn(String label, String value) {
        return new Column(new ColumnProps(), new ColumnStyler()
                .borderColor(theme.colors().primary())
                .borderWidth(theme.border().width()))
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().body()).bold()))
                .c_child(new Text(value, new TextProps().fontSize(theme.typography().body())));
    }

    public static Component TextWithValue(String label, ReadableState<String> valueState) {
        return new Row()
                .r_child(new Text(label, new TextProps().fontSize(theme.typography().body()).bold()))
                .r_child(new Text(valueState, new TextProps().fontSize(theme.typography().body())));
    }

    public static Component InputColumnPhone(String label, State<String> inputState) {
        var inputProps = new InputProps().fontSize(theme.typography().small())
                .height(35).placeHolder("(00) 00000-0000");

        var inputStyler = new InputStyler()
                .borderWidth(theme.border().width())
                .borderColor(theme.colors().primary());

        var input = new Input(inputState, inputProps, inputStyler)
                .onInitialize(value -> {
                    String formatted = formatPhone(value);
                    return OnChangeResult.of(formatted, value);
                })
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");

                    // Limita a 11 dígitos (padrão BR com DDD)
                    if (numeric.length() > 11) {
                        numeric = numeric.substring(0, 11);
                    }

                    String formatted = formatPhone(numeric);
                    return OnChangeResult.of(formatted, numeric);
                })
                .lockCursorToEnd();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(input);
    }



    public static Component InputColumnNumeric(String label, State<String> inputState,  String placeholder) {
        var inputProps = new InputProps().fontSize(theme.typography().small())
                .height(35).placeHolder(placeholder);

        var inputStyler = new InputStyler().
                borderWidth(theme.border().width())
                .borderColor(theme.colors().primary());

        var input = new Input(inputState, inputProps, inputStyler)
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");
                    if (numeric.isEmpty()) {
                        return OnChangeResult.of("", "");
                    }
                    return OnChangeResult.of(numeric, numeric);
                })
                .lockCursorToEnd();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(input);
    }

    private static final NumberFormat BRL =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    public static Component InputColumnCurrency(String label, State<String> inputState) {
        var icon = Entypo.CREDIT;
        var fonticon = FontIcon.of(icon, 15, Color.web("green"));

        var inputProps = new InputProps().fontSize(theme.typography().small()).height(35)
                .width(140)
                .placeHolder("R$ 0,00");

        var inputStyler = new InputStyler().
                borderWidth(theme.border().width())
                .borderColor(theme.colors().primary());

        // inputState armazena valores brutos (em centavos), campo exibe formato BRL
        var input = new Input(inputState, inputProps, inputStyler)
                .onInitialize(value -> {
                    if (value.matches("\\d+")) {
                        BigDecimal realValue = new BigDecimal(value).movePointLeft(2);
                        return OnChangeResult.of(BRL.format(realValue), value);
                    }
                    return OnChangeResult.of(value, value);
                })
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");
                    if (numeric.isEmpty()) {
                        return OnChangeResult.of("R$ 0,00", "0");
                    }

                    // Converte centavos para BigDecimal do valor real
                    BigDecimal realValue = new BigDecimal(numeric).movePointLeft(2);
                    return OnChangeResult.of(BRL.format(realValue), numeric);
                })
                .lockCursorToEnd()
                .left(fonticon);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(input);
    }

    public static Component InputColumnComFocusHandler(String label, ReadableState<String> inputState, String placeholder, Runnable focusChangeHandler) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new Input((State<String>) inputState,
                                new InputProps().fontSize(theme.typography().small()).height(35)
                                        .placeHolder(placeholder),
                                new InputStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        ).onChangeFocus(focus -> {
                            if (!focus) focusChangeHandler.run();
                        })
                );
    }

    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder, boolean disableInput) {
        InputProps props = new InputProps().fontSize(theme.typography().small()).height(35)
                .placeHolder(placeholder);

        if(disableInput) props.disable();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new Input((State<String>) inputState,
                        props,
                                new InputStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        )
                );
    }

    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder) {
      return InputColumn(label, inputState, placeholder, false);
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder) {
        return TextAreaColumn(label,inputState,placeholder,100);
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder, int height) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(theme.typography().small())))
                .c_child(new TextAreaInput(inputState,
                                new InputProps().fontSize(theme.typography().small()).height(35)
                                        .placeHolder(placeholder)
                                        .height(height),
                                new InputStyler().
                                        borderWidth(theme.border().width())
                                        .borderColor(theme.colors().primary())
                        )
                );
    }

    public static Component errorText(String message) {
        return new Column(new ColumnProps(), new ColumnStyler().bgColor("white")).c_child(new SpacerVertical(5))
                .c_child(new Text(message, new TextProps().variant(TextVariant.SUBTITLE), new TextStyler().color("red")));
    }


    public static Row commonCustomMenus(Runnable onClickNew, Runnable onEdit, Runnable onDelete, Runnable onClone) {
        return new Row(new RowProps().spacingOf(20))
                .r_child(MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", () -> executar(onClickNew::run)))
                .r_child(MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(onEdit::run)))
                .r_child(MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(onDelete::run)))
                .r_child(MenuItem("Clonar", Entypo.COPY, "black", () -> executar(onClone::run)))
                .r_child(new SpacerHorizontal().fill())
                //.r_child(MenuItem("Sair", Entypo.REPLY, "red", () -> router.closeSpawn("cad-produtos/"+id)));
                ;
    }

    public static Component MenuItem(String title, Ikon ikon, String color, Runnable onClick) {

        var icon = Component.CreateFromJavaFxNode(FontIcon.of(ikon, 25, Color.web(color)));

        return new Clickable(new Card(
                new Column(new ColumnProps().centerHorizontally())
                        .c_child(icon)
                        .c_child(new SpacerVertical(6))
                        .c_child(new Text(title, new TextProps().variant(TextVariant.SMALL)))
        ), onClick
        );
    }

    public static Component searchInput(State<String> stateInput, String placeholder) {
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

    private static void executar(Action action) {
        try {
            action.run();
            IO.println("Operation completed successfully");
        } catch (Exception e) {
            IO.println("Error: " + e.getMessage());
        }
    }




    public enum AlertType {ERRO, SUCESSO}


    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }
}
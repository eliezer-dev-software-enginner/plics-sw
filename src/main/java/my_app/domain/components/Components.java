package my_app.domain.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.*;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.application.ErrorReporter;
import megalodonte.base.Animations;
import megalodonte.base.UI;
import megalodonte.base.async.RunnableThrowing;
import megalodonte.base.components.Component;
import megalodonte.base.state.ReadableState;
import megalodonte.base.state.State;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.DatePicker;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.OnChangeResult;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.ListState;
import megalodonte.v2.Show;
import my_app.db.models.ProdutoModel;
import my_app.domain.Data;
import my_app.domain.Parcela;
import my_app.domain.states.EnderecoState;
import my_app.domain.states.TotaisState;
import my_app.utils.DateUtils;
import my_app.utils.Utils;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import static my_app.utils.Utils.*;

public class Components {


    public record Endereco(String uf, String cep, String cidade, String bairro,String rua, String numero){}
    public static Component ItemDetailEndereco(Endereco endereco){
        return new Container()
                .c_child(Components.TextWithDetails("UF: ", endereco.uf()))
                .c_child(Components.TextWithDetails("CEP: ", Utils.formatCep(endereco.cep())))
                .c_child(Components.TextWithDetails("Cidade: ", endereco.cidade()))
                .c_child(Components.TextWithDetails("Bairro: ", endereco.bairro()))
                .c_child(Components.TextWithDetails("Rua: ", endereco.rua()))
                .c_child(Components.TextWithDetails("Número: ", endereco.numero()));
    }

    public static Component enderecoComponent(EnderecoState enderecoState){
        return new Container().children(
                Components.FormTitle("Endereço"),
                new FlowRow(new FlowRowProps().spacingOf(10))
                        .children(
                                Components.InputColumnCep("Cep", enderecoState.cep),
                                Components.SelectColumn("UF", Data.ufList, enderecoState.ufSelected, it -> it),
                                Components.InputColumn("Cidade", enderecoState.cidade, ""),
                                Components.InputColumn("Bairro", enderecoState.bairro, ""),
                                Components.InputColumn("Rua", enderecoState.rua, ""),
                                Components.InputColumnNumeric("Número", enderecoState.numero, "")
                        )
        );
    }

    public static Component imageWithTextRow(String imgPath, String text) {
        return new Row().children(
                new Image(imgPath, new ImageProps().size(25)),
                new SpacerHorizontal(5),
                new Text(text, new TextProps().color("white").fontSize(14))
        );
    }

    public static Row TextWithDetails(String label, Object value, boolean wrapText) {
        var comp = new Text(value == null ? "" : value.toString(),
                new TextProps().fontSize(ThemeManager.theme().typography().body()));

        var textValueComponent = wrapText ? new TextFlow(comp) : comp;

        return new Row()
                .children(
                        new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()),
                        textValueComponent
                );
    }

    public static Row TextWithDetails(String label, Object value) {
        return TextWithDetails(label, value, false);
    }

    public static Component aPrazoForm(
            State<List<Parcela>> parcelas,
            ComputedState<Boolean> tipoPagamentoSelectedIsAPrazo,
            ComputedState<String> totalLiquido) {
        var dtPrimeiraParcela = State.of(LocalDate.now().plusMonths(1).minusDays(1));
        var qtdParcelas = State.of("1");

        Runnable handleGerarParcelas = () -> {
            int qtd = Integer.parseInt(qtdParcelas.get());
            if(qtd < 1){
                Components.ShowAlertError("Quantidade de parcelas inválida: " + qtd + ". Informe um valor maior que zero.");
                return;
            }
            var list = Parcela.gerarParcelas(dtPrimeiraParcela.get(), qtd, Double.parseDouble(totalLiquido.get()));
            parcelas.set(list);
        };

        ForEachState<Parcela, Component> parcelaComponentForEachState = ForEachState.of(parcelas, Components::parcelaItem);

        return Show.when(tipoPagamentoSelectedIsAPrazo,
                () -> new Column(new ColumnProps())
                        .c_child(
                                new Row(new RowProps().spacingOf(10).bottomVertically())
                                        .r_child(Components.DatePickerColumn(dtPrimeiraParcela, "Data primeira parcela"))
                                        .r_child(Components.InputColumnNumeric("Quantidade de parcelas", qtdParcelas, "Ex: 1"))
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
        return new Button(btnText,
                new ButtonProps()
                        .fillWidth()
                        .height(31)
                        .fontSize(16)
                        .textColor("white").bgColor("#10b981")
        ).onClick(onClick);
    }

    public static Component ScrollPaneDefault(Component child) {
        var scroll = new ScrollPane();
        scroll.setContent(child.getJavaFxNode());
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;-fx-border-color: transparent;");

        return Component.CreateFromJavaFxNode(scroll);
    }

    public static void ShowPopupWithButton(
            ScreenContext screenContext, String message, String btnTitle, Runnable callback) {
        Popup popup = new Popup();
        popup.setAutoHide(false);

        Label label = new Label(message);
        label.setStyle("""
                    -fx-background-color: #333;
                    -fx-text-fill: white;
                    -fx-padding: 10 16;
                    -fx-background-radius: 6;
                """);

        Card card = new Card(new Container()
                .children(
                        Component.CreateFromJavaFxNode(label),
                        new SpacerVertical(15),
                        new Row(new RowProps().spacingOf(10)).children(
                                new Button(btnTitle).onClick(callback),
                                new Button("Fechar", new ButtonProps().bgColor("red")).onClick(()->{
                                    callback.run();
                                    popup.hide();
                                })
                        )
                ));


        popup.getContent().add(card.getJavaFxNode());
        popup.show(screenContext.selfStage());
    }

    public static void ShowPopup(ScreenContext context, String message) {
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
        popup.show(context.selfStage());
    }

    public static void ShowPopupForced(ScreenContext context, String message, String buttonText, Runnable onButtonClick) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(context.selfStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Aviso");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #333; -fx-font-size: 14px; -fx-text-alignment: center;");

        javafx.scene.control.Button btn = new javafx.scene.control.Button(buttonText);
        btn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-size: 14px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btn.setOnAction(e -> {
            stage.close();
            onButtonClick.run();
        });

        VBox vbox = new VBox(20, label, btn);
        vbox.setPadding(new Insets(24));
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox, 420, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void ShowModal(Component ui, ScreenContext context, int height) {
        Stage stage = new Stage();

        Scroll scroll = new Scroll(ui);
        stage.setScene(new Scene((Parent) scroll.getJavaFxNode(), 800, height));
        stage.setTitle("Detalhes");

        Stage owner = context.selfStage();
        stage.initOwner(owner);

        stage.setOnHidden(event -> {
            owner.requestFocus();
            owner.toFront();
        });

        stage.show();
    }

    public static void ShowModal(Component ui, ScreenContext context) {
        ShowModal(ui, context, 500);
    }

    public static void ShowAlertAdvice(String bodyMessage, RunnableThrowing handleSuccessEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(bodyMessage);
        alert.setContentText("Essa ação não poderá ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                handleSuccessEvent.run();
            } catch (Exception e) {
                ErrorReporter.handle(e);
                throw new IllegalStateException(e);
            }
        }
    }

    public static Card CardImageSelector(State<String> imagemState, Runnable handleChangeImage) {
        return new Card(
                new Column(new ColumnProps().centerHorizontally().spacingOf(15))
                        .c_child(new Text("Foto do produto", new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new Image(imagemState, new ImageProps().size(120)))
                        .c_child(new SpacerVertical().fill())
                        .c_child(new Button("Inserir imagem",
                                new ButtonProps().fontSize(ThemeManager.theme().typography().small()).bgColor("#A6B1E1"))
                                .onClick(handleChangeImage)
                        ),
                new CardProps().height(300).padding(20)
        );
    }

    public static void ShowAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Erro");
        alert.initModality(Modality.NONE); // não deixa o Glass tocar na janela owner

        ButtonType okButton = new ButtonType("Fechar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(okButton);
        alert.setContentText(message);
        alert.show();
    }

    public static Component DatePickerColumn(State<LocalDate> localDateState, String label) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(new DatePicker(localDateState,
                                new DatePickerProps().fontSize(ThemeManager.theme().typography().small()).height(31)
                                        .placeHolder("dd/mm/yyyy")
                                        .locale(new Locale("pt", "BR"))
                                        .pattern("dd/MM/yyyy")
                                        .width(140)
                                        .editable(false)
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

    public static Component FormTitle(String title, String textColor) {
        return new Text(title, new TextProps().variant(TextVariant.BODY).bold().textColor(textColor));
    }

    public static Component FormTitle(String title) {
        return new Text(title, new TextProps().variant(TextVariant.BODY).bold());
    }

    static final ButtonProps propsBtnCadastro = new ButtonProps().fillWidth().height(31)
            .fontSize(ThemeManager.theme().typography().small()).textColor("white").bgColor("#2563eb");

    public static Component ButtonCadastro(String textState, Runnable handleAdd) {
        return new Button(textState, propsBtnCadastro
        ).onClick(handleAdd);
    }

    public static Component ButtonCadastro(ComputedState<String> textState, Runnable handleAdd) {
        return new Button(textState, propsBtnCadastro
        ).onClick(handleAdd);
    }

    @Deprecated(forRemoval = true)
    public static Component ButtonCadastro(State<String> textState, Runnable handleAdd) {
        return new Button(textState, propsBtnCadastro).onClick(handleAdd);
    }

    private final static SelectProps selectProps = new SelectProps()
            .minWidth(100)
            .height(31);

    public static <T> Component SelectColumn(String label, State<List<T>> listState, State<T> stateSelected, Function<T, String> display) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(new Select<T>(selectProps)
                        .items(listState)
                        .value(stateSelected)
                        .displayText(display)
                );
    }

    public static <T> Component SelectColumn(String label, List<T> list, State<T> stateSelected, Function<T, String> display) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
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
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(select);
    }

    public static <T> Component SelectColumn(String label, ListState<T> list, State<T> stateSelected, Function<T, String> display,
                                             boolean compareById, ReadableState<Boolean> expandAutomatically) {
        var select = new Select<T>(selectProps)
                .items(list)
                .displayText(display)
                .value(stateSelected);


        if (compareById) {
            select.compareById();
        }

        if(expandAutomatically != null) {
            select.expandWhen(expandAutomatically);
        }

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(select);
    }

    public static <T> Component SelectColumnWithButton(
            String label,ListState<T> list, State<T> stateSelected,
            Function<T, String> display, boolean compareById,
            String btnText, Runnable handleClick) {

        var rowProps = new RowProps().spacingOf(2)
                .bottomVertically();

        return new Row(rowProps)
                .r_child(Components.SelectColumn(label, list, stateSelected, display, compareById))
                .r_child(new Button(btnText, new ButtonProps().height(31)
                        .textColor("#FFF")).onClick(handleClick)
                ).r_child(new SpacerVertical(2));
    }

    public static <T> Component SelectColumn(String label, ListState<T> list, State<T> stateSelected, Function<T, String> display, boolean compareById) {
        var select = new Select<T>(selectProps)
                .items(list)
                .value(stateSelected)
                .displayText(display);

        if (compareById) {
            select.compareById();
        }

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(select);
    }

    public static Column TextColumn(String label, String value) {
        return new Column(new ColumnProps()
                .borderColor(ThemeManager.theme().colors().primary())
                .borderWidth(ThemeManager.theme().border().width()))
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()))
                .c_child(new Text(value, new TextProps().fontSize(ThemeManager.theme().typography().body())));
    }

    public static Row displayOperationsRow(TotaisState totais) {
        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(TextWithValue("Valor total(bruto): ", totais.totalBruto))
                .r_child(TextWithValue("Desconto: ", totais.descontoComputed))
                .r_child(TextWithValue("Total geral(líquido): ", totais.totalLiquido.map(Utils::toBRLCurrency)));
    }

    public static Component TextWithValue(String label, ReadableState<String> valueState) {
        return new Row()
                .r_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()))
                .r_child(new Text(valueState, new TextProps().fontSize(ThemeManager.theme().typography().body())));
    }

    public static Component InputColumnCep(String label, State<String> inputState) {
        var inputProps = getInputProps("00000-000");

        var input = new Input(inputState, inputProps)
                .onInitialize(value -> {
                    String formatted = formatCep(value);
                    return OnChangeResult.of(formatted, value);
                })
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");

                    if (numeric.length() > 8) {
                        numeric = numeric.substring(0, 8);
                    }

                    String formatted = formatCep(numeric);
                    return OnChangeResult.of(formatted, numeric);
                })
                .lockCursorToEnd();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static Component InputColumnCpf(String label, State<String> inputState) {
        var inputProps = getInputProps("000.000.000-00");

        var input = new Input(inputState, inputProps)
                .onInitialize(value -> {
                    String formatted = formatCpf(value);
                    return OnChangeResult.of(formatted, value);
                })
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");

                    if (numeric.length() > 11) {
                        numeric = numeric.substring(0, 11);
                    }

                    String formatted = formatCpf(numeric);
                    return OnChangeResult.of(formatted, numeric);
                })
                .lockCursorToEnd();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static class InputRef {
        private Input inputRef;

        public void set(Input input) {
            this.inputRef = input;
        }

        public void requestFocus() {
            UI.runOnUi(() -> {
                Node node = inputRef.getJavaFxNode();
                if (node instanceof Parent parent) {
                    for (Node child : parent.getChildrenUnmodifiable()) {
                        if (child instanceof TextInputControl) {
                            child.requestFocus();
                            return;
                        }
                    }
                }
                node.requestFocus();
            });
        }
    }

    public static Component InputColumnDecimal(String label, State<String> inputState, String placeholder, InputRef inputRef) {
        var inputProps = getInputProps(placeholder);

        var input = new Input(inputState, inputProps)
                .onInitialize(value -> {
                    if (value == null || value.trim().isEmpty()) {
                        return OnChangeResult.of("", "");
                    }
                    return OnChangeResult.of(formatarDecimal(value), value);
                })
                .onChange(value -> {
                    if (value == null) value = "";
                    String cleaned = value.replaceAll("[^0-9,]", "");
                    int commaIdx = cleaned.indexOf(',');
                    if (commaIdx >= 0 && commaIdx != cleaned.lastIndexOf(',')) {
                        cleaned = cleaned.substring(0, cleaned.length() - 1);
                        commaIdx = cleaned.indexOf(',');
                    }
                    String intPart = commaIdx >= 0 ? cleaned.substring(0, commaIdx) : cleaned;
                    String decPart = commaIdx >= 0 ? "," + cleaned.substring(commaIdx + 1) : "";
                    String intTrimmed = intPart.isEmpty() ? "0" : intPart.replaceFirst("^0+(?!$)", "");
                    StringBuilder fmt = new StringBuilder();
                    int len = intTrimmed.length();
                    for (int i = 0; i < len; i++) {
                        if (i > 0 && (len - i) % 3 == 0) fmt.append('.');
                        fmt.append(intTrimmed.charAt(i));
                    }
                    String display = fmt + decPart;
                    String internal = intTrimmed + (commaIdx >= 0 ? "." + cleaned.substring(commaIdx + 1) : "");
                    return OnChangeResult.of(display, internal);
                })
                .lockCursorToEnd();

        if(inputRef != null) inputRef.set((Input) input);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static Component InputColumnDecimal(String label, State<String> inputState, String placeholder) {
        return InputColumnDecimal(label, inputState, placeholder, null);
    }

    private static String formatarDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String normalizado = value.replace(",", ".");
        int dotIdx = normalizado.indexOf('.');
        String intPart = dotIdx >= 0 ? normalizado.substring(0, dotIdx) : normalizado;
        String decPart = dotIdx >= 0 ? normalizado.substring(dotIdx + 1) : "";
        intPart = intPart.replaceAll("[^0-9]", "");
        decPart = decPart.replaceAll("[^0-9]", "");
        intPart = intPart.isEmpty() ? "0" : intPart.replaceFirst("^0+(?!$)", "");
        StringBuilder fmt = new StringBuilder();
        int len = intPart.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) fmt.append('.');
            fmt.append(intPart.charAt(i));
        }
        return decPart.isEmpty() ? fmt.toString() : fmt + "," + decPart;
    }

    public static Component InputColumnCnpjAlfanumerico(String label, State<String> inputState) {
        var inputProps = getInputProps("AA.AAA.AAA/AAAA-DD");

        var input = new Input(inputState, inputProps)
                .onInitialize(value -> {
                    String formatted = formatCnpj(value);
                    return OnChangeResult.of(formatted, value);
                })
                .onChange(value -> {
                    String raw = value.toUpperCase().replaceAll("[^0-9A-Z]", "");

                    if (raw.length() > 14) {
                        raw = raw.substring(0, 14);
                    }

                    String formatted = formatCnpj(raw);
                    return OnChangeResult.of(formatted, raw);
                })
                .lockCursorToEnd();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static Component InputColumnPhone(String label, State<String> inputState) {
        var inputProps = getInputProps("(00) 00000-0000");

        var input = new Input(inputState, inputProps)
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
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static Component InputColumnNumeric(String label, State<String> inputState, String placeholder) {
        var inputProps = getInputProps(placeholder);

        var input = new Input(inputState, inputProps)
                .onChange(value -> {
                    String numeric = value.replaceAll("[^0-9]", "");
                    if (numeric.isEmpty()) {
                        return OnChangeResult.of("", "");
                    }
                    return OnChangeResult.of(numeric, numeric);
                })
                .lockCursorToEnd();

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    private static final NumberFormat BRL =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public static Component InputColumnCurrency(String label, State<String> inputState, boolean disableInput) {
        var icon = Entypo.CREDIT;
        var fonticon = FontIcon.of(icon, 15, Color.web("green"));

        var inputProps = getInputProps("R$ 0,00").width(140);

        if (disableInput) inputProps.disable();

        // inputState armazena valores brutos (em centavos), campo exibe formato BRL
        var input = new Input(inputState, inputProps)
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
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static Component InputColumnCurrency(String label, State<String> inputState) {
        return InputColumnCurrency(label, inputState, false);
    }

    static InputProps getInputProps(String placeholder) {
        return getInputProps(placeholder, 31);
    }

    static InputProps getInputProps(String placeholder, int height) {
        return new InputProps().height(height)
                .placeHolder(placeholder).fontSize(ThemeManager.theme().typography().small());
    }

    public static Component InputColumnComEnterHandler(String label, ReadableState<String> inputState, String placeholder, Runnable onEnter) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(new Input((State<String>) inputState,
                                getInputProps(placeholder).borderWidth(ThemeManager.theme().border().width())
                                        .borderColor(ThemeManager.theme().colors().border()).borderRadius(ThemeManager.theme().border().radiusMd())
                        ).onEnter(onEnter)
                );
    }

    public static Component InputColumnComDynamicSearch(String label, ReadableState<String> inputState,
                                                        String placeholder,
                                                        megalodonte.v2.ListState<ProdutoModel> produtoModelListState,
                                                        State<ProdutoModel> produtoSelected,
                                                        ComputedState<Boolean> sugestoesProdutoVisible) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(new Input((State<String>) inputState,
                                getInputProps(placeholder)
                        )
                )
                .c_child(Show.when(sugestoesProdutoVisible,
                        ()-> SelectColumn("Produto encontrado", produtoModelListState, produtoSelected,
                                it->  it.getCodigoBarras() + " - " + it.getDescricao(),true, sugestoesProdutoVisible )));
    }

    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder, boolean disableInput,
                                        int borderWidth, int borderRadius, String borderColor, String labelColor) {
        var props = getInputProps(placeholder);
        if (disableInput) props.disable();


        TextProps labelProps = new TextProps().fontSize(ThemeManager.theme().typography().small());
        if (labelColor!=null) {
            labelProps.color(labelColor);
            labelProps.textColor(labelColor);
        }

        return new Column()
                .c_child(new Text(label, labelProps))
                .c_child(new Input((State<String>) inputState,
                                props.borderWidth(borderWidth).borderColor(borderColor).borderRadius(borderRadius)
                        )
                );
    }


    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder, boolean disableInput,String labelColor) {
        return InputColumn(label, inputState, placeholder, disableInput, ThemeManager.theme().border().width(), ThemeManager.theme().border().radiusMd(), ThemeManager.theme().colors().border(),labelColor);
    }

    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder, boolean disableInput) {
        return InputColumn(label, inputState, placeholder, disableInput,null);
    }

    public static Component InputColumnAuth(String label, ReadableState<String> inputState, String placeholder) {
        return InputColumn(label, inputState, placeholder, false, "#fff");
    }

    public static Component InputColumn(String label, ReadableState<String> inputState, String placeholder) {
        return InputColumn(label, inputState, placeholder, false);
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder) {
        return TextAreaColumn(label, inputState, placeholder, 80);
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder, int height) {
        TextAreaInput textAreaInput = new TextAreaInput(inputState,
                getInputProps(placeholder, height).width(400)
        );

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(textAreaInput);
    }

    public static Component InputWithButtonRow(String label, String btnTitle, State<String> inputState, Runnable onClick) {
        return new Row(new RowProps().bottomVertically())
                .r_child(Components.InputColumn(label, inputState, ""))
                .r_child(new Button(btnTitle, new ButtonProps().height(32).textColor("#FFF")
                                .borderRadius(ThemeManager.theme().border().radiusSm()).borderWidth(ThemeManager.theme().border().width()).borderColor(ThemeManager.theme().colors().primary())
                        )
                                .onClick(onClick)
                );
    }

    public static Component errorText(String message) {
        return new Container(new ContainerProps().bgColor("white")).c_child(new SpacerVertical(5))
                .c_child(new Text(message, new TextProps().variant(TextVariant.SUBTITLE).textColor("red")));
    }

//v3
    public static <T> Component commonCustomMenusv3(
            State<Boolean> focusState, Runnable onClickNew,
            Runnable onEdit, Runnable onDelete, Runnable onClone) {

        return new Row(new RowProps().spacingOf(20))
                .children(
                        MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", () -> executar(onClickNew::run)),
                        Show.when(focusState, ()-> new Row(new RowProps().spacingOf(20)).children(
                                MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(onEdit::run)),
                                MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(onDelete::run)),
                                MenuItem("Clonar", Entypo.COPY, "black", () -> executar(onClone::run))
                        )).withTransition(Animations::fadeSlide)
                );
    }

    public static <T> Component commonCustomMenus(
            State<T> itemSelectedInTable, Runnable onClickNew,
            Runnable onEdit, Runnable onDelete, Runnable onClone) {

        ComputedState<Boolean> thereIsItemSelectedInTable = ComputedState.of(() -> !itemSelectedInTable.isNull(), itemSelectedInTable);

        return Show.when(thereIsItemSelectedInTable, () -> new Row(new RowProps().spacingOf(20))
                .children(
                        MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", () -> executar(onClickNew::run)),
                        MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(onEdit::run)),
                        MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(onDelete::run)),
                        MenuItem("Clonar", Entypo.COPY, "black", () -> executar(onClone::run))
                )).withTransition(Animations::fadeSlide);
    }


    @Deprecated
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
        ), onClick);
    }

    public static Component searchInput(State<String> stateInput, String placeholder) {
        var icon = FontIcon.of(AntDesignIconsOutlined.SEARCH, 20, Color.web(ThemeManager.theme().colors().secondary()));
        return new Input(stateInput,
                new InputProps().placeHolder(placeholder)
                        .width(300)
                        .height(31))
                .left(icon);
    }

    private static void executar(Action action) {
        try {
            action.run();
            IO.println("Operation completed successfully");
        } catch (Exception e) {
            IO.println("Error: " + e.getMessage());

        }
    }

    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }
}
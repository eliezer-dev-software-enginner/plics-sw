package my_app.core.components;

import disgust.io.ButtonsPack;
import disgust.io.br.Pack;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.application.ErrorReporter;
import megalodonte.base.Animations;
import megalodonte.base.async.RunnableThrowing;
import megalodonte.base.components.Component;
import megalodonte.base.components.IconInterface;
import megalodonte.base.components.Ref;
import megalodonte.base.state.ReadableState;
import megalodonte.base.state.State;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.DatePicker;
import megalodonte.components.inputs.OnChangeResult;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.components.v2.Input;
import megalodonte.props.*;
import megalodonte.props.v2.InputProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.v2.ListState;
import megalodonte.v2.Show;
import my_app.core.db.models.ProdutoModel;
import my_app.core.Data;
import my_app.core.Identifier;
import my_app.core.Parcela;
import my_app.core.states.EnderecoState;
import my_app.core.states.TotaisState;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;
import pack.utilities.CurrencyPack;
import pack.utilities.DatePack;
import pack.utilities.FormatterPack;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class Components {

    public static IconInterface ikon(Ikon ikon, double size, String color) {
        return IconInterface.of(FontIcon.of(ikon, (int) size, Color.web(color)));
    }

    public record Endereco(String uf, String cep, String cidade, String bairro,String rua, String numero){}
    public static Component ItemDetailEndereco(Endereco endereco){
        return new Container()
                .c_child(Components.TextWithDetails("UF: ", endereco.uf()))
                .c_child(Components.TextWithDetails("CEP: ", FormatterPack.formatCep(endereco.cep())))
                .c_child(Components.TextWithDetails("Cidade: ", endereco.cidade()))
                .c_child(Components.TextWithDetails("Bairro: ", endereco.bairro()))
                .c_child(Components.TextWithDetails("Rua: ", endereco.rua()))
                .c_child(Components.TextWithDetails("Número: ", endereco.numero()));
    }

    public static Component enderecoComponent(EnderecoState enderecoState){
        return new Container().children(
                Components.FormSubtitle("Endereço"),
                new FlowRow(new FlowRowProps().spacingOf(10))
                        .children(
                                Pack.InputColumnCep("Cep", enderecoState.cep),
                                Components.SelectColumn("UF", Data.ufList, enderecoState.ufSelected, it -> it),
                                disgust.io.Pack.InputColumn("Cidade", enderecoState.cidade, "Ex: São Paulo"),
                                disgust.io.Pack.InputColumn("Bairro", enderecoState.bairro, "Ex: Centro"),
                                disgust.io.Pack.InputColumn("Rua", enderecoState.rua, "Ex: Av. Brasil"),
                                disgust.io.Pack.InputColumnNumeric("Número", enderecoState.numero, "Ex: 123")
                        )
        );
    }

    public static Component imageWithTextRow(String imgPath, String text) {
        return new Row().children(
                new Image(imgPath, new ImageProps().size(25)),
                new SpacerHorizontal(5),
                new Text(text, new TextProps().textColor("white").fontSize(14))
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

        RunnableThrowing handleGerarParcelas = () -> {
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
                                        .r_child(ButtonsPack.OutlinedButton("Gerar parcelas",ButtonVariant.PRIMARY, handleGerarParcelas)))
                        .items(parcelaComponentForEachState)
        );
    }

    public static Component parcelaItem(Parcela parcela) {
        return new Row(new RowProps())
                .r_child(Components.TextColumn("PARCELA", String.valueOf(parcela.numero())))
                .r_child(Components.TextColumn("VENCIMENTO", DatePack.millisToBrazilianDateTime(parcela.dataVencimento())))
                .r_child(Components.TextColumn("VALOR", String.format("R$ %.2f", parcela.valor())));
    }

    public static Component actionButton(State<String> btnText, RunnableThrowing onClick) {
        return new Button(btnText,
                new ButtonProps()
                        .fillWidth()
                        .paddingTop(10)
                        .paddingDown(10)
                        .textColor("white").bgColor(ThemeManager.theme().colors().primary())
        ).onClick(onClick);
    }
    public static Component actionButton(String btnText, RunnableThrowing onClick) {
        return ButtonsPack.ContainedButton(btnText, ButtonVariant.PRIMARY, onClick);
    }

    public static Component actionButtons(ComputedState<String> btnText, RunnableThrowing onClick) {
        return new Button(btnText,
                new ButtonProps()
                        .fillWidth()
                        .fontSize(16)
                        .textColor("white").bgColor("#10b981")
        ).onClick(onClick);
    }

    public static Component ScrollPaneDefault(Component child) {
        var scroll = new ScrollPane();
        scroll.setContent(child.getJavaFxNode());
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        // -fx-background-color no node raiz não alcança a subestrutura do skin
        // (.viewport, .corner) — cada um tem seu próprio background opaco herdado
        // do modena.css, sentando por cima de qualquer bgImage atrás do ScrollPane.
        // -fx-background é a variável que o modena.css usa internamente tanto pro
        // scroll-pane quanto pro viewport, então sobrescrever ela aqui cobre os dois.
        scroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );

        return Component.CreateFromJavaFxNode(scroll);
    }

    public static void ShowPopupWithButton(
            ScreenContextInterface screenContext, String message, String btnTitle, RunnableThrowing callback) {
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
                                    popup.hide();
                                })
                        )
                ));


        popup.getContent().add(card.getJavaFxNode());
        popup.show(screenContext.selfStage());
    }

    public static void ShowPopup(ScreenContextInterface context, String message) {
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

    public static Stage ShowModal(Component ui, ScreenContextInterface context, int height) {
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
        return stage;
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

    public static Card CardImageSelector(State<String> imagemState, RunnableThrowing handleChangeImage) {
        return new Card(
                new Column(new ColumnProps().centerHorizontally().spacingOf(15))
                        .c_child(new Text("Foto do produto", new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()))
                        .c_child(new Image(imagemState, new ImageProps().size(120)))
                        .c_child(new SpacerVertical().fill())
                        .c_child(new Button("Inserir imagem",
                                new ButtonProps().fontSize(ThemeManager.theme().typography().small()).bgColor("#A6B1E1"))
                                .onClick(handleChangeImage)
                        ),
                new CardProps().height(300).paddingAll(20)
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
        return DatePickerColumn(localDateState, label, null);
    }

    public static Component DatePickerColumn(State<LocalDate> localDateState, String label, IconInterface icon) {
        var datePicker = new DatePicker(localDateState,
                new DatePickerProps()
                        .placeHolder("dd/mm/yyyy")
                        .locale(new Locale("pt", "BR"))
                        .pattern("dd/MM/yyyy")
                        .width(160)
                        .editable(false)
        );

        if (icon != null) {
            datePicker.icon(icon);
        }

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(datePicker);
    }

    public static Column ImageSelector(String title, State<String> imageState,
                                       ImageProps props,
                                       RunnableThrowing callback) {
        return new Column()
                .c_child(new Image(imageState, props))
                .c_child(new SpacerVertical(10))
                .c_child(ButtonsPack.OutlinedButton(title, ButtonVariant.PRIMARY, callback));
    }

    public static Text FormTitle(String title, String textColor) {
        return new Text(title, new TextProps().fontSize(ThemeManager.theme().typography().title()).bold().textColor(textColor));
    }

    public static Text FormTitle(String title) {
        return new Text(title, new TextProps().fontSize(ThemeManager.theme().typography().title()).bold());
    }

    public static Text FormSubtitle(String title, String color) {
        return new Text(title, new TextProps().fontSize(ThemeManager.theme().typography().subtitle())
                .textColor(color));
    }

    public static Text FormSubtitle(String title) {
        return FormSubtitle(title, "black");
    }

    static final ButtonProps propsBtnCadastro = new ButtonProps().fillWidth()
            .fontSize(ThemeManager.theme().typography().small()).textColor("white").bgColor("#2563eb");

    public static Component ButtonCadastro(ComputedState<String> textState, RunnableThrowing handleAdd) {
        return new Button(textState, propsBtnCadastro
        ).onClick(handleAdd);
    }

    @Deprecated(forRemoval = true)
    public static Component ButtonCadastro(State<String> textState, RunnableThrowing handleAdd) {
        return new Button(textState, propsBtnCadastro).onClick(handleAdd);
    }

    private final static SelectProps selectProps = new SelectProps()
            .minWidth(100);


    /**
     * Comparador por id que enxerga o campo herdado da superclasse. Os models
     * guardam o id em Identifier (superclasse), então o compareById() do Select
     * (que usa getDeclaredField, só campos da própria classe) nunca acerta —
     * o valor selecionado via buscarById() ficava como referência "estranha"
     * fora da lista e o select pintava o toString() do objeto na tela.
     */
    private static <T> boolean sameId(T a, T b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a instanceof Identifier ai && b instanceof Identifier bi) {
            return ai.getId() != null && ai.getId().equals(bi.getId());
        }
        return a.equals(b);
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
                .displayText(display);

        if (compareById) {
            select.itemComparator(Components::sameId);
        }

        select.value(stateSelected);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(select);
    }

    public static <T> Component SelectColumn(String label, ListState<T> list, State<T> stateSelected, Function<T, String> display,
                                             boolean compareById, ReadableState<Boolean> expandAutomatically) {
        var select = new Select<T>(selectProps)
                .items(list)
                .displayText(display);

        if (compareById) {
            select.itemComparator(Components::sameId);
        }

        select.value(stateSelected);

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
            String btnText, RunnableThrowing handleClick) {

        var rowProps = new RowProps().spacingOf(2)
                .bottomVertically();

        return new Row(rowProps)
                .r_child(Components.SelectColumn(label, list, stateSelected, display, compareById))
                .r_child(new Button(btnText, new ButtonProps()
                        .textColor("#FFF")).onClick(handleClick)
                ).r_child(new SpacerVertical(2));
    }

    public static <T> Component SelectColumn(String label, ListState<T> list, State<T> stateSelected, Function<T, String> display, boolean compareById) {
        var select = new Select<T>(selectProps)
                .items(list)
                .displayText(display);

        if (compareById) {
            select.itemComparator(Components::sameId);
        }

        select.value(stateSelected);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(select);
    }

    public static Column TextColumn(String label, String value) {
        return new Column(new ColumnProps())
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()))
                .c_child(new Text(value, new TextProps().fontSize(ThemeManager.theme().typography().body())));
    }

    public static Row displayOperationsRow(TotaisState totais) {
        return new Row(new RowProps().bottomVertically().spacingOf(ThemeManager.theme().spacing().sm()))
                .r_child(TextWithValue("Valor total(bruto): ", totais.totalBruto))
                .r_child(TextWithValue("Desconto: ", totais.descontoComputed))
                .r_child(TextWithValue("Total geral(líquido): ", totais.totalLiquido.map(CurrencyPack::toBRLCurrency)));
    }

    public static Component TextWithValue(String label, ReadableState<String> valueState) {
        return new Row()
                .r_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().body()).bold()))
                .r_child(new Text(valueState, new TextProps().fontSize(ThemeManager.theme().typography().body())));
    }



    /**
     * Trocar por Ref do pacote megalodonte.base
     */
    @Deprecated(forRemoval = true)
    public static class InputRef {
        private megalodonte.components.inputs.Input inputRef;

        public void set(megalodonte.components.inputs.Input input) {
            this.inputRef = input;
        }

        public void requestFocus() {
            inputRef.requestFocus();
        }
    }

    public static Component InputColumnDecimal(String label, State<String> inputState, String placeholder) {
        return InputColumnDecimal(label,inputState,placeholder,null);
    }
    public static Component InputColumnDecimal(String label, State<String> inputState, String placeholder, InputRef inputRef) {
        var inputProps = getInputProps(placeholder).width(140);

        var input = new megalodonte.components.inputs.Input(inputState, inputProps)
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

        if(inputRef != null) inputRef.set((megalodonte.components.inputs.Input) input);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
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

    public static Component InputColumnNumeric(String label, State<String> inputState, String placeholder) {
        var inputProps = getInputProps(placeholder).width(100);

        var input = new megalodonte.components.inputs.Input(inputState, inputProps)
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

    static megalodonte.props.InputProps getInputProps(String placeholder) {
        return new megalodonte.props.InputProps()
                .placeHolder(placeholder).fontSize(ThemeManager.theme().typography().small())
                .fontSize(ThemeManager.theme().typography().small());
    }


    public static Component InputColumnComEnterHandler(String label, ReadableState<String> inputState, String placeholder,
                                                         Runnable onEnter, Ref<Input> ref) {
        var input = new megalodonte.components.inputs.Input((State<String>) inputState,
                        getInputProps(placeholder).width(100).borderWidth(ThemeManager.theme().border().width())
                                .borderColor(ThemeManager.theme().colors().border()).borderRadius(ThemeManager.theme().border().radiusMd())
                ).onEnter(onEnter);
        if (ref != null) input.ref(ref);

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(input);
    }

    public static Component SelectDropDownSearch(String label, ReadableState<String> inputState,
                                                        String placeholder,
                                                        megalodonte.v2.ListState<ProdutoModel> produtoModelListState,
                                                        State<ProdutoModel> produtoSelected,
                                                        ComputedState<Boolean> sugestoesProdutoVisible) {

        ForEachState<ProdutoModel,Component> produtoModelRowForEachState = ForEachState.of(produtoModelListState,
                produtoModel ->
                    new Clickable(
                            new Card(new Row(new RowProps().spacingOf(ThemeManager.theme().spacing().sm())).children(
                                    Show.when(produtoModel.getImagem()!=null, ()-> new Image(produtoModel.getImagem(), new ImageProps().size(30))),
                                    new Text(produtoModel.getCodigoBarras() + " - " + produtoModel.getDescricao())
                            ))
                    ).onClick(()-> produtoSelected.set(produtoModel))
                );

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(new megalodonte.components.inputs.Input((State<String>) inputState,
                                getInputProps(placeholder).width(220)
                        )
                )
                .c_child(Show.when(sugestoesProdutoVisible,
                            ()-> new Column(new ColumnProps().maxHeight(200))
                                    .items(produtoModelRowForEachState,true)
                        )
                );
    }

    public static Component InputColumnComDynamicSearch(String label, ReadableState<String> inputState,
                                                        String placeholder,
                                                        megalodonte.v2.ListState<ProdutoModel> produtoModelListState,
                                                        State<ProdutoModel> produtoSelected,
                                                        ComputedState<Boolean> sugestoesProdutoVisible) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(new megalodonte.components.inputs.Input((State<String>) inputState,
                                getInputProps(placeholder).width(220)
                        )
                )
                .c_child(Show.when(sugestoesProdutoVisible,
                        ()-> SelectColumn("Produto encontrado", produtoModelListState, produtoSelected,
                                it->  it.getCodigoBarras() + " - " + it.getDescricao(),true, sugestoesProdutoVisible )));
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder) {
        return TextAreaColumn(label, inputState, placeholder, 80);
    }

    public static Component TextAreaColumnWidthNoRestricted(String label, State<String> inputState, String placeholder, int height) {
        TextAreaInput textAreaInput = new TextAreaInput(inputState,
                getInputProps(placeholder).height(height)
        );

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(textAreaInput);
    }

    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder, int height) {
        TextAreaInput textAreaInput = new TextAreaInput(inputState,
                getInputProps(placeholder).width(400).height(height)
        );

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(textAreaInput);
    }

    // Variante que cresce com o conteúdo em vez de ficar travado numa altura fixa
    // (a outra sobrecarga, com um único "height", trava prefHeight=minHeight=maxHeight).
    // Não reaproveita getInputProps(placeholder) — aquele helper seta height=31 por
    // baixo (feito pra Input de uma linha), e InputProps.applyTextAreaTheme prioriza
    // "height" sobre "maxHeight" quando os dois estão setados, então o campo ficava
    // travado em 31px em vez de crescer.
    public static Component TextAreaColumn(String label, State<String> inputState, String placeholder, int minHeight, int maxHeight) {
        TextAreaInput textAreaInput = new TextAreaInput(inputState,
                new megalodonte.props.InputProps()
                        .placeHolder(placeholder)
                        .fontSize(ThemeManager.theme().typography().small())
                        .minHeight(minHeight)
                        .maxHeight(maxHeight)
                        .width(400)
        );

        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(ThemeManager.theme().typography().small())))
                .c_child(textAreaInput);
    }
    //v3
    public static <T> Component commonCustomMenusv3(
            State<Boolean> focusState, Runnable onClickNew,
            Runnable onEdit, Runnable onDelete, Runnable onClone) {

        return new Row(new RowProps().spacingOf(ThemeManager.theme().spacing().md()))
                .children(
                        MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", onClickNew::run),
                        Show.when(focusState, ()-> new Row(new RowProps().spacingOf(ThemeManager.theme().spacing().md())).children(
                                MenuItem("Editar", Entypo.EDIT, "blue", onEdit::run),
                                MenuItem("Excluir", Entypo.TRASH, "red", onDelete::run),
                                MenuItem("Clonar", Entypo.COPY, "black", onClone::run)
                        )).withTransition(Animations::fadeSlide)
                );
    }

    @Deprecated
    public static Row commonCustomMenus(Runnable onClickNew, Runnable onEdit, Runnable onDelete, Runnable onClone) {
        return new Row(new RowProps().spacingOf(20))
                .r_child(MenuItem("Novo (CTRL + N)", Entypo.ADD_TO_LIST, "green", onClickNew::run))
                .r_child(MenuItem("Editar", Entypo.EDIT, "blue", onEdit::run))
                .r_child(MenuItem("Excluir", Entypo.TRASH, "red", onDelete::run))
                .r_child(MenuItem("Clonar", Entypo.COPY, "black", onClone::run))
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
                        .c_child(new Text(title, new TextProps().fontSize(ThemeManager.theme().typography().small())))
        ), onClick);
    }

    public static Component searchInputFill(State<String> stateInput, String placeholder) {
        var icon = FontIcon.of(AntDesignIconsOutlined.SEARCH, 20, Color.web(ThemeManager.theme().colors().secondary()));
        return new Input(stateInput,
                new InputProps().placeHolder(placeholder))
                .left(icon);
    }

    public static Component searchInput(State<String> stateInput, String placeholder) {
        var icon = FontIcon.of(AntDesignIconsOutlined.SEARCH, 20, Color.web(ThemeManager.theme().colors().secondary()));

        return new megalodonte.components.inputs.Input(stateInput,getInputProps(placeholder).width(300))
                .left(icon);
    }
    
}
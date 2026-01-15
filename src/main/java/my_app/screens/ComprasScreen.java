package my_app.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import megalodonte.ComputedState;
import megalodonte.Show;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.screens.components.Components;
import my_app.utils.Utils;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComprasScreen implements ScreenComponent {
    private final Router router;
    private CategoriaRepository categoriaRepository = new CategoriaRepository();

    State<LocalDate> dataCompra = State.of(LocalDate.now());
    State<String> numeroNota = State.of("");
    State<String> btnText = State.of("+ Adicionar");

    ObservableList<CategoriaModel> categoriasObservable = FXCollections.observableArrayList();

    State<String> codigo = State.of("");
    State<ProdutoModel> produtoEncontrado = State.of(null);
    State<String> qtd = State.of("2");
    State<String> observacao = State.of("");

    List<String> tiposPagamento = List.of("A VISTA","CRÉDITO", "DÉBITO", "PIX", "A PRAZO");
    State<String> tipoPagamentoSeleced = State.of(tiposPagamento.get(1));

    State<String> descontoEmDinheiro = State.of("0");

    // Preço de compra (armazena em centavos, ex: 123 = R$ 1,23)
    State<String> pcCompra = State.of("0");

    ComputedState<String> totalBruto = ComputedState.of(()-> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty()? "0": qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        return Utils.toBRLCurrency(BigDecimal.valueOf(qtdValue * precoCompraValue));
    }, descontoEmDinheiro, qtd, pcCompra);


    ComputedState<String> totalLiquido = ComputedState.of(()-> {
        int qtdValue = Integer.parseInt(qtd.get().trim().isEmpty()? "0": qtd.get());
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;

        double precoDescontoValue = Double.parseDouble(descontoEmDinheiro.get()) / 100.0;

        return Utils.toBRLCurrency(BigDecimal.valueOf(qtdValue * precoCompraValue - precoDescontoValue));
    }, descontoEmDinheiro, qtd, pcCompra);


    //State<String> totalLiquido = State.of("0");
    State<String> dataValidade = State.of("0");

    public final ObservableList<FornecedorModel> fornecedores = FXCollections.observableArrayList();
    public final State<FornecedorModel> fornecedorSelected = State.of(null);


    public ComprasScreen(Router router) {
        this.router = router;
    }

    @Override
    public void onMount() {
        fetchData();
    }

    private void fetchData() {
        //TODO: implementar

        Async.Run(()->{
            try{
               var list = new FornecedorRepository().listar();
              fornecedores.addAll(list);//meu select fica preenchido

               UI.runOnUi(()->{
                   if(!list.isEmpty()){
                       list.stream().filter(f-> f.id == 1L)
                               .findFirst()
                               .ifPresent(fornecedorSelected::set);
                   }
               });

            }catch (SQLException e){
IO.println("Erro on fetch data: " + e.getMessage());
            }

        });

        IO.println(dataCompra.get());
    }

    private final Theme theme = ThemeManager.theme();


    public Component render() {
        return new Column(new ColumnProps().paddingAll(5), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(
                      this::handleClickMenuNew,this::handleClickMenuEdit, this::handleClickMenuDelete))
                .c_child(new SpacerVertical(10))
                .c_child(form())
                .c_child(new SpacerVertical(20));
    }


    Component form(){
        final var top = new Row(new RowProps().bottomVertically().spacingOf(10))
//                .r_child(Components.InputColumn("Data de compra", codigo,"Ex: 01/12/2026"))
                .r_child(Components.DatePickerColumn(dataCompra,"Data de compra 2", "dd/mm/yyyy"))
                .r_child(Components.SelectColumn("Fornecedor", fornecedores, fornecedorSelected, f-> f.nome))
                .r_child(Components.InputColumn("N NF/Pedido compra", numeroNota,"Ex: 12345678920"));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total(bruto): ", totalBruto))
                .r_child(Components.TextWithValue("Desconto: ", descontoEmDinheiro))
                .r_child(Components.TextWithValue("Total geral(líquido): ", totalLiquido)
                );


        return new Card(new Column()
                .c_child(Components.FormTitle("Cadastrar Nova Compra"))
                .c_child(new SpacerVertical(20))
                .c_child(top)
                .c_child(new SpacerVertical(10))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(Components.InputColumn("Código", codigo,"xxxxxxxx"))
                        .r_child(Components.InputColumn("Descrição do produto",produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: Paraiso"))
                        //.r_child(Components.InputColumn("Pc. de compra", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: R$ 10,00"))
                        .r_child(Components.InputColumnCurrency("Pc. de compra", pcCompra))
                        .r_child(Components.InputColumn("Quantidade", qtd,"Ex: 2"))
                        .r_child(Components.InputColumnCurrency("Desconto em R$", descontoEmDinheiro))
                )
                .c_child(new SpacerVertical(10))
                .c_child(
                        new Row(new RowProps().spacingOf(10)).r_child(Components.SelectColumn("Tipo de pagamento",tiposPagamento, tipoPagamentoSeleced,it->it))
                                        .r_child(Components.TextAreaColumn("Observação",observacao,""))
                        )
                .c_child(new SpacerVertical(10))
                .c_child(valoresRow)
        );
    }

    private void handleAdd(LocalDate localDate){
        //TODO: implementar

        IO.println(dataCompra.get());

        String dataBR = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        // p.precoCompra = new BigDecimal(precoCompraRaw.get()).movePointLeft(2);

    }

    private void handleClickMenuNew() {
        btnText.set("+ Adicionar");
        //nome.set("");
    }

    private void handleClickMenuEdit() {
        //  nome.set(categoriaSelecionada.get().nome);
        btnText.set("+ Atualizar");
    }

    private void handleClickMenuDelete() {
        //TODO: implementar
    }



}
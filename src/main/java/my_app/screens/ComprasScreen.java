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
    State<String> totalBruto = State.of("0");
    State<String> descontoPorcentagem = State.of("0");
    State<String> descontoEmDinheiro = State.of("0");

    //acho que é preço cadastrado de compra -> ver video
    State<String> pcCompraRow = State.of("0");
    State<String> pcCompra = State.of("0");


    ComputedState<String> totalLiquido = ComputedState.of(()-> {
        int qtdValue = Integer.parseInt(qtd.get());
        IO.println("qtdValue: " + qtdValue);
        double precoCompraValue = Double.parseDouble(pcCompra.get()) / 100.0;
        IO.println("precoCompraValue: " + precoCompraValue);

        return String.valueOf(qtdValue * precoCompraValue);
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
                       list.stream().filter(f-> f.id == 1L).findFirst()
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
                .r_child(Components.InputColumn("N NF/Pedido compra", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: 12345678920"));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total:", codigo))
                .r_child(Components.TextWithValue("Desconto:", produtoEncontrado.map(p-> p != null? p.descricao: "")))
                .r_child(Components.TextWithValue("Total geral:", totalLiquido)
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
                        .r_child(Components.InputColumnCurrency("Pc. de compra", pcCompra, pcCompraRow))
                        .r_child(Components.InputColumn("Quantidade", qtd,"Ex: 2"))
                        .r_child(Components.InputColumn("Tipo de unidade", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: rua das graças")))
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
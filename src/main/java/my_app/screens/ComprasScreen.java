package my_app.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import megalodonte.Show;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
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
import java.util.ArrayList;
import java.util.List;

public class ComprasScreen implements ScreenComponent {
    private final Router router;

    State<String> dataCompra = State.of("");
    State<String> fornecedorNameSelected = State.of("");
    State<String> numeroNota = State.of("");
    State<String> btnText = State.of("+ Adicionar");

    ObservableList<CategoriaModel> categoriasObservable = FXCollections.observableArrayList();

    private CategoriaRepository categoriaRepository = new CategoriaRepository();
    State<String> codigo = State.of("");
    State<ProdutoModel> produtoEncontrado = State.of(null);
    State<String> qtd = State.of("0");
    State<String> totalBruto = State.of("0");
    State<String> descontoPorcentagem = State.of("0");
    State<String> descontoEmDinheiro = State.of("0");
    State<String> totalLiquido = State.of("0");
    State<String> dataValidade = State.of("0");

    //acho que é preço cadastrado de compra -> ver video
    private State<String> pcCompra = State.of("");
    
    State<Boolean> hasProdutoEncontrado = State.of(false);
    State<Boolean> hasntProdutoEncontrado = State.of(true);

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
                               .ifPresent(fornecedorSelected::set);//está funcionando normalmente
                   }

               });

            }catch (SQLException e){
IO.println("Erro on fetch data: " + e.getMessage());
            }

        });

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
                .r_child(Components.InputColumn("Data de compra", codigo,"Ex: 01/12/2026"))
                .r_child(Components.SelectColumn("Fornecedor", fornecedores, fornecedorSelected, f-> f.nome))
                .r_child(Components.InputColumn("N NF/Pedido compra", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: 12345678920"));

        final var valoresRow = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(Components.TextWithValue("Valor total:", codigo))
                .r_child(Components.TextWithValue("Desconto:", produtoEncontrado.map(p-> p != null? p.descricao: "")))
                .r_child(Components.TextWithValue("Total geral:", produtoEncontrado.map(p-> p != null? p.descricao: ""))
                );


        return new Card(new Column()
                .c_child(Components.FormTitle("Cadastrar Nova Compra"))
                .c_child(new SpacerVertical(20))
                .c_child(top)
                .c_child(new SpacerVertical(10))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(Components.InputColumn("Código", codigo,"xxxxxxxx"))
                        .r_child(Components.InputColumn("Descrição do produto",produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: Paraiso"))
                        .r_child(Components.InputColumn("Pc. de compra", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: R$ 10,00"))
                        .r_child(Components.InputColumn("Quantidade", qtd,"Ex: 2"))
                        .r_child(Components.InputColumn("Tipo de unidade", produtoEncontrado.map(p-> p != null? p.descricao: ""),"Ex: rua das graças")))
                .c_child(new SpacerVertical(10))
                .c_child(valoresRow)
        );
    }

    private void handleAdd(){
        //TODO: implementar
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
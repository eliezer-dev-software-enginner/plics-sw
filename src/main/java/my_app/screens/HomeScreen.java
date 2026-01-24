package my_app.screens;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.GridFlowProps;
import megalodonte.props.ImageProps;
import megalodonte.props.TextProps;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.utils.related.TextVariant;

import java.util.List;

public class HomeScreen {

    private final Router router;

    public HomeScreen(Router router) {
        this.router = router;
    }

    public Component render (){
        return new Column(new ColumnProps(), new ColumnStyler().bgColor("#fff"))
                .c_child(Component.CreateFromJavaFxNode(menuBar()))
                .c_child(new GridFlow(new GridFlowProps().tileSize(200, 220).centerHorizontally().spacingOf(16))
                        .items(cardItemList,this::CardColumn)
                );
    }

    private Node menuBar(){
        var menuBar = new MenuBar();
        var menu = new Menu("Preferências");
        var menuCadastros = new Menu("Cadastros");
        var menuGerencial = new Menu("Gerencial");

        MenuItem menuItemCadFornecedores = new MenuItem("Fornecedores",null);
        menuItemCadFornecedores.setOnAction(ev->  router.spawnWindow("fornecedores"));
        MenuItem menuItemClientes = new MenuItem("Clientes",null);
        menuItemClientes.setOnAction(ev-> router.spawnWindow("clientes"));
        MenuItem menuItemCategorias = new MenuItem("Categorias",null);
        menuItemCategorias.setOnAction(ev-> router.spawnWindow("categorias"));

        menuCadastros.getItems().addAll(menuItemCadFornecedores, menuItemClientes, menuItemCategorias);
        //------------Gerencial
        MenuItem menuItemEmpresa = new MenuItem("Empresa",null);
        menuItemEmpresa.setOnAction(ev-> router.spawnWindow("empresa"));
        menuGerencial.getItems().addAll(menuItemEmpresa);
       // var itemPreferences = new MenuItem("Preferências");
       // menu.getItems().add(itemPreferences);

        menuBar.getMenus().addAll(menu, menuCadastros, menuGerencial);
        return menuBar;
    }


    record CardItem(String img, String title, String desc, String destination){}
    List<CardItem> cardItemList = List.of(
            new CardItem("/assets/venda.png", "Venda (F3)","Tela de vendas","cad-produto"),
            new CardItem("/assets/ordem_servico.png", "Ordem de serviço (F5)","Tela de ordem de serviço",null),
            new CardItem("/assets/produtos.png", "Produtos (F3)","Gerencie seus produtos","produtos"),
            new CardItem("/assets/clientes.png", "Clientes","Gerencie seus clientes","clientes"),
            new CardItem("/assets/contas_a_receber.png", "Contas a receber","Tela de contas a receber",null),
            new CardItem("/assets/pdv.png", "PDV","Meu PDV",null),
            new CardItem("/assets/despesas.png", "Venda (F3)","Tela de vendas",null),
            new CardItem("/assets/compras.png", "Compras de mercadorias (F5)","Tela de compras","compras"),
            new CardItem("/assets/abertura.png", "Venda (F3)","Tela de vendas",null),
            new CardItem("/assets/relatorio.png", "Ordem de serviço (F5)","Tela de vendas",null)
    );
    Component CardColumn(CardItem cardItem){
       return new Clickable(
               new Card(
                new Column(new ColumnProps().centerHorizontally(), new ColumnStyler().bgColor("#fff"))
                        .c_child(new Image(cardItem.img, new ImageProps().size(60)))
                        .c_child(new Text(cardItem.title, new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new Text(cardItem.desc,  new TextProps().variant(TextVariant.SMALL)))
                ),
               ()-> router.spawnWindow(cardItem.destination));

    }
}

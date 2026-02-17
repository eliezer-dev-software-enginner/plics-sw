package my_app.screens;

import megalodonte.components.*;
import megalodonte.props.*;
import megalodonte.router.Router;
import megalodonte.utils.related.TextVariant;

import java.util.List;

public class HomeScreen {

    private final Router router;

    public HomeScreen(Router router) {
        this.router = router;
    }

    public Component render (){
//        GridFlow items = new GridFlow(new GridFlowProps().tileSize(200, 220)
//                .centerHorizontally().spacingOf(16))
//                .items(cardItemList, this::CardColumn);

        return new Column(new ColumnProps().bgColor("#fff"))
                .c_childs(
                        menuBar(),
                        new Column(new ColumnProps().centerHorizontally()).c_childs(
                                new Row()
                                        .r_childs(
                                                CardColumn(cardItemList.get(0)),
                                                CardColumn(cardItemList.get(1)),
                                                CardColumn(cardItemList.get(2)),
                                                CardColumn(cardItemList.get(3))
                                        ),
                                new Row()
                                        .r_childs(
                                                CardColumn(cardItemList.get(4)),
                                                CardColumn(cardItemList.get(5)),
                                                CardColumn(cardItemList.get(6))
                                        )
                        )

                );
    }

    private Component menuBar(){
        return new MenuBar()
                .menu(new Menu("Preferências").item("Abrir tela", ()-> router.spawnWindow("preferencias")))
                .menu(new Menu("Cadastros")
                        .item("Fornecedores", ()-> router.spawnWindow("fornecedores"))
                        .item("Clientes", ()-> router.spawnWindow("clientes"))
                        .item("Categorias", ()-> router.spawnWindow("categorias"))
                        .item("Produtos", ()-> router.spawnWindow("produtos"))
                )
                .menu(new Menu("Gerencial")
                        .item("Empresa", ()-> router.spawnWindow("empresa"))
                )
                .menu(new Menu("Suporte")
                        .item("Relatar erro", ()-> router.spawnWindow("relatar-erro"))
                        .item("Sugerir melhoria/funcionalidade", ()-> router.spawnWindow("sugerir-melhoria"))
                );
    }


    record CardItem(String img, String title, String desc, String destination){}
    List<CardItem> cardItemList = List.of(
            new CardItem("/assets/venda.png", "Venda (F3)","Tela de vendas","vendas"),
            new CardItem("/assets/ordem_servico.png", "Ordem de serviço (F5)","Tela de ordem de serviço","ordem-de-servico"),
            new CardItem("/assets/produtos.png", "Produtos (F3)","Gerencie seus produtos","produtos"),
            new CardItem("/assets/clientes.png", "Clientes","Gerencie seus clientes","clientes"),
            new CardItem("/assets/contas_a_receber.png", "Contas a receber","Tela de contas a receber","contas-a-receber"),
          //  new CardItem("/assets/pdv.png", "PDV","Meu PDV",null),
            new CardItem("/assets/despesas.png", "Contas a pagar","Tela de contas a pagar","contas-a-pagar"),
            new CardItem("/assets/compras.png", "Compras de mercadorias (F5)","Tela de compras","compras")
           // new CardItem("/assets/abertura.png", "Abertura de Caixa","Tela de vendas",null)
           // new CardItem("/assets/relatorio.png", "Ordem de serviço (F5)","Tela de vendas",null)
    );
    Component CardColumn(CardItem cardItem){
       return new Clickable(
               new Card(
                new Column(new ColumnProps().centerHorizontally().paddingAll(20))
                        .c_child(new Image(cardItem.img, new ImageProps().size(60)))
                        .c_child(new Text(cardItem.title, (TextProps) new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new Text(cardItem.desc,  new TextProps().variant(TextVariant.SMALL))),
                       new CardProps().padding(0).height(220).width(200).borderRadius(20)),
               ()-> router.spawnWindow(cardItem.destination)
       );
    }
}

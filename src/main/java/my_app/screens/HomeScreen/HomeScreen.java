package my_app.screens.HomeScreen;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.router.Router;

import java.util.List;

public class HomeScreen {

    private final Router router;

    public HomeScreen(Router router) {
        this.router = router;
    }

    public Component render (){
        return new Column(new ColumnProps(), new ColumnStyler().bgColor("#fff"))
                .c_child(new GridFlow(new GridFlowProps().tileSize(200, 220).centerHorizontally().spacingOf(16))
                        .items(cardItemList,this::CardColumn)
                );
    }


    record CardItem(String img, String title, String desc, String destination){}
    List<CardItem> cardItemList = List.of(
            new CardItem("/assets/venda.png", "Venda (F3)","Tela de vendas","cad-produto"),
            new CardItem("/assets/ordem_servico.png", "Ordem de serviço (F5)","Tela de vendas",null),
            new CardItem("/assets/produtos.png", "Produtos (F3)","Tela de produtos","cad-produtos/teste}"),
            new CardItem("/assets/clientes.png", "Ordem de serviço (F5)","Tela de vendas",null),
            new CardItem("/assets/contas_a_receber.png", "Venda (F3)","Tela de vendas",null),
            new CardItem("/assets/pdv.png", "Ordem de serviço (F5)","Tela de vendas",null),
            new CardItem("/assets/despesas.png", "Venda (F3)","Tela de vendas",null),
            new CardItem("/assets/compras.png", "Ordem de serviço (F5)","Tela de vendas",null),
            new CardItem("/assets/abertura.png", "Venda (F3)","Tela de vendas",null),
            new CardItem("/assets/relatorio.png", "Ordem de serviço (F5)","Tela de vendas",null)
    );
    Component CardColumn(CardItem cardItem){
        return new Column(new ColumnProps().centerHorizontally().onClick(()-> router.spawnWindow(cardItem.destination)), new ColumnStyler().bgColor("#fff").borderColor("black").borderWidth(1))
                .c_child(new Image(cardItem.img, new ImageProps().size(100)))
                .c_child(new Text(cardItem.title, new TextProps().fontSize(18).bold()))
                .c_child(new Text(cardItem.desc,  new TextProps().fontSize(16)));
    }
}

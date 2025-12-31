package my_app.screens.HomeScreen;

import megalodonte.*;

import java.util.List;

public class HomeScreen {

    private final Router router;

    public HomeScreen(Router router) {
        this.router = router;
    }

    public Component render (){
        return new Column(new ColumnProps(), new ColumnStyler().bgColor("#fff"))
                .child(new GridFlow(new GridFlowProps().tileSize(200, 220).centerHorizontally().spacingOf(16))
                        .items(cardItemList,this::CardColumn)
                );
    }


    record CardItem(String img, String title, String desc){}
    List<CardItem> cardItemList = List.of(
            new CardItem("/assets/venda.png", "Venda (F3)","Tela de vendas"),
            new CardItem("/assets/ordem_servico.png", "Ordem de serviço (F5)","Tela de vendas"),
            new CardItem("/assets/produtos.png", "Venda (F3)","Tela de vendas"),
            new CardItem("/assets/clientes.png", "Ordem de serviço (F5)","Tela de vendas"),
            new CardItem("/assets/contas_a_receber.png", "Venda (F3)","Tela de vendas"),
            new CardItem("/assets/pdv.png", "Ordem de serviço (F5)","Tela de vendas"),
            new CardItem("/assets/despesas.png", "Venda (F3)","Tela de vendas"),
            new CardItem("/assets/compras.png", "Ordem de serviço (F5)","Tela de vendas"),
            new CardItem("/assets/abertura.png", "Venda (F3)","Tela de vendas"),
            new CardItem("/assets/relatorio.png", "Ordem de serviço (F5)","Tela de vendas")
    );
    Component CardColumn(CardItem cardItem){
        return new Column(new ColumnProps().centerHorizontally(), new ColumnStyler().bgColor("#fff").borderColor("black").borderWidth(1))
                .child(new Image(cardItem.img, new ImageProps().size(100)))
                .child(new Text(cardItem.title, new TextProps().fontSize(18).bold()))
                .child(new Text(cardItem.desc,  new TextProps().fontSize(16)));
    }
}

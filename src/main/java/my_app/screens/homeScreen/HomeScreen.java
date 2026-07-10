package my_app.screens.homeScreen;

import javafx.application.Platform;
import megalodonte.base.state.State;
import megalodonte.base.Animations;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import megalodonte.v2.Show;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class HomeScreen implements ScreenComponent {

    private final HomeScreenViewModel viewModel;

    private final ScreenContext ctx;

    public HomeScreen(ScreenContext ctx) {
        this.ctx = ctx;
        this.viewModel = new HomeScreenViewModel(ctx);
    }

    private void buscarAtualizacao() {
        viewModel.update();
    }

    @Override
    public void onMount() {
        if (viewModel.isLicensaTesteExpirada()) {
            //ctx.navigate("entrar-com-credenciais");
            Platform.runLater(() -> ctx.navigate("entrar-com-credenciais"));
            return;
        }
        viewModel.calcularFinanceiroMesAtual();
    }

    public Component render (){
        var rowProps = new RowProps().spacingOf(10);
        return new Container(new ContainerProps().bgImage("/assets/home-bg.jpg")).children(
                menuBar(),
                new Container(new ContainerProps().paddingAll(10))
                        .children(
                                new Row(rowProps).children(
                                        new Column().children(
                                                financeCard("Receitas", AntDesignIconsOutlined.RISE, viewModel.receitas),
                                                financeCard("Despesas", AntDesignIconsOutlined.FALL, viewModel.despesas),
                                                financeCard("Lucro líquido", AntDesignIconsOutlined.FUND, viewModel.lucroLiquido)
                                        ),
                                        centerContent()
                                )
                        )
        );
    }

    private Column centerContent() {
        var rowProps = new RowProps().spacingOf(10);
        return new Column(new ColumnProps().spacingOf(10)).children(
                new Row(rowProps)
                        .children(
                                CardColumn(cardItemList.get(0)),
                                CardColumn(cardItemList.get(1)),
                                CardColumn(cardItemList.get(2)),
                                CardColumn(cardItemList.get(3))
                        ),
                new Row(rowProps)
                        .children(
                                CardColumn(cardItemList.get(4)),
                                CardColumn(cardItemList.get(5)),
                                CardColumn(cardItemList.get(6)),
                                CardColumn(cardItemList.get(7))
                        ),
                new Row(rowProps)
                        .children(
                                CardColumn(cardItemList.get(8)),
                              saudacaoComponent()
                        )
        );
    }

    public Component saudacaoComponent(){
        return new Column().children(
                Show.when(viewModel.gifVisible, ()->  new Image(viewModel.currentGif, new ImageProps().size(80)))
                        .withTransition(Animations::fadeSlide),
                new Text(viewModel.vendasHoje, new TextProps().variant(TextVariant.BODY).bold().textColor("#fff"))
        );
    }

    private Component financeCard(String title, Ikon ikon, State<String> valueState){
        return new Card(new Column(new ColumnProps().centerHorizontally().paddingAll(20))
                        .children(
                                Component.CreateFromJavaFxNode(FontIcon.of(ikon)),
                                new Text(title, new TextProps().variant(TextVariant.BODY).bold()),
                                new Text("do mês",  new TextProps().variant(TextVariant.SMALL)),
                                new Text(valueState, new TextProps().variant(TextVariant.SUBTITLE).bold())
                        ),
                new CardProps().padding(0).height(100).borderRadius(20).width(170)
        );
    }

    private Component menuBar(){
        return new MenuBar()
                .menu(new Menu("Preferências").item("Abrir tela", ()-> ctx.router().spawnWindow("preferencias",e->{})))
                .menu(new Menu("Cadastros")
                        .item("Fornecedores", ()-> ctx.router().spawnWindow("fornecedores",e->{}))
                        .item("Clientes", ()-> ctx.router().spawnWindow("clientes",e->{}))
                        .item("Categorias", ()-> ctx.router().spawnWindow("categorias",e->{}))
                        .item("Produtos", ()-> ctx.router().spawnWindow("produtos",e->{}))
                        .item("Técnicos", ()-> ctx.router().spawnWindow("tecnicos",e->{}))
                        //.item("Ler planilha IA", ()-> ctx.router().spawnWindow("ler-planilha-ia",e->{}))
                )
                .menu(new Menu("Gerencial")
                        .item("Empresa", ()-> ctx.router().spawnWindow("empresa",e->{}))
                )
                .menu(new Menu("Suporte")
                        .item("Relatar erro", ()-> ctx.router().spawnWindow("relatar-erro",e->{}))
                        .item("Sugerir melhoria/funcionalidade", ()-> ctx.router().spawnWindow("sugerir-melhoria",e->{}))
                        .item("Novidades dessa atualização", ()-> ctx.router().spawnWindow("info-update",e->{}))
                        .item("Buscar atualização", this::buscarAtualizacao)
                );
    }


    //TODO: HISTÓRICO DE CAICXA, COMPRAS DE MERCADORIA E CONTAS A PAGAR NÃO APARECEM
    record CardItem(String img, String title, String desc, String destination){}
    final List<CardItem> cardItemList = List.of(
            new CardItem("/assets/venda.png", "Venda","Tela de vendas","vendas"),
            new CardItem("/assets/ordem_servico.png", "Ordem de serviço","Tela de ordem de serviço","ordem-de-servico"),
            new CardItem("/assets/produtos.png", "Produtos","Gerencie seus produtos","produtos"),
            new CardItem("/assets/clientes.png", "Clientes","Gerencie seus clientes","clientes"),
            new CardItem("/assets/contas_a_receber.png", "Contas a receber","Tela de contas a receber","contas-a-receber"),
            new CardItem("/assets/pdv.png", "PDV","Abrir caixa","pdv-screen"),
            new CardItem("/assets/despesas.png", "Contas a pagar","Tela de contas a pagar","contas-a-pagar"),
            new CardItem("/assets/compras.png", "Compras de mercadorias","Tela de compras","compras"),
            new CardItem("/assets/abertura.png", "Histórico do caixa","Histórico do caixa","pedidos")
           // new CardItem("/assets/relatorio.png", "Ordem de serviço (F5)","Tela de vendas",null)
    );
    Component CardColumn(CardItem cardItem){
       return new Clickable(
               new Card(
                new Column(new ColumnProps().centerHorizontally().paddingAll(20))
                        .c_child(new Image(cardItem.img, new ImageProps().size(60)))
                        .c_child(new Text(cardItem.title, new TextProps().variant(TextVariant.BODY).bold()))
                        .c_child(new Text(cardItem.desc,  new TextProps().variant(TextVariant.SMALL))),
                       new CardProps().padding(0).height(200).width(230).borderRadius(20)),
               ()-> ctx.router().spawnWindow(cardItem.destination,e->{})
       );
    }
}

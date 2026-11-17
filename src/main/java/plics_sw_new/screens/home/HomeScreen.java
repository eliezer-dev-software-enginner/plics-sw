package plics_sw_new.screens.home;

import javafx.application.Platform;
import megalodonte.base.components.Component;
import megalodonte.base.components.IconInterface;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import org.kordamp.ikonli.javafx.FontIcon;
import plics_sw_new.AppRoutes;
import plics_sw_new.screens.home.components.AdsComponents;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class HomeScreen implements ScreenComponent {
    final HomeViewModel homeViewModel = new HomeViewModel();
    HomeAdsPane adsPane = new HomeAdsPane();

    public HomeScreen(){
        homeViewModel.init();
    }

@Override
public Component render() {
    return new Scroll(
            new Container(new ContainerProps().bgColor("#f3f7fb")).children(
                    new Column(new ColumnProps().paddingTop(24).paddingDown(18).fillWidth().fillHeight().bgColor( "#f3f7fb"))
                            .children(
                                    new Column(new ColumnProps().spacingOf(12).fillWidth().bgColor("#ffffff")).children(
                                            new Text("PAINEL DE CONTROLE", new TextProps().fontSize(12).textColor("#167a80").bold()),
                                            new Text("Bem-vindo ao Plics SW", new TextProps().fontSize(27).textColor("#18324d").bold()),
                                            new Text("Acompanhe o desempenho e acesse as rotinas do seu negócio.", new TextProps().fontSize(14).textColor("#53657e")),
                                            new Text(homeViewModel.mesAtual, new TextProps().fontSize(14).textColor( "#47647c"))
                                    ),
                                    new AdsComponents(homeViewModel.ads, homeViewModel.adsLoaded).DestaqueRow(),
                                    //adsPane.urgentComponent(),
                                    new Row(new RowProps().spacingOf(12).fillWidth()).children(
                                            new Column(new ColumnProps().paddingDown(18).bgColor("#e9f5f2").fillWidth().spacingOf(8)).children(
                                                    new Text("Receitas do mês", new TextProps().fontSize(13).textColor( "#53657e").bold()),
                                                    new Text(homeViewModel.receitas, new TextProps().fontSize(23).textColor("#18324d").bold())
                                            ),
                                            new Column(new ColumnProps().paddingDown(18).bgColor("#eef3fb").fillWidth().spacingOf(8)).children(
                                                    new Text("Despesas do mês", new TextProps().fontSize(13).textColor( "#53657e").bold()),
                                                    new Text(homeViewModel.despesas, new TextProps().fontSize(23).textColor("#18324d").bold())
                                            ),
                                            new Column(new ColumnProps().paddingDown(18).bgColor( "#eaf2fc").fillWidth().spacingOf(8)).children(
                                                    new Text("Lucro líquido", new TextProps().fontSize(13).textColor( "#53657e").bold()),
                                                    new Text(homeViewModel.lucroLiquido, new TextProps().fontSize(23).textColor("#18324d").bold())
                                            )
                                    ),
                                    new Column(new ColumnProps().paddingAll(14).bgColor("#ffffff")).c_child(
                                                    new Text(homeViewModel.vendasHoje, new TextProps().fontSize(14).textColor("#47647c"))
                                    ),
                                    new Column(new ColumnProps().spacingOf(12)).children(
                                            new Text("Acesso rápido", new TextProps().fontSize(18).textColor( "#18324d").bold()),
                                            new Text("As tarefas mais usadas, sempre à mão.", new TextProps().fontSize(13).textColor("#53657e")),
                                            new Row(new RowProps().spacingOf(10).fillWidth()).children(
                                                    new Button("Venda", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/venda.png",24)),
                                                            //.onClick(open())ctx, AppRoutes.Screens.VENDAS

                                                    new Button("Produtos", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/produtos.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Produtos", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/produtos.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Clientes", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/clientes.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Contas a receber", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/contas_a_receber.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Produtos", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/produtos.png",24))
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS

                                            ),
                                            new Row(new RowProps().spacingOf(10).fillWidth()).children(
                                                    new Button("PDV", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/pdv.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS

                                                    new Button("Categorias", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/categoria.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Fornecedores", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/fornecedores.png",24)),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Histórico do caixa", new ButtonProps().fillWidth().bgColor( "#e7f0f8").textColor("#183f60").bold())
                                                            .icon(Components.icon("/assets/abertura.png",24))
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                            )
                                    ),
                                    new Column(new ColumnProps().paddingAll(20).bgColor("#ffffff").fillWidth().spacingOf(12)).children(
                                            new Text("Gerencial e suporte", new TextProps().fontSize(18).textColor( "#18324d").bold()),
                                            new Row(new RowProps().spacingOf(10).fillWidth()).children(
                                                    new Button("Empresa", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#183f60")),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Relatórios", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#183f60")),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Preferências", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#183f60"))
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                            ),
                                            new Row(new RowProps().spacingOf(10).fillWidth()).children(
                                                    new Button("Novidades", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#183f60")),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Termos de uso", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#183f60")),
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                                    new Button("Preferências", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#183f60"))
                                                    //.onClick(open())ctx, AppRoutes.Screens.VENDAS
                                            )
                                    ),
                                    new Button("Canal de vídeos", new ButtonProps().fillWidth().bgColor( "#edf2f7").textColor("#254365")),
                                    adsPane.regularComponent()
                            )
            )
    );
    }

    private static void open(ScreenContextInterface ctx, AppRoutes.Screens screen) {
        ctx.spawnWindow(screen.name());
    }
}

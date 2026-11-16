package plics_sw_new.screens.home;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Scroll;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;

public final class HomeScreen implements ScreenComponent {
    final HomeViewModel homeViewModel = new HomeViewModel();
    HomeAdsPane adsPane = new HomeAdsPane();

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
                                    adsPane.urgentComponent(),
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
                                    )
                            )
            )
    );
    }
}

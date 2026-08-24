package my_app.screens.relatoriosScreen;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Button;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.props.FlowRowProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.components.Components;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

public class RelatoriosScreen implements ScreenComponent {

    private final RelatoriosScreenViewModel vm;

    public RelatoriosScreen(ScreenContext ctx) {
        this.vm = new RelatoriosScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.gerarRelatorio();
    }

    @Override
    public Component render() {
        return Components.ScrollPaneDefault(
                new Container(new ContainerProps().paddingAll(15).fillHeight()).children(
                        Components.FormTitle("Relatórios Financeiros"),
                        new SpacerVertical(15),
                        filtroPeriodo(),
                        new SpacerVertical(20),
                        new Row(new RowProps().spacingOf(15)).children(
                                cardReceitas(),
                                cardDespesas(),
                                cardResumo()
                        ),
                        new SpacerVertical(20),
                        cardMaisVendidos(),
                        new SpacerVertical(20),
                        cardSemVenda(),
                        new SpacerVertical(20),
                        cardDevolvidos(),
                        new SpacerVertical(20),
                        new Row(new RowProps().spacingOf(15)).children(
                                cardNovosClientes(),
                                cardNovosFornecedores()
                        ),
                        new SpacerVertical(20),
                        new FlowRow(new FlowRowProps().spacingOf(15)).children(
                                graficoComparativo(),
                                graficoPizzaReceitas(),
                                graficoPizzaDespesas(),
                                graficoPizzaFormasPagamento()
                        ),
                        new SpacerVertical(20),
                        new Button("Baixar PDF")
                                .icon(Components.ikon(AntDesignIconsOutlined.FILE_PDF, 16, "white"))
                                .onClick(vm::baixarPdf),
                        new SpacerVertical(20)
                )
        );
    }

    private Component graficoComparativo() {
        var barChart = new BarChart<String, Number>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Receitas x Despesas x Lucro");
        barChart.setLegendVisible(false);
        barChart.setPrefSize(320, 260);
        barChart.getData().add(vm.comparativoSeries);
        return Component.CreateFromJavaFxNode(barChart);
    }

    private Component graficoPizzaReceitas() {
        var pieChart = new PieChart(vm.receitasPieData);
        pieChart.setTitle("Composição das Receitas");
        pieChart.setPrefSize(320, 260);
        pieChart.setLegendVisible(false);
        return Component.CreateFromJavaFxNode(pieChart);
    }

    private Component graficoPizzaDespesas() {
        var pieChart = new PieChart(vm.despesasPieData);
        pieChart.setTitle("Composição das Despesas");
        pieChart.setPrefSize(320, 260);
        pieChart.setLegendVisible(false);
        return Component.CreateFromJavaFxNode(pieChart);
    }

    private Component graficoPizzaFormasPagamento() {
        var pieChart = new PieChart(vm.formasPagamentoPieData);
        pieChart.setTitle("Formas de pagamento mais usadas");
        pieChart.setPrefSize(320, 260);
        pieChart.setLegendVisible(false);
        return Component.CreateFromJavaFxNode(pieChart);
    }

    private Component filtroPeriodo() {
        return new Card(new Row(new RowProps().spacingOf(15).centerVertically()).children(
                Components.DatePickerColumn(vm.dataInicio, "Data início"),
                Components.DatePickerColumn(vm.dataFim, "Data fim"),
                new Button("Gerar relatório")
                        .icon(Components.ikon(AntDesignIconsOutlined.BAR_CHART, 16, "white"))
                        .onClick(vm::gerarRelatorio)
        ));
    }

    private Component cardTitulo(String titulo, Ikon icon, String color) {
        return new Row(new RowProps().spacingOf(8).centerVertically()).children(
                Component.CreateFromJavaFxNode(FontIcon.of(icon, 18, Color.web(color))),
                new Text(titulo, new TextProps().fontSize(ThemeManager.theme().typography().subtitle()).bold())
        );
    }

    private Component cardReceitas() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Receitas", AntDesignIconsOutlined.RISE, "#10b981"),
                new SpacerVertical(10),
                linha("Vendas de mercadoria", vm.receitasVendas),
                linha("Vendas no PDV", vm.receitasPedidosPdv),
                linha("Contas a receber recebidas", vm.receitasContasRecebidas),
                new SpacerVertical(10),
                linha("Total de receitas", vm.totalReceitas)
        ));
    }

    private Component cardDespesas() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Despesas", AntDesignIconsOutlined.FALL, "#ef4444"),
                new SpacerVertical(10),
                linha("Compras de mercadoria", vm.despesasCompras),
                linha("Contas a pagar pagas", vm.despesasContasPagas),
                new SpacerVertical(10),
                linha("Total de despesas", vm.totalDespesas)
        ));
    }

    private Component cardResumo() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Lucro líquido do período", AntDesignIconsOutlined.FUND, "#2563eb"),
                new Text(vm.lucroLiquido, new TextProps().fontSize(ThemeManager.theme().typography().title()).bold()),
                new SpacerVertical(15),
                new Text("Situação atual (não vinculada ao período)", new TextProps().fontSize(ThemeManager.theme().typography().small())),
                linha("Contas a receber em aberto", vm.contasReceberEmAberto),
                linha("Contas a pagar em aberto", vm.contasPagarEmAberto)
        ));
    }

    private Component cardMaisVendidos() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Produtos mais vendidos do período", AntDesignIconsOutlined.TROPHY, "#f59e0b"),
                new SpacerVertical(10),
                new Text(vm.produtosMaisVendidos, new TextProps().fontSize(ThemeManager.theme().typography().body()))
        ));
    }

    private Component cardSemVenda() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Produtos sem venda no período", AntDesignIconsOutlined.INBOX, "#6b7280"),
                new SpacerVertical(10),
                new Text(vm.produtosSemVenda, new TextProps().fontSize(ThemeManager.theme().typography().body()))
        ));
    }

    private Component cardDevolvidos() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Produtos devolvidos no período", AntDesignIconsOutlined.ROLLBACK, "#f97316"),
                new SpacerVertical(10),
                new Text(vm.produtosDevolvidos, new TextProps().fontSize(ThemeManager.theme().typography().body()))
        ));
    }

    private Component cardNovosClientes() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Novos clientes no período", AntDesignIconsOutlined.USER_ADD, "#8b5cf6"),
                new SpacerVertical(10),
                new Text(vm.novosClientes, new TextProps().fontSize(ThemeManager.theme().typography().body()))
        ));
    }

    private Component cardNovosFornecedores() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                cardTitulo("Novos fornecedores no período", AntDesignIconsOutlined.SHOP, "#0891b2"),
                new SpacerVertical(10),
                new Text(vm.novosFornecedores, new TextProps().fontSize(ThemeManager.theme().typography().body()))
        ));
    }

    private Component linha(String label, megalodonte.base.state.State<String> valor) {
        return new Row(new RowProps().spacingOf(10)).children(
                new Text(label + ":", new TextProps().fontSize(ThemeManager.theme().typography().body())),
                new Text(valor, new TextProps().fontSize(ThemeManager.theme().typography().body()).bold())
        );
    }
}

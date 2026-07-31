package my_app.screens.relatoriosScreen;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.utils.related.TextVariant;
import my_app.domain.components.Components;

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
                                cardDespesas()
                        ),
                        new SpacerVertical(15),
                        cardResumo(),
                        new SpacerVertical(20),
                        new Row(new RowProps().spacingOf(15)).children(
                                graficoComparativo(),
                                graficoPizzaReceitas(),
                                graficoPizzaDespesas()
                        ),
                        new SpacerVertical(20),
                        new Button("Baixar PDF").onClick(vm::baixarPdf),
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

    private Component filtroPeriodo() {
        return new Card(new Row(new RowProps().spacingOf(15).centerVertically()).children(
                Components.DatePickerColumn(vm.dataInicio, "Data início"),
                Components.DatePickerColumn(vm.dataFim, "Data fim"),
                new Button("Gerar relatório").onClick(vm::gerarRelatorio)
        ));
    }

    private Component cardReceitas() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                new Text("Receitas", new TextProps().variant(TextVariant.SUBTITLE).bold()),
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
                new Text("Despesas", new TextProps().variant(TextVariant.SUBTITLE).bold()),
                new SpacerVertical(10),
                linha("Compras de mercadoria", vm.despesasCompras),
                linha("Contas a pagar pagas", vm.despesasContasPagas),
                new SpacerVertical(10),
                linha("Total de despesas", vm.totalDespesas)
        ));
    }

    private Component cardResumo() {
        return new Card(new Column(new ColumnProps().fillWidth()).children(
                new Text("Lucro líquido do período", new TextProps().variant(TextVariant.SUBTITLE).bold()),
                new Text(vm.lucroLiquido, new TextProps().variant(TextVariant.TITLE).bold()),
                new SpacerVertical(15),
                new Text("Situação atual (não vinculada ao período)", new TextProps().variant(TextVariant.SMALL)),
                linha("Contas a receber em aberto", vm.contasReceberEmAberto),
                linha("Contas a pagar em aberto", vm.contasPagarEmAberto)
        ));
    }

    private Component linha(String label, megalodonte.base.state.State<String> valor) {
        return new Row(new RowProps().spacingOf(10)).children(
                new Text(label + ":", new TextProps().variant(TextVariant.BODY)),
                new Text(valor, new TextProps().variant(TextVariant.BODY).bold())
        );
    }
}

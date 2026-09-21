package my_app.screens.vendaScreen;

import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.components.IconInterface;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Card;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.FlowRowProps;
import megalodonte.props.RowProps;
import megalodonte.base.route.v2.ScreenContextInterface;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.ClienteModel;
import my_app.core.db.models.VendaModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.VendaService;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pack.utilities.CurrencyPack;

import java.sql.SQLException;

public class ScreenAddOrEditVenda extends ScreenAddOrEdit<VendaModel, VendaMercadoriaScreenViewModel> {

    public ScreenAddOrEditVenda(ScreenContextInterface screenContext) {
        super(screenContext);
        viewModel.fetchListData("add".equals(type));
    }

    @Override
    public Component render() {
        return new Card(
                new Column(new ColumnProps().spacingOf(10)).children(
                        Components.FormSubtitle("Dados da Venda"),
                        new SpacerVertical(20),
                        formFirstRow(),
                        Components.displayOperationsRow(viewModel.totais),
                        Components.TextWithValue("Total com frete:", viewModel.totalComFrete.map(CurrencyPack::toBRLCurrency)),
                        Components.aPrazoForm(viewModel.parcelas, viewModel.tipoPagamentoIsAPrazo, viewModel.totalComFrete),
                        Components.actionButton(getBtnActionText(), this::handleAddOrUpdate)
                )
        );
    }


    private FlowRow formFirstRow() {
        return new FlowRow(new FlowRowProps().spacingOf(ThemeManager.theme().spacing().sm())).children(
                Components.SelectDropDownSearch("Nome/código do produto", viewModel.codigo, "xxxxxxxx",
                        viewModel.sugestoesProduto, viewModel.produtoEncontrado, viewModel.sugestoesProdutoVisible),
                Components.DatePickerColumn(viewModel.dataVenda, "Data de venda",
                        IconInterface.of(FontIcon.of(AntDesignIconsOutlined.CALENDAR))),
                Components.SelectColumn("Cliente", viewModel.clientes, viewModel.clienteSelected, ClienteModel::getNome, true),
                disgust.io.Pack.InputColumn("N NF/Pedido compra", viewModel.numeroNota, "Ex: 12345678920"),
                Components.InputColumnDecimal("Quantidade", viewModel.qtd, "Ex: 2"),
                Pack.InputColumnCurrency("Pc. de venda", viewModel.pcVenda),
                Pack.InputColumnCurrency("Desconto em R$", viewModel.descontoEmDinheiro),
                Pack.InputColumnCurrency("Frete", viewModel.frete),
                Components.SelectColumn("Tipo de pagamento",
                        Data.tiposPagamentoList, viewModel.tipoPagamentoSelecionado, it -> it),
                Components.SelectColumn("Refletir no estoque?",
                        Data.simNaoList, viewModel.opcaoEstoqueSelected, it -> it),
                Components.TextAreaColumn("Observação", viewModel.observacao, "Alguma observação sobre esta venda?"),
                new Row(
                        new RowProps().spacingOf(ThemeManager.theme().spacing().sm())
                ).children(
                        Components.TextWithValue("Estoque anterior:", viewModel.estoqueAnterior),
                        Components.TextWithValue("Estoque após venda:", viewModel.estoqueAtual)
                )

        );
    }


    @Override
    protected VendaMercadoriaScreenViewModel getViewModel(ScreenContextInterface screenContext) {
        return new VendaMercadoriaScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<VendaModel> getService() throws SQLException {
        return new VendaService();
    }

    @Override
    protected String getTitle() {
        return "venda";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ScreenAddOrEditVenda.class);
    }
}

package my_app.screens.comprasScreen;

import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.FlowRowProps;
import megalodonte.props.RowProps;
import megalodonte.router.v5.ScreenContext;
import my_app.core.Data;
import my_app.core.ScreenAddOrEdit;
import my_app.core.components.Components;
import my_app.core.db.models.CompraModel;
import my_app.core.db.models.FornecedorModel;
import my_app.core.db.services.BaseService;
import my_app.core.db.services.CompraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ScreenAddOrEditCompras extends ScreenAddOrEdit<CompraModel, ComprasScreenViewModel> {

    public ScreenAddOrEditCompras(ScreenContext screenContext) {
        super(screenContext);
    }

    @Override
    public Component render() {
        return new Column(new ColumnProps().spacingOf(10)).children(
                Components.FormTitle("Cadastrar Nova Compra"),
                new SpacerVertical(ThemeManager.theme().spacing().lg()),
                formFirstRow(),
                formSecondRow(),
                new Row(new RowProps().spacingOf(15))
                        .r_child(Components.TextWithValue("Estoque anterior:", viewModel.estoqueAnterior))
                        .r_child(Components.TextWithValue("Estoque após compra:", viewModel.estoqueAtual)),
                Components.displayOperationsRow(viewModel.totais),
                Components.aPrazoForm(viewModel.parcelas, viewModel.tipoPagamentoSelectedIsAPrazo, viewModel.totais.totalLiquido),
                Components.actionButtons(viewModel.btnText, this::handleAddOrUpdate)
        );
    }

    private Component formFirstRow() {
        return new FlowRow(new FlowRowProps().spacingOf(10)).children(
                Components.DatePickerColumn(viewModel.dataCompra, "Data de compra"),
                Components.SelectColumn("Fornecedor", viewModel.fornecedores, viewModel.fornecedorSelected, FornecedorModel::getNome, true),
                disgust.io.Pack.InputColumn("N NF/Pedido compra", viewModel.numeroNota, "Ex: 12345678920"),
                Components.InputColumnComDynamicSearch("Código do produto", viewModel.codigo, "xxxxxxxx",
                        viewModel.sugestoesProduto, viewModel.produtoEncontrado, viewModel.sugestoesProdutoVisible),
                disgust.io.Pack.InputColumn("Descrição do produto", viewModel.produtoEncontrado.map(p -> p != null ? p.getDescricao() : ""), "Ex: Paraiso",true),
                Pack.InputColumnCurrency("Pc. de compra", viewModel.pcCompra)
        );
    }

    private Row formSecondRow() {
        Component quantidadeInput = Components.InputColumnDecimal("Quantidade", viewModel.qtd, "Ex: 1,500",viewModel.quantidadeRef);

        return new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(quantidadeInput)
                .r_child(Pack.InputColumnCurrency("Desconto em R$", viewModel.descontoEmDinheiro))
                .r_child(Components.SelectColumn("Tipo de pagamento", Data.tiposPagamentoList, viewModel.tipoPagamentoSelected, it -> it))
                .r_child(Components.SelectColumn("Refletir no estoque?",Data.simNaoList, viewModel.opcaoEstoqueSelected, it -> it))
                .r_child(Components.TextAreaColumn("Observação", viewModel.observacao, "Alguma observação sobre esta compra?"));
    }

    @Override
    protected ComprasScreenViewModel getViewModel(ScreenContext screenContext) {
        return new ComprasScreenViewModel(screenContext);
    }

    @Override
    protected BaseService<CompraModel> getService() throws SQLException {
        return new CompraService();
    }

    @Override
    protected String getTitle() {
        return "compra";
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(ScreenAddOrEditCompras.class);
    }
}

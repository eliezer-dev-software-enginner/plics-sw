package my_app.screens.vendaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.IconInterface;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.FlowRow;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.ClienteModel;
import my_app.domain.ContratoTelaCrudV3;
import my_app.domain.Data;
import my_app.domain.ViewModelScreenContract;
import my_app.domain.components.Components;
import megalodonte.components.layout_components.Row;
import my_app.utils.DateUtils;
import my_app.utils.Utils;

import my_app.db.models.VendaModel;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

public class VendaMercadoriaScreen implements ScreenComponent, ContratoTelaCrudV3<VendaModel> {
    private final VendaMercadoriaScreenViewModel vm;
    private final ScreenContext screenContext;

    public VendaMercadoriaScreen(ScreenContext ctx) {
        this.vm = new VendaMercadoriaScreenViewModel(ctx);
        this.screenContext = ctx;
    }

    @Override
    public void onMount() { vm.fetchListData(); }

    @Override
    public void onDestroy() {
        ContratoTelaCrudV3.super.onDestroy();
    }

    @Override
    public Component render() { return mainView(vm.focusState); }

    @Override
    public Component form() {
        return new Column(new ColumnProps().spacingOf(10)).children(
                Components.FormTitle("Cadastrar Nova Venda"),
                new SpacerVertical(20),
                formFirstRow(),
                Components.displayOperationsRow(vm.totais),
                Components.aPrazoForm(vm.parcelas, vm.tipoPagamentoIsAPrazo, vm.totais.totalLiquido),
                Components.actionButtons(vm.btnText, this::handleAddOrUpdate)
        );
    }

    private FlowRow formFirstRow() {
        return new FlowRow(new FlowRowProps().spacingOf(ThemeManager.theme().spacing().sm())).children(
                Components.SelectDropDownSearch("Nome/código do produto", vm.codigo, "xxxxxxxx",
                        vm.sugestoesProduto, vm.produtoEncontrado, vm.sugestoesProdutoVisible),
                Components.DatePickerColumn(vm.dataVenda, "Data de venda",
                        IconInterface.of(FontIcon.of(AntDesignIconsOutlined.CALENDAR))),
                Components.SelectColumn("Cliente", vm.clientes, vm.clienteSelected, ClienteModel::getNome, true),
                Components.InputColumn("N NF/Pedido compra", vm.numeroNota, "Ex: 12345678920"),
                Components.InputColumnDecimal("Quantidade", vm.qtd, "Ex: 2",vm.quantidadeRef),
                Components.InputColumnCurrency("Pc. de venda", vm.pcVenda),
                Components.InputColumnCurrency("Desconto em R$", vm.descontoEmDinheiro),
                Components.SelectColumn("Tipo de pagamento",
                        Data.tiposPagamentoList, vm.tipoPagamentoSelecionado, it -> it),
                Components.SelectColumn("Refletir no estoque?",
                        Data.simNaoList, vm.opcaoEstoqueSelected, it -> it),
                Components.TextAreaColumn("Observação", vm.observacao, "Alguma observação sobre esta venda?"),
                new Row(
                    new RowProps().spacingOf(ThemeManager.theme().spacing().sm())
                ).children(
                        Components.TextWithValue("Estoque anterior:", vm.estoqueAnterior),
                        Components.TextWithValue("Estoque após venda:", vm.estoqueAtual)
                )

        );
    }

    @Override
    public SimpleTable table() {
        return new SimpleTable<VendaModel>()
                .fromData(vm.filteredList)
                .header()
                .columns()
                .column("ID", VendaModel::getId)
                .imageColumn("Imagem", it -> it.getProduto().getImagem())
                .column("Produto", it -> it.getProduto().getDescricao())
                .column("Preço de venda", it -> Utils.toBRLCurrency(it.getPrecoUnitario()))
                .column("Quantidade", VendaModel::getQuantidade)
                .column("Total líquido", it -> Utils.toBRLCurrency(it.getTotalLiquido()))
                .column("Data", it -> DateUtils.localDateTimeToBrazilianDateTime(it.getDataCriacao()))
                .build()
                .onChangeFocus(vm::handleFocusChange)
                .onItemSelectChange(vm.vendaSelected::set)
                .onItemDoubleClick(it -> Components.ShowModal(itemDetails(it), this.screenContext, 550));
    }

    public Component itemDetails(VendaModel model) {
        var validade = model.getDataValidade() != null ? DateUtils.millisToBrazilianDateTime(model.getDataValidade()) : "Sem validade";
        return new Column(new ColumnProps().paddingAll(20))
                .c_child(new Text("Detalhes da venda de mercadoria", new TextProps().fontSize(ThemeManager.theme().typography().subtitle())))
                .c_child(new SpacerVertical(20))
                .c_child(Show.when(model.getProduto().getImagem()!=null,
                            ()->new Image(model.getProduto().getImagem(), new ImageProps().size(100)))
                )
                .c_child(Components.TextWithDetails("ID: ", model.getId()))
                .c_child(Components.TextWithDetails("Código do produto: ", model.getProdutoCod()))
                .c_child(Components.TextWithDetails("Nome do produto: ", model.getProduto().getDescricao()))
                .c_child(Components.TextWithDetails("Id do cliente: ", model.getCliente().getId()))
                .c_child(Components.TextWithDetails("Nome do cliente: ", model.getCliente().getNome()))
                .c_child(Components.TextWithDetails("Número da nota: ", model.getNumeroNota()))
                .c_child(Components.TextWithDetails("Tipo de pagamento: ", model.getTipoPagamento()))
                .c_child(Components.TextWithDetails("Quantidade: ", model.getQuantidade()))
                .c_child(Components.TextWithDetails("Preço de venda: ", Utils.toBRLCurrency(model.getPrecoUnitario())))
                .c_child(Components.TextWithDetails("Desconto: ", Utils.toBRLCurrency(model.getDesconto())))
                .c_child(Components.TextWithDetails("Ganho líquido: ", Utils.toBRLCurrency(model.getTotalLiquido())))
                .c_child(Components.TextWithDetails("Data de criação: ", DateUtils.localDateTimeToBrazilianDateTime(model.getDataCriacao())))
                .c_child(Components.TextWithDetails("Validade: ", validade))
                .c_child(Components.TextWithDetails("Observação: ", model.getObservacao(), true))
                .c_child(new Row(new RowProps().spacingOf(10)).children(
                        new Button("Imprimir nota de venda").onClick(() -> vm.imprimirNotaDeVenda(model)),
                        new Button("Imprimir (modo alternativo)").onClick(() -> vm.imprimirNotaDeVendaAlternativo(model))
                ));
    }

    @Override
    public ViewModelScreenContract viewModel() { return vm; }
}

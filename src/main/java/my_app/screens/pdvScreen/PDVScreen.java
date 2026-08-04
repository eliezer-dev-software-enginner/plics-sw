package my_app.screens.pdvScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.ClienteModel;
import my_app.domain.Data;
import my_app.domain.components.Components;

import java.math.BigDecimal;

public class PDVScreen implements ScreenComponent {

    private final PDVScreenViewModel vm;

    public PDVScreen(ScreenContext ctx) {
        this.vm = new PDVScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadProdutos();
        vm.loadClientes();
    }

    @Override
    public Component render() {
        var theme = ThemeManager.theme();
        return new Container(new ContainerProps().bgColor(theme.colors().surface()))
        //return new Container(new ContainerProps().bgImage("/assets/wallpapers/welcome.jpg"))
                .children(new Container(new ContainerProps().fillHeight()).children(
                        topBanner(),
                        new Row(new RowProps().spacingOf(10).paddingAll(10)).children(
                                produtoForm(),
                                table(),
                                new Column().children(
                                        Components.InputColumnCurrency("SUBTOTAL", vm.subtotal,true),
                                        Components.InputColumnCurrency("DESCONTO", vm.desconto),
                                        Components.InputColumnCurrency("TOTAL A PAGAR", vm.totalAPagar,true),
                                        new Row().children(
                                                Components.InputColumnCurrency("TOTAL RECEBIDO", vm.totalRecebido),
                                                Components.InputColumnCurrency("TROCO", vm.troco,true)
                                        ),
                                        new SpacerVertical(30),
                                        vendaFiadaComponent(),
                                        new SpacerVertical(30),
                                        new Row(new RowProps().spacingOf(10)).children(
                                                new Button("Finalizar Venda").onClick(vm::finalizarVenda),
                                                new Button("Imprimir nota de venda").onClick(vm::imprimirNota),
                                                new Button("Imprimir (modo alternativo)").onClick(vm::imprimirNotaAlternativa)
                                        )

                                )
                        ),
                        new SpacerVertical().fill(),
                        bottomBanner()
                ));
    }

    Component topBanner(){
        return new Container(new ContainerProps().bgColor("#140F2D").paddingAll(10)).children(
            Components.FormTitle("Plics SW - Meu Ponto de venda (PDV)","#fff")
        );
    }

    Component bottomBanner(){
        return new Container(new ContainerProps().bgColor("#140F2D").paddingAll(10)).children(
                //qrCode
                new Column().children(
                        new Image("/assets/qrcode_suporte.jpg", new ImageProps().size(80)),
                        new Text("Plics - SW", new TextProps().color("white").bold().fontSize(14))
                        ),
                        new SpacerVertical(10),
                        new Text("Scaneie o QRCode para ir para o suporte no WhatsApp.",
                                new TextProps().textColor("#fff").fontSize(13))

        );
    }

    Component produtoForm(){
        return new Card(
                new Column().children(
                        Components.FormTitle("Buscar produto"),
                        new SpacerVertical(5),
                        //Components.InputColumn("Código do produto", vm.codigoBarrasInput, "Ex: João"),
                        Components.SelectDropDownSearch("Código do produto", vm.codigoBarrasInput, "xxxxxxxx",
                                vm.sugestoesProduto, vm.produtoEncontrado, vm.sugestoesProdutoVisible),
                        Components.InputColumnComEnterHandler("Quantidade",
                                        vm.quantidadeInput, "Ex: 1",
                                        () -> vm.adicionarPorCodigo(vm.codigoBarrasInput.get()),
                                        vm.qtdRef
                                )
                )
        );
    }

    public Component table() {
        return new Column(new ColumnProps().fillWidth()).children(
                Components.FormTitle("LISTA DE PRODUTOS"),
                //codigo, descricao(nome), qtd, vlr. Unit., Total
                new SimpleTable<ItemVenda>()
                    .fromData(vm.itensCarrinho)
                    .header()
                        .columns()
                            .imageColumn("Imagem", it -> it.produto.getImagem())
                            .column("Cod",          it -> it.produto.getCodigoBarras())
                            .column("Nome",          it -> it.produto.getDescricao())
                            .editableColumn("Qtd.", it -> it.quantidade,
                                    (it, val) -> {
                                        try {
                                            vm.atualizarQuantidade(it, new BigDecimal(val));
                                        } catch (NumberFormatException e) {
                                            // valor inválido, ignora
                                        }
                                    })
                            .column("Vlr. Unit.", it -> it.produto.getPrecoVenda())
                            .column("Total", ItemVenda::totalItem)
                        .end()
                    .build()
        );
    }

    Component vendaFiadaComponent(){
        return new Column().children(
                Components.FormTitle("Cliente e pagamento"),
                Components.SelectColumnWithButton("Cliente", vm.clientes, vm.clienteSelected, ClienteModel::getNome,
                        true,"+ Criar cliente", vm::handleCriarCliente),
                Components.SelectColumn("Forma de pagamento", Data.tiposPagamentoList, vm.formaPagamentoSelecionado, it -> it),
                Show.when(vm.tipoPagamentoIsAPrazo, ()->
                        Components.InputColumn("Nº Parcelas", vm.numeroParcelas, "Ex: 3")
                )
        );
    }

}
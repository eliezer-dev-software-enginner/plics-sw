package my_app.screens.pdvScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;
import megalodonte.router.v4.ScreenContext;
import megalodonte.theme.ThemeManager;
import megalodonte.v2.Show;
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
                .children(new Container(new ContainerProps().fillHeight()).children(
                        topBanner(),
                        new Row(new RowProps().spacingOf(10).paddingAll(10)).children(
                                produtoForm(),
                                table(),
                                new Column().children(
                                        Components.InputColumnCurrency("SUBTOTAL", vm.subtotal,true),
                                        new Row().children(
                                                Components.InputColumnCurrency("TOTAL RECEBIDO", vm.totalRecebido),
                                                Components.InputColumnCurrency("TROCO", vm.troco,true)
                                        ),
                                        new SpacerVertical(30),
                                        vendaFiadaComponent(),
                                        new SpacerVertical(30),
                                        new Button("Finalizar Venda").onClick(vm::finalizarVenda)
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
                        Components.InputColumn("Código do produto", vm.codigoBarrasInput, "Ex: João"),
                        Components.InputColumnComEnterHandler("Quantidade",
                                        vm.quantidadeInput, "Ex: 1",
                                        () -> vm.adicionarPorCodigo(vm.codigoBarrasInput.get())
                                )
                )
        );
    }

    public Component table() {
        return new Column().children(
                Components.FormTitle("LISTA DE PRODUTOS"),
                //codigo, descricao(nome), qtd, vlr. Unit., Total
                new SimpleTable<ItemVenda>()
                    .fromData(vm.itensCarrinho)
                    .header()
                        .columns()
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
                            .column("Total", it -> it.totalItem())
                        .end()
                    .build()
        );
    }

    Component vendaFiadaComponent(){
        return new Column().children(
                new Checkbox("É uma venda fiada?",vm.isVendaFiada),
                Show.when(vm.isVendaFiada, ()-> {
                    return new Column().children(
                            Components.FormTitle("Quem é o cliente?"),
                            Components.SelectColumnWithButton("Cliente", vm.clientes, vm.clienteSelected, f -> f.getNome(),
                                    true,"+ Criar cliente", vm::handleCriarCliente)
                    );
                })
        );
    }

}
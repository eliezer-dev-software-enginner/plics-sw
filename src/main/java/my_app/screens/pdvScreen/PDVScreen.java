package my_app.screens.pdvScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.*;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.ProdutoModel;
import my_app.screens.components.Components;

import java.math.BigDecimal;

public class PDVScreen implements ScreenComponent {

    private final PDVScreenViewModel vm;

    public PDVScreen(ScreenContext ctx) {
        this.vm = new PDVScreenViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.loadProdutos();
        System.out.println("Produto loaded");
    }

    @Override
    public Component render() {
        return new Container()
                .children(Components.FormTitle("Caixa Aberto"),
                        new Row().children(
                                produtoForm(),
                                table(),
                                new Column().children(
                                        Components.InputColumn("SUBTOTAL", vm.subtotal, "0,00"),
                                        new Row().children(
                                                Components.InputColumn("TOTAL RECEBIDO", vm.totalRecebido,"0,00"),
                                                Components.InputColumn("TROCO", vm.troco,"0,00")
                                        )
                                )

                        )
                );
    }

    Component produtoForm(){
        return new Card(
                new Row().children(
                        //imagem,
                        new Column().children(
                                Components.InputColumn("Código de barras", vm.codigoBarrasInput, "Ex: João"),
                                Components.InputColumnComEnterHandler("Quantidade",
                                        vm.quantidadeInput, "Ex: 1",
                                        () -> vm.adicionarPorCodigo(vm.codigoBarrasInput.get())
                                )
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
                            .column("Cod",          it -> it.produto.codigoBarras)
                            .column("Nome",          it -> it.produto.descricao)
                            .editableColumn("Qtd.", it -> it.quantidade,
                                    (it, val) -> {
                                        try {
                                            vm.atualizarQuantidade(it, new BigDecimal(val));
                                        } catch (NumberFormatException e) {
                                            // valor inválido, ignora
                                        }
                                    })
                            .column("Vlr. Unit.", it -> it.produto.precoVenda)
                            .column("Total", it -> it.totalItem())
                        .end()
                    .build()
        );
    }
}
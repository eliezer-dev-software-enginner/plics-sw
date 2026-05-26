package my_app.screens.pdvScreen;

import my_app.db.models.ProdutoModel;

import java.math.BigDecimal;

public class ItemVenda {
    public final ProdutoModel produto;
    public BigDecimal quantidade;

    public ItemVenda(ProdutoModel produto) {
        this.produto = produto;
        this.quantidade = BigDecimal.ONE;
    }

    public BigDecimal totalItem() {
        return produto.precoVenda.multiply(quantidade);
    }
}
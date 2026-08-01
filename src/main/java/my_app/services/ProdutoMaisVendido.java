package my_app.services;

import java.math.BigDecimal;

// Produto do ranking "mais vendidos" do RelatoriosScreen — soma de quantidades
// vendidas (PDV + vendas de mercadoria) agrupadas por código de barras, com a
// descrição/unidade resolvidas da tabela produtos.
public record ProdutoMaisVendido(
        String codigoBarras,
        String descricao,
        String unidade,
        BigDecimal quantidade
) {
}

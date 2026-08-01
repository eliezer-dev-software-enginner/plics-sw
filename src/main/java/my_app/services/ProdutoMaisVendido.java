package my_app.services;

import java.math.BigDecimal;

// Item do card de produtos do RelatoriosScreen. No ranking "mais vendidos" a
// quantidade é a soma de vendas (PDV + vendas de mercadoria) por código de barras;
// no card "produtos sem venda no período" é BigDecimal.ZERO. Descrição/unidade
// resolvidas da tabela produtos.
public record ProdutoMaisVendido(
        String codigoBarras,
        String descricao,
        String unidade,
        BigDecimal quantidade
) {
}

package my_app.services;

import java.math.BigDecimal;
import java.util.List;

// Resumo das devoluções do período do RelatoriosScreen: quantas vendas/pedidos foram
// devolvidos, quantas unidades de produto voltaram no total e o detalhamento por produto
// (mesmo formato do ranking — descrição/unidade resolvidas da tabela produtos).
public record ResumoDevolucoes(
        int numeroDevolucoes,
        BigDecimal totalUnidades,
        List<ProdutoMaisVendido> produtos
) {

    public static ResumoDevolucoes vazio() {
        return new ResumoDevolucoes(0, BigDecimal.ZERO, List.of());
    }
}

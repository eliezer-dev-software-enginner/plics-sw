package my_app.services;

import java.math.BigDecimal;

// Snapshot dos totais exibidos no RelatoriosScreen — mesmos números das telas de
// origem (Home, Vendas, PDV, Compras, Contas a Pagar/Receber), só que agrupados
// pra um período arbitrário escolhido pelo usuário, em vez do mês atual fixo.
public record RelatorioDados(
        BigDecimal receitasVendas,
        BigDecimal receitasPedidosPdv,
        BigDecimal receitasContasRecebidas,
        BigDecimal despesasCompras,
        BigDecimal despesasContasPagas,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal lucroLiquido,
        BigDecimal contasReceberEmAberto,
        BigDecimal contasPagarEmAberto
) {
}

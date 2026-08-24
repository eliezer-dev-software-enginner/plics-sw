package my_app.services;

import my_app.db.models.ClienteModel;
import my_app.db.models.FornecedorModel;

import java.math.BigDecimal;
import java.util.List;

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
        BigDecimal contasPagarEmAberto,
        List<ProdutoMaisVendido> produtosMaisVendidos,
        List<ProdutoMaisVendido> produtosSemVenda,
        ResumoDevolucoes devolucoes,
        List<ClienteModel> novosClientes,
        List<FornecedorModel> novosFornecedores,
        List<FormaPagamentoValor> formasPagamento
) {
}

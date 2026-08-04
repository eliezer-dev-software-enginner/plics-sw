package my_app.services;

import java.math.BigDecimal;

// Item do card/gráfico "Formas de pagamento mais usadas" do RelatoriosScreen —
// soma do valor vendido (vendas de mercadoria + PDV) por forma de pagamento.
public record FormaPagamentoValor(
        String forma,
        BigDecimal valor
) {
}

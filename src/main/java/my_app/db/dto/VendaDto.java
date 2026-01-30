package my_app.db.dto;

import java.math.BigDecimal;

public record VendaDto(
    Long produtoId,
    Long clienteId,
    BigDecimal quantidade,
    BigDecimal precoUnitario,
    BigDecimal desconto,
    BigDecimal valorTotal,
    String formaPagamento,
    String observacao
) {
}
package my_app.db.dto;

import my_app.domain.ForeignKey;

import java.math.BigDecimal;

public record VendaDto(
    @ForeignKey String produtoCod,
    @ForeignKey Long clienteId,
    BigDecimal quantidade,
    BigDecimal precoUnitario,
    BigDecimal desconto,
    //BigDecimal valorTotal,
    String formaPagamento,
    String observacao,
    BigDecimal totalLiquido,
    Long dataValidade
) {
}
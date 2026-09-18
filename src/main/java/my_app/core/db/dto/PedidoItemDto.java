package my_app.core.db.dto;

import java.math.BigDecimal;

public record PedidoItemDto(
        Long pedidoId,
        String produtoCod,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal desconto,
        BigDecimal totalItem
) {}
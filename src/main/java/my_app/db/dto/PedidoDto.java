package my_app.db.dto;

import java.math.BigDecimal;

public record PedidoDto(
        Integer clienteId,
        String formaPagamento,
        BigDecimal totalLiquido,
        BigDecimal desconto,
        String observacao,
        boolean isFiado
) {}
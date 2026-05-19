package my_app.db.dto;

import java.math.BigDecimal;

public record PedidoDto(
        Long clienteId,
        String formaPagamento,
        BigDecimal totalLiquido,
        BigDecimal desconto,
        String observacao,
        boolean isFiado
) {}
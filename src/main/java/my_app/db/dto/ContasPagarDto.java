package my_app.db.dto;

import java.math.BigDecimal;

public record ContasPagarDto(
    String descricao,
    BigDecimal valorOriginal,
    BigDecimal valorPago,
    BigDecimal valorRestante,
    Long dataVencimento,
    Long dataPagamento,
    String status,
    Long fornecedorId,
    Long compraId,
    String numeroDocumento,
    String tipoDocumento,
    String observacao
) {}
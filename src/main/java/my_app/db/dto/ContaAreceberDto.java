package my_app.db.dto;

import my_app.domain.ForeignKey;

import java.math.BigDecimal;

public record ContaAreceberDto(
    String descricao,
    BigDecimal valorOriginal,
    BigDecimal valorRecebido,
    BigDecimal valorRestante,
    Long dataVencimento,
    Long dataRecebimento,
    String status,
    @ForeignKey Long clienteId,
    @ForeignKey Long vendaId,
    String numeroDocumento,
    String tipoDocumento,
    String observacao
) {}
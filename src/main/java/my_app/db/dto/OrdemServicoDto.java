package my_app.db.dto;

import java.math.BigDecimal;

public record OrdemServicoDto(
        Long clienteId,
        Long tecnicoId,
        String equipamento,
        BigDecimal mao_de_obra_valor,
        BigDecimal pecas_valor,
        String checklist_relatorio,
        Long data_escolhida,
        String tipoPagamento,
        String status,
        BigDecimal totalLiquido,
        Long numeroOs
) {
}

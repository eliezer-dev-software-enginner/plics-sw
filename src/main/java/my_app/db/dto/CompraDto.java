package my_app.db.dto;

import java.math.BigDecimal;

public record CompraDto(
        String produtoCod,
        BigDecimal precoCompra,
        Long fornecedorId,
        BigDecimal quantidade,
        BigDecimal descontoEmReais,
        String tipoPagamento,
        String observacao,
        long dataCompra,
        String numeroNota,
        Long dataValidade,
        BigDecimal quantidadeAnterior,
        BigDecimal estoqueAposCompra,
        String refletirEstoque
//        long dataCriacao
) {
}

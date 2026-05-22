package my_app.db.models;

import my_app.db.dto.PedidoDto;
import my_app.db.dto.PedidoItemDto;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

// PedidoModel.java
public class PedidoModel extends ModelBase<PedidoDto> {
    public Long clienteId;
    public String formaPagamento;
    public BigDecimal totalLiquido;
    public BigDecimal desconto;
    public String observacao;
    public boolean isFiado;
    public List<PedidoItemModel> itens; // composição, não vem do banco diretamente

    @Override
    public PedidoModel fromIdAndDtoAndMillis(Long id, PedidoDto dto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.clienteId = dto.clienteId();
        this.formaPagamento = dto.formaPagamento();
        this.totalLiquido = dto.totalLiquido();
        this.desconto = dto.desconto();
        this.observacao = dto.observacao();
        this.isFiado = dto.isFiado();
        return this;
    }

    @Override
    public PedidoModel fromResultSet(ResultSet rs) throws SQLException {
        var m = new PedidoModel();
        m.id = rs.getLong("id");
        m.clienteId = rs.getLong("cliente_id");
        if (rs.wasNull()) m.clienteId = null;
        m.formaPagamento = rs.getString("forma_pagamento");
        m.totalLiquido = rs.getBigDecimal("total_liquido");
        m.desconto = rs.getBigDecimal("desconto");
        m.observacao = rs.getString("observacao");
        m.isFiado = rs.getInt("is_fiado") == 1;
        m.dataCriacao = rs.getLong("data_criacao");
        return m;
    }
}

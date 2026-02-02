package my_app.db.models;

import my_app.db.dto.VendaDto;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VendaModel extends ModelBase<VendaDto> {
    public Long id;
    public Long produtoId;
    public Long clienteId;
    public BigDecimal quantidade;
    public BigDecimal precoUnitario;
    public BigDecimal totalLiquido;
    public BigDecimal desconto;
    public BigDecimal valorTotal;
    public String formaPagamento;
    public String observacao;
    public Long dataCriacao;

    // composição (domínio)
    public ProdutoModel produto;
    public ClienteModel cliente;

    @Override
    public VendaModel fromResultSet(ResultSet rs) throws SQLException {
        var v = new VendaModel();
        v.id = rs.getLong("id");
        v.produtoId = rs.getLong("produto_id");
        v.clienteId = rs.getLong("cliente_id");
        v.quantidade = rs.getBigDecimal("quantidade");
        v.precoUnitario = rs.getBigDecimal("preco_unitario");
        v.desconto = rs.getBigDecimal("desconto");
        v.valorTotal = rs.getBigDecimal("valor_total");
        v.formaPagamento = rs.getString("forma_pagamento");
        v.observacao = rs.getString("observacao");
        v.totalLiquido = rs.getBigDecimal("total_liquido");
        v.dataCriacao = rs.getLong("data_criacao");
        return v;
    }

    @Override
    public VendaModel fromIdAndDto(Long id, VendaDto dto) {
        var v = new VendaModel();
        v.id = id;
        v.produtoId = dto.produtoId();
        v.clienteId = dto.clienteId();
        v.quantidade = dto.quantidade();
        v.precoUnitario = dto.precoUnitario();
        v.desconto = dto.desconto();
        v.valorTotal = dto.valorTotal();
        v.formaPagamento = dto.formaPagamento();
        v.observacao = dto.observacao();
        v.totalLiquido = dto.totalLiquido();
        v.dataCriacao = System.currentTimeMillis();
        return v;
    }
}
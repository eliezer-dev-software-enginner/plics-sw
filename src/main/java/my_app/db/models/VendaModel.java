package my_app.db.models;

import my_app.db.dto.VendaDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VendaModel extends ModelBase<VendaDto> {
    public String produtoCod;
    @ForeignKey
    public Long clienteId;
    public BigDecimal quantidade;
    public BigDecimal desconto;
    public String tipoPagamento;
    public String observacao;
    public long dataVenda;
    public String numeroNota;
    public BigDecimal precoUnitario;
    public BigDecimal totalLiquido;
    public Long dataValidade;

    // composição (domínio)
    public ProdutoModel produto;
    public ClienteModel cliente;

    @Override
    public VendaModel fromResultSet(ResultSet rs) throws SQLException {
        var v = new VendaModel();
        v.id = rs.getLong("id");
        v.produtoCod = rs.getString("produto_cod");
        v.clienteId = rs.getLong("cliente_id");
        v.quantidade = rs.getBigDecimal("quantidade");
        v.precoUnitario = rs.getBigDecimal("preco_unitario");
        v.totalLiquido = rs.getBigDecimal("totalLiquido");
        v.desconto = rs.getBigDecimal("desconto");
        v.tipoPagamento = rs.getString("tipo_pagamento");
        v.observacao = rs.getString("observacao");
        v.dataCriacao = rs.getLong("data_criacao");
        v.dataVenda = rs.getLong("data_venda");
        v.numeroNota = rs.getString("numero_nota");
        v.dataValidade = rs.getLong("data_validade");
        return v;
    }

    @Override
    public VendaModel fromIdAndDto(Long id, VendaDto dto) {
        var v = new VendaModel();
        v.id = id;
        v.produtoCod = dto.produtoCod();
        v.clienteId = dto.clienteId();
        v.quantidade = dto.quantidade();
        v.precoUnitario = dto.precoUnitario();
        v.desconto = dto.desconto();
        v.tipoPagamento = dto.formaPagamento();
        v.observacao = dto.observacao();
        v.totalLiquido = dto.totalLiquido();
        v.dataCriacao = System.currentTimeMillis();
        return v;
    }
}
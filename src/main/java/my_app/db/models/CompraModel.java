package my_app.db.models;

import my_app.db.dto.CompraDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CompraModel extends ModelBase<CompraDto> {
    public String produtoCod;
    @ForeignKey
    public Long fornecedorId;
    public BigDecimal quantidade;
    public BigDecimal descontoEmReais;
    public String tipoPagamento;
    public String observacao;
    public long dataCompra;
    public String numeroNota;
    public BigDecimal precoDeCompra;
    public BigDecimal totalLiquido;
    public Long dataValidade;

    public FornecedorModel fornecedor;

    public CompraModel() {}

    @Override
    public CompraModel fromIdAndDtoAndMillis(Long id, CompraDto compraDto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.produtoCod = compraDto.produtoCod();
        this.fornecedorId = compraDto.fornecedorId();
        this.quantidade = compraDto.quantidade();
        this.precoDeCompra = compraDto.precoCompra();
        this.totalLiquido = compraDto.totalLiquido();
        this.descontoEmReais = compraDto.descontoEmReais();
        this.tipoPagamento = compraDto.tipoPagamento();
        this.observacao = compraDto.observacao();
        this.dataCompra = compraDto.dataCompra();
        this.numeroNota = compraDto.numeroNota();
        this.dataValidade = compraDto.dataValidade();

        return this;
    }

    @Override
    public CompraModel fromResultSet(ResultSet rs) throws SQLException {
        var model = new CompraModel();
        model.id = rs.getLong("id");
        model.produtoCod = rs.getString("codigo_barras");
        model.fornecedorId = rs.getLong("fornecedor_id");
        model.quantidade = rs.getBigDecimal("quantidade");
        model.precoDeCompra = rs.getBigDecimal("preco_compra");
        model.totalLiquido = rs.getBigDecimal("total_liquido");
        model.descontoEmReais = rs.getBigDecimal("desconto_em_reais");
        model.tipoPagamento = rs.getString("tipo_pagamento");
        model.observacao = rs.getString("observacao");
        model.dataCriacao = rs.getLong("data_criacao");
        model.dataCompra = rs.getLong("data_compra");
        model.numeroNota = rs.getString("numero_nota");
        model.dataValidade = rs.getLong("data_validade");
        if (rs.wasNull()) {
            model.dataValidade = null;
        }
        return model;
    }

}
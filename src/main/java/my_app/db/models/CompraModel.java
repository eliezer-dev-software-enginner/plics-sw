package my_app.db.models;

import my_app.db.dto.CompraDto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CompraModel extends ModelBase<CompraDto> {
    public String produtoCod;
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
    public CompraModel fromResultSet(ResultSet rs) throws SQLException {
        var model = new CompraModel();
        model.id = rs.getLong("id");
        model.produtoCod = rs.getString("produto_cod");
        model.fornecedorId = rs.getLong("fornecedor_id");
        model.quantidade = rs.getBigDecimal("quantidade");
        model.precoDeCompra = rs.getBigDecimal("preco_compra");
        model.totalLiquido = rs.getBigDecimal("total_liquido");
        model.descontoEmReais = rs.getBigDecimal("desconto_em_reais");
        model.tipoPagamento = rs.getString("tipo_pagamento");
        model.observacao = rs.getString("observacao");
        model.dataCompra = rs.getLong("data_compra");
        model.numeroNota = rs.getString("numero_nota");
        model.dataValidade = rs.getLong("data_validade");
        if (rs.wasNull()) {
            model.dataValidade = null;
        }
        
        model.dataCriacao = rs.getLong("data_criacao");
        return model;
    }

    @Override
    public CompraModel fromIdAndDto(Long id, CompraDto compraDto) {
        var model = new CompraModel();
        model.id = id;
        model.produtoCod = compraDto.produtoCod();
        model.fornecedorId = compraDto.fornecedorId();
        model.quantidade = compraDto.quantidade();
        model.precoDeCompra = compraDto.precoCompra();
        model.totalLiquido = compraDto.totalLiquido();
        model.descontoEmReais = compraDto.descontoEmReais();
        model.tipoPagamento = compraDto.tipoPagamento();
        model.observacao = compraDto.observacao();
        model.dataCompra = compraDto.dataCompra();
        model.numeroNota = compraDto.numeroNota();
        model.dataValidade = compraDto.dataValidade();
        
        //model.dataCriacao = compraDto.dataCriacao();
        return model;
    }

}
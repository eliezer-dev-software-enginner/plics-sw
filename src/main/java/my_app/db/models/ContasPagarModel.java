package my_app.db.models;

import my_app.domain.ModelBase;
import my_app.db.dto.ContasPagarDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

public class ContasPagarModel extends ModelBase<ContasPagarDto> {
    public String descricao;
    public BigDecimal valorOriginal;
    public BigDecimal valorPago;
    public BigDecimal valorRestante;
    public Long dataVencimento;
    public Long dataPagamento;
    public String status;
    public Long fornecedorId;
    public Long compraId;
    public String numeroDocumento;
    public String tipoDocumento;
    public String observacao;

    // Related objects (not stored in database)
    public FornecedorModel fornecedor;
    public CompraModel compra;

    @Override
    public ContasPagarModel fromIdAndDtoAndMillis(Long id, ContasPagarDto dto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.descricao = dto.descricao();
        this.valorOriginal = dto.valorOriginal();
        this.valorPago = dto.valorPago();
        this.valorRestante = dto.valorRestante();
        this.dataVencimento = dto.dataVencimento();
        this.dataPagamento = dto.dataPagamento();
        this.status = dto.status();
        this.fornecedorId = dto.fornecedorId();
        this.compraId = dto.compraId();
        this.numeroDocumento = dto.numeroDocumento();
        this.tipoDocumento = dto.tipoDocumento();
        this.observacao = dto.observacao();
        return this;
    }

    @Override
    public ContasPagarModel fromResultSet(ResultSet rs) throws SQLException {
        ContasPagarModel model = new ContasPagarModel();
        model.id = rs.getLong("id");
        model.descricao = rs.getString("descricao");
        model.valorOriginal = rs.getBigDecimal("valor_original");
        model.valorPago = rs.getBigDecimal("valor_pago");
        model.valorRestante = rs.getBigDecimal("valor_restante");
        model.dataVencimento = rs.getLong("data_vencimento");
        model.dataPagamento = rs.getLong("data_pagamento");
        if (rs.wasNull()) model.dataPagamento = null;
        model.status = rs.getString("status");
        model.fornecedorId = rs.getLong("fornecedor_id");
        if (rs.wasNull()) model.fornecedorId = null;
        model.compraId = rs.getLong("compra_id");
        if (rs.wasNull()) model.compraId = null;
        model.numeroDocumento = rs.getString("numero_documento");
        model.tipoDocumento = rs.getString("tipo_documento");
        model.observacao = rs.getString("observacao");
        model.dataCriacao = rs.getLong("data_criacao");
        return model;
    }

    public boolean isQuitado() {
        return "PAGO".equals(status);
    }

    public boolean isVencido() {
        if (dataPagamento != null) return false;
        return System.currentTimeMillis() > dataVencimento;
    }

    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }

    public boolean isParcial() {
        return "PARCIAL".equals(status);
    }

    @Override
    public String toString() {
        return "ContasPagarModel{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", valorOriginal=" + valorOriginal +
                ", valorRestante=" + valorRestante +
                ", dataVencimento=" + dataVencimento +
                ", status='" + status + '\'' +
                ", fornecedorId=" + fornecedorId +
                '}';
    }
}
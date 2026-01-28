package my_app.db.models;

import my_app.db.models.ModelBase;
import my_app.db.dto.ContasPagarDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

public class ContasPagarModel extends ModelBase<ContasPagarDto> {
    public Long id;
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
    public Long dataCriacao;

    // Related objects (not stored in database)
    public FornecedorModel fornecedor;
    public CompraModel compra;

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

    @Override
    public ContasPagarModel fromIdAndDto(Long id, ContasPagarDto dto) {
        ContasPagarModel model = new ContasPagarModel();
        model.id = id;
        model.descricao = dto.descricao();
        model.valorOriginal = dto.valorOriginal();
        model.valorPago = dto.valorPago();
        model.valorRestante = dto.valorRestante();
        model.dataVencimento = dto.dataVencimento();
        model.dataPagamento = dto.dataPagamento();
        model.status = dto.status();
        model.fornecedorId = dto.fornecedorId();
        model.compraId = dto.compraId();
        model.numeroDocumento = dto.numeroDocumento();
        model.tipoDocumento = dto.tipoDocumento();
        model.observacao = dto.observacao();
        model.dataCriacao = System.currentTimeMillis();
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
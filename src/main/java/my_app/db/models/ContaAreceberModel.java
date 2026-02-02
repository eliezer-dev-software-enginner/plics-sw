package my_app.db.models;

import my_app.db.dto.ContaAreceberDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContaAreceberModel extends ModelBase<ContaAreceberDto> {
    public String descricao;
    public BigDecimal valorOriginal;
    public BigDecimal valorRecebido;
    public BigDecimal valorRestante;
    public Long dataVencimento;
    public Long dataRecebimento;
    public String status;
    @ForeignKey
    public Long clienteId;
    @ForeignKey
    public Long vendaId;
    public String numeroDocumento;
    public String tipoDocumento;
    public String observacao;

    // Related objects (not stored in database)
    public FornecedorModel fornecedor;
    public CompraModel compra;

    @Override
    public ContaAreceberModel fromResultSet(ResultSet rs) throws SQLException {
        ContaAreceberModel model = new ContaAreceberModel();
        model.id = rs.getLong("id");
        model.descricao = rs.getString("descricao");
        model.valorOriginal = rs.getBigDecimal("valor_original");
        model.valorRecebido = rs.getBigDecimal("valor_recebido");
        model.valorRestante = rs.getBigDecimal("valor_restante");
        model.dataVencimento = rs.getLong("data_vencimento");
        model.dataRecebimento = rs.getLong("data_recebimento");
        if (rs.wasNull()) model.dataRecebimento = null;
        model.status = rs.getString("status");
        model.clienteId = rs.getLong("fornecedor_id");
        if (rs.wasNull()) model.clienteId = null;
        model.vendaId = rs.getLong("compra_id");
        if (rs.wasNull()) model.vendaId = null;
        model.numeroDocumento = rs.getString("numero_documento");
        model.tipoDocumento = rs.getString("tipo_documento");
        model.observacao = rs.getString("observacao");
        model.dataCriacao = rs.getLong("data_criacao");
        return model;
    }

    @Override
    public ContaAreceberModel fromIdAndDto(Long id, ContaAreceberDto dto) {
        ContaAreceberModel model = new ContaAreceberModel();
        model.id = id;
        model.descricao = dto.descricao();
        model.valorOriginal = dto.valorOriginal();
        model.valorRecebido = dto.valorRecebido();
        model.valorRestante = dto.valorRestante();
        model.dataVencimento = dto.dataVencimento();
        model.dataRecebimento = dto.dataRecebimento();
        model.status = dto.status();
        model.clienteId = dto.clienteId();
        model.vendaId = dto.vendaId();
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
        if (dataRecebimento != null) return false;
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
                ", clienteId=" + clienteId +
                '}';
    }
}
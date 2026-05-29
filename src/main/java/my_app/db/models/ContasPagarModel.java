package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.ContasPagarDto;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
@Setter
@Getter
@Table("contas_a_pagar")
public class ContasPagarModel {
    private long id;
    private long data_criacao_millis;
    private String descricao;
    public BigDecimal valorOriginal;
    private BigDecimal valorPago;
    private BigDecimal valorRestante;
    private Long dataVencimento;
    private Long dataPagamento;
    private String status;
    private Long fornecedorId;
    private Long compraId;
    private String numeroDocumento;
    private String tipoDocumento;
    private String observacao;

    // Related objects (not stored in database)
    private FornecedorModel fornecedor;
    private CompraModel compra;

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
}
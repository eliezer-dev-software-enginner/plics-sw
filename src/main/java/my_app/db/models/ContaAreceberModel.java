package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.ContaAreceberDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
@Setter
@Getter
@Table("contas_a_receber")
public class ContaAreceberModel{
    private long id;
    private long data_criacao_millis;
    private String descricao;
    private BigDecimal valorOriginal;
    private BigDecimal valorRecebido;
    private BigDecimal valorRestante;
    private Long dataVencimento;
    private Long dataRecebimento;
    private String status;
    private Long clienteId;
    private Long vendaId;
    private String numeroDocumento;
    private String tipoDocumento;
    private String observacao;

    // Related objects (not stored in database)
    private ClienteModel cliente;
    private VendaModel venda;

    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }

    public boolean isParcial() {
        return "PARCIAL".equals(status);
    }
}
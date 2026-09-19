package my_app.core.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.core.Identifier;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("contas_a_receber")
public class ContaAreceberModel extends Identifier {

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    private String descricao;

    @Column(name = "valor_original")
    private BigDecimal valorOriginal;

    @Column(name = "valor_recebido")
    private BigDecimal valorRecebido;

    @Column(name = "valor_restante")
    private BigDecimal valorRestante;

    @Column(name = "data_vencimento")
    private Long dataVencimento;

    @Column(name = "data_recebimento")
    private Long dataRecebimento;

    private String status;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "venda_id")
    private Long vendaId;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    private String observacao;

    private transient ClienteModel cliente;
    private transient VendaModel venda;
}

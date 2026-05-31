package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("contas_pagar")
public class ContasPagarModel {

    @Column(primary = true)
    private Integer id;

    private String descricao;

    @Column(name = "valor_original")
    private BigDecimal valorOriginal;

    @Column(name = "valor_pago")
    private BigDecimal valorPago;

    @Column(name = "valor_restante")
    private BigDecimal valorRestante;

    @Column(name = "data_vencimento")
    private Long dataVencimento;

    @Column(name = "data_pagamento")
    private Long dataPagamento;

    private String status;

    @Column(name = "fornecedor_id")
    private Integer fornecedorId;

    @Column(name = "compra_id")
    private Integer compraId;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    private String observacao;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    private transient FornecedorModel fornecedor;
}

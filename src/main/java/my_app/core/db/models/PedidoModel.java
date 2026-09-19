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
@Table("pedidos")
public class PedidoModel extends Identifier {

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "forma_pagamento")
    private String formaPagamento;

    @Column(name = "total_liquido")
    private BigDecimal totalLiquido;

    private BigDecimal desconto;
    private String observacao;

    @Column(name = "is_fiado")
    private Integer fiado;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    private BigDecimal frete;

    private Boolean devolvida;

    @Column(name = "data_devolucao")
    private Long dataDevolucao;
}

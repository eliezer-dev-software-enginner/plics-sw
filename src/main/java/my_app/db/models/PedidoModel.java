package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("pedidos")
public class PedidoModel {

    @Column(primary = true)
    private Integer id;

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
}

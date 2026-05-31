package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("vendas")
public class VendaModel {

    @Column(primary = true)
    private Integer id;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    @Column(name = "produto_cod")
    private String produtoCod;

    @Column(name = "cliente_id")
    private Integer clienteId;

    private BigDecimal quantidade;

    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    @Column(name = "total_liquido")
    private BigDecimal totalLiquido;

    private BigDecimal desconto;

    @Column(name = "tipo_pagamento")
    private String tipoPagamento;

    private String observacao;

    @Column(name = "data_venda")
    private Long dataVenda;

    @Column(name = "numero_nota")
    private String numeroNota;

    @Column(name = "data_validade")
    private Long dataValidade;

    private transient ProdutoModel produto;
    private transient ClienteModel cliente;
}

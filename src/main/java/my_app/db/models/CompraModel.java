package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;



@Setter
@Getter
@Table("compras")
public class CompraModel {
    @Column(primary = true)
    private long id;

    @Column(name = "dataCriacao")
    private long dataCriacaoMillis;

    @Column(name = "produto_cod")
    private String produtoCod;

    @Column(name = "fornecedor_id")
    private Long fornecedorId;

    private BigDecimal quantidade;
    private BigDecimal descontoEmReais;

    @Column(name = "tipo_pagamento")
    private String tipoPagamento;

    private String observacao;

    @Column(name = "data_compra")
    private long dataCompra;

    @Column(name = "numero_nota")
    private String numeroNota;

    @Column(name = "preco_compra")
    private BigDecimal precoDeCompra;

    @Column(name = "total_liquido")
    private BigDecimal totalLiquido;

    @Column(name = "data_validade")
    private Long dataValidade;

    private transient FornecedorModel fornecedor;
}

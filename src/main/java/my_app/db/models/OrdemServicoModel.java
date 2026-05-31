package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("ordens_de_servico")
public class OrdemServicoModel {

    @Column(primary = true)
    private Integer id;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "tecnico_id")
    private Integer tecnicoId;

    @Column(name = "numero_os")
    private Long numeroOs;

    private String equipamento;

    @Column(name = "mao_de_obra_valor")
    private BigDecimal maoDeObraValor;

    @Column(name = "pecas_valor")
    private BigDecimal pecasValor;

    @Column(name = "tipo_pagamento")
    private String tipoPagamento;

    private String status;

    @Column(name = "checklist_relatorio")
    private String checklistRelatorio;

    @Column(name = "data_escolhida")
    private Long dataEscolhida;

    @Column(name = "total_liquido")
    private BigDecimal totalLiquido;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    private transient ClienteModel cliente;
    private transient TecnicoModel tecnico;
}

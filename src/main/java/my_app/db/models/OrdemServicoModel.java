package my_app.db.models;

import my_app.db.dto.CompraDto;
import my_app.db.dto.OrdemServicoDto;
import my_app.db.dto.TecnicoDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrdemServicoModel extends ModelBase<OrdemServicoDto> {

    @SqlField(name = "cliente_id", type = "long")
    public long clienteId;

    @SqlField(name = "tecnico_id", type = "long")
    public long tecnicoId;

    @SqlField(name = "equipamento", type = "string")
    public String equipamento;

    @SqlField(name = "mao_de_obra_valor", type = "big-decimal")
    public BigDecimal maoDeObraValor;

    @SqlField(name = "pecas_valor", type = "big-decimal")
    public BigDecimal pecas_valor;

    @SqlField(name = "tipo_pagamento", type = "string")
    public String tipoPagamento;

    @SqlField(name = "status", type = "string")
    public String status;

    @SqlField(name = "checklist_relatorio", type = "string")
    public String checklistRelatorio;

    @SqlField(name = "data_escolhida", type = "long")
    public long dataEscolhida;

    @SqlField(name = "total_liquido", type = "big-decimal")
    public BigDecimal totalLiquido;

    @SqlField(name = "numero_os", type = "long")
    public long numeroOs;

    public ClienteModel cliente;
    public TecnicoModel tecnico;

    public OrdemServicoModel() {}

    @Override
    public OrdemServicoModel fromIdAndDtoAndMillis(Long id, OrdemServicoDto dto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.clienteId = dto.clienteId();
       this.tecnicoId = dto.tecnicoId();
       this.equipamento = dto.equipamento();
       this.maoDeObraValor = dto.mao_de_obra_valor();
       this.pecas_valor = dto.pecas_valor();
       this.tipoPagamento = dto.tipoPagamento();
       this.checklistRelatorio = dto.checklist_relatorio();
       this.dataEscolhida = dto.data_escolhida();
       this.totalLiquido = dto.totalLiquido();
       this.status = dto.status();
       this.numeroOs = dto.numeroOs();
        return this;
    }

}
package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.OrdemServicoDto;
import my_app.domain.ModelBase;
import my_app.domain.SqlField;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
@Setter
@Getter
@Table("ordens_de_servico")
public class OrdemServicoModel {
    private long id;
    private long data_criacao_millis;
    private long clienteId;
    private long tecnicoId;
    private String equipamento;
    private BigDecimal maoDeObraValor;

    private BigDecimal pecas_valor;
    private String tipoPagamento;
    private String status;
    private String checklistRelatorio;
    private long dataEscolhida;
    private BigDecimal totalLiquido;
    private long numeroOs;

    private ClienteModel cliente;
    private TecnicoModel tecnico;
}
package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.VendaDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Setter
@Getter
@Table("vendas")
public class VendaModel {
    private long id;
    private long data_criacao_millis;

    private String produtoCod;
    private Long clienteId;
    private BigDecimal quantidade;
    private BigDecimal desconto;
    private String tipoPagamento;
    private String observacao;
    private long dataVenda;
    private String numeroNota;
    private BigDecimal precoUnitario;
    private BigDecimal totalLiquido;
    private Long dataValidade;

    // composição (domínio)
    private ProdutoModel produto;
    private ClienteModel cliente;
}
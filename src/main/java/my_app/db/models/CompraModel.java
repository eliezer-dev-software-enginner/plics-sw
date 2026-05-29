package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.CompraDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
@Setter
@Getter
@Table("compras")
public class CompraModel {
    private long id;
    private long data_criacao_millis;
    public String produtoCod;
    @ForeignKey
    public Long fornecedorId;
    public BigDecimal quantidade;
    public BigDecimal descontoEmReais;
    public String tipoPagamento;
    public String observacao;
    public long dataCompra;
    public String numeroNota;
    public BigDecimal precoDeCompra;
    public BigDecimal totalLiquido;
    public Long dataValidade;

    public FornecedorModel fornecedor;
}
package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.ProdutoDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Setter
@Getter
@Table("produtos")
public class ProdutoModel {
    private long id;
    private long data_criacao_millis;
    //TODO: CODIGO DE BARRAS DEVERIA SER UMA TABELA, ONDE ELE É UNICO POR PRODUTO

    // ainda string, mas já pensando em normalizar depois
    private String codigoBarras;
    private String descricao;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;

    private BigDecimal totalLiquido;
    private String unidade;
    private String marca;
    //TODO: MOVER MARGEM PARA UMA TABELA PROPRIA
    //private BigDecimal margem;

    // campo derivado (não vem do banco)
    private BigDecimal lucro;

    @ForeignKey
    private Long categoriaId;
    @ForeignKey
    private Long fornecedorId;

    // composição (domínio)
    private CategoriaModel categoria;
    private FornecedorModel fornecedor;

    private BigDecimal estoque;
    private String observacoes;
    private String imagem;

    private Long validade;
    private String comissao;
    private String garantia;
}

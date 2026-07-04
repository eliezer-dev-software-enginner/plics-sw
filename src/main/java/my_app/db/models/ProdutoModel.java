package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("produtos")
public class ProdutoModel {

    @Column(primary = true)
    private Integer id;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    private String descricao;

    @Column(name = "preco_compra")
    private BigDecimal precoCompra;

    @Column(name = "preco_venda")
    private BigDecimal precoVenda;

    @Column(name = "total_liquido")
    private BigDecimal totalLiquido;

    private String unidade;
    private String marca;

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "fornecedor_id")
    private Integer fornecedorId;

    private BigDecimal estoque;
    private String observacoes;
    private String imagem;

    private String cor;
    private String tamanho;
    private String modelo;

    private Long validade;
    private String comissao;
    private String garantia;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    // transient fields (runtime composition)
    private transient my_app.db.models.CategoriaModel categoria;
    private transient my_app.db.models.FornecedorModel fornecedor;
}

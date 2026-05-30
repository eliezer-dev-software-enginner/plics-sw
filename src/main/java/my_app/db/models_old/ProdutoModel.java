package my_app.db.models_old;

import my_app.db.dto.ProdutoDto;
import my_app.domain.ForeignKey;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProdutoModel extends ModelBase<ProdutoDto> {
    //TODO: CODIGO DE BARRAS DEVERIA SER UMA TABELA, ONDE ELE É UNICO POR PRODUTO

    // ainda string, mas já pensando em normalizar depois
    public String codigoBarras;
    public String descricao;
    public BigDecimal precoCompra;
    public BigDecimal precoVenda;

    public BigDecimal totalLiquido;
    public String unidade;
    public String marca;
    //TODO: MOVER MARGEM PARA UMA TABELA PROPRIA
    //public BigDecimal margem;

    // campo derivado (não vem do banco)
    public BigDecimal lucro;

    @ForeignKey
    public Long categoriaId;
    @ForeignKey
    public Long fornecedorId;

    // composição (domínio)
    public CategoriaModel categoria;
    public FornecedorModel fornecedor;

    public BigDecimal estoque;
    public String observacoes;
    public String imagem;

    public Long validade;
    public String comissao;
    public String garantia;

    @Override
    public ProdutoModel fromIdAndDtoAndMillis(Long id, ProdutoDto dto, long millis) {
        this.id = id;
        this.dataCriacao = millis;
        this.codigoBarras = dto.codigoBarras;
        this.descricao = dto.descricao;
        this.precoCompra = dto.precoCompra;
        this.precoVenda = dto.precoVenda;
        this.unidade = dto.unidade;
        this.marca = dto.marca;
        this.categoriaId = dto.categoriaId;
        this.fornecedorId = dto.fornecedorId;
        this.estoque = dto.estoque;
        this.observacoes = dto.observacoes;
        this.imagem = dto.imagem;
        this.validade = dto.validade;
        this.comissao = dto.comissao;
        this.garantia = dto.garantia;
        this.totalLiquido = dto.totalLiquido;

        // campo derivado (runtime)
        if (this.precoCompra != null && this.precoVenda != null) {
            this.lucro = this.precoVenda.subtract(this.precoCompra);
        }

        return this;
    }

    @Override
    public ProdutoModel fromResultSet(ResultSet rs) throws SQLException {
        var p = new ProdutoModel();
        p.id = rs.getLong("id");
        p.codigoBarras = rs.getString("codigo_barras");
        p.descricao = rs.getString("descricao");
        p.precoCompra = rs.getBigDecimal("preco_compra");
        p.precoVenda = rs.getBigDecimal("preco_venda");
        //p.margem = rs.getBigDecimal("margem");
        //p.lucro = rs.getBigDecimal("lucro");
        p.unidade = rs.getString("unidade");
        p.categoriaId = rs.getLong("categoria_id");
        p.fornecedorId = rs.getLong("fornecedor_id");
        p.estoque = rs.getBigDecimal("estoque");
        p.observacoes = rs.getString("observacoes");
        p.imagem = rs.getString("imagem");
        p.marca = rs.getString("marca");
        p.validade = rs.getLong("validade");
        p.totalLiquido = rs.getBigDecimal("total_liquido");

        if (rs.wasNull()) {
            p.validade = null;
        }

        p.comissao = rs.getString("comissao");
        p.garantia = rs.getString("garantia");

        // campo derivado (runtime)
        if (p.precoCompra != null && p.precoVenda != null) {
            p.lucro = p.precoVenda.subtract(p.precoCompra);
        }

        p.dataCriacao = rs.getLong("data_criacao");
        return p;
    }

}

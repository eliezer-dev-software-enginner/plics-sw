package my_app.db.models;

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

    @Override
    public ProdutoModel fromIdAndDto(Long id, ProdutoDto dto) {
        var p = new ProdutoModel();
        p.id = id;
        p.codigoBarras = dto.codigoBarras;
        p.descricao = dto.descricao;
        p.precoCompra = dto.precoCompra;
        p.precoVenda = dto.precoVenda;
        p.unidade = dto.unidade;
        p.marca = dto.marca;
        p.categoriaId = dto.categoriaId;
        p.fornecedorId = dto.fornecedorId;
        p.estoque = dto.estoque;
        p.observacoes = dto.observacoes;
        p.imagem = dto.imagem;
        p.validade = dto.validade;
        p.comissao = dto.comissao;
        p.garantia = dto.garantia;
        p.totalLiquido = dto.totalLiquido;

        // campo derivado (runtime)
        if (p.precoCompra != null && p.precoVenda != null) {
            p.lucro = p.precoVenda.subtract(p.precoCompra);
        }

        return p;
    }
}

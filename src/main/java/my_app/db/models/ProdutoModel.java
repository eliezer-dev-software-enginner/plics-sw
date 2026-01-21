package my_app.db.models;

import my_app.db.dto.ProdutoDto;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProdutoModel implements ModelBase<ProdutoDto> {
        public Long id;
        //TODO: CODIGO DE BARRAS DEVERIA SER UMA TABELA, ONDE ELE É UNICO POR PRODUTO

        // ainda string, mas já pensando em normalizar depois
        public String codigoBarras;
        public String descricao;
        public BigDecimal precoCompra;
        public BigDecimal precoVenda;
        public String unidade;
        public String marca;
        //TODO: MOVER MARGEM PARA UMA TABELA PROPRIA
        //public BigDecimal margem;

        // campo derivado (não vem do banco)
        public BigDecimal lucro;
        //TODO: DEVE SER CATEGORIA_ID
        // FK (persistência)
        public Long categoriaId;

        // composição (domínio)
        public CategoriaModel categoria;

        public Long fornecedorId;
        public BigDecimal estoque;
        public String observacoes;
        public String imagem;


        public Long dataCriacao; // epoch millis
        public String validade;
        public String comissao;
        public String garantia;


        @Override
    public  ProdutoModel fromResultSet(ResultSet rs) throws SQLException {
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
            p.validade = rs.getString("validade");
            p.comissao = rs.getString("comissao");
            p.garantia = rs.getString("garantia");

            // campo derivado (runtime)
            if (p.precoCompra != null && p.precoVenda != null) {
                p.lucro = p.precoVenda.subtract(p.precoCompra);
            }
            return p;
        }

    @Override
    public ProdutoModel fromIdAndDto(Long id, ProdutoDto dto) {
        return null;
    }
}

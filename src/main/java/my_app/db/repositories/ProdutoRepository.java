package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.models.Models.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {

    private Connection conn() throws SQLException {
        return DB.getInstance().connection();
    }

    // CREATE
    public void salvar(Produto p) throws SQLException {
        String sql = """
            INSERT INTO produto 
            (codigo_barras, descricao, preco_compra, preco_venda, margem, lucro,
             unidade, categoria, fornecedor, estoque, observacoes, imagem)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.codigoBarras);
            ps.setString(2, p.descricao);
            ps.setBigDecimal(3, p.precoCompra);
            ps.setBigDecimal(4, p.precoVenda);
            ps.setBigDecimal(5, p.margem);
            ps.setBigDecimal(6, p.lucro);
            ps.setString(7, p.unidade);
            ps.setString(8, p.categoria);
            ps.setString(9, p.fornecedor);
            ps.setInt(10, p.estoque);
            ps.setString(11, p.observacoes);
            ps.setString(12, p.imagem);
            ps.executeUpdate();
        }
    }

    // READ
    public Produto buscarPorCodigoBarras(String codigo) throws SQLException {
        String sql = "SELECT * FROM produto WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public List<Produto> listar() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM produto");
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(Produto p) throws SQLException {
        String sql = """
            UPDATE produto SET
              descricao = ?, preco_compra = ?, preco_venda = ?, margem = ?, lucro = ?,
              unidade = ?, categoria = ?, fornecedor = ?, estoque = ?, observacoes = ?, imagem = ?
            WHERE codigo_barras = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.descricao);
            ps.setBigDecimal(2, p.precoCompra);
            ps.setBigDecimal(3, p.precoVenda);
            ps.setBigDecimal(4, p.margem);
            ps.setBigDecimal(5, p.lucro);
            ps.setString(6, p.unidade);
            ps.setString(7, p.categoria);
            ps.setString(8, p.fornecedor);
            ps.setInt(9, p.estoque);
            ps.setString(10, p.observacoes);
            ps.setString(11, p.imagem);
            ps.setString(12, p.codigoBarras);
            ps.executeUpdate();
        }
    }

    // DELETE
    public void excluir(String codigoBarras) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM produto WHERE codigo_barras = ?")) {
            ps.setString(1, codigoBarras);
            ps.executeUpdate();
        }
    }

    private Produto map(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.id = rs.getLong("id");
        p.codigoBarras = rs.getString("codigo_barras");
        p.descricao = rs.getString("descricao");
        p.precoCompra = rs.getBigDecimal("preco_compra");
        p.precoVenda = rs.getBigDecimal("preco_venda");
        p.margem = rs.getBigDecimal("margem");
        p.lucro = rs.getBigDecimal("lucro");
        p.unidade = rs.getString("unidade");
        p.categoria = rs.getString("categoria");
        p.fornecedor = rs.getString("fornecedor");
        p.estoque = rs.getInt("estoque");
        p.observacoes = rs.getString("observacoes");
        p.imagem = rs.getString("imagem");
        return p;
    }
}


package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.models.ProdutoModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {

    private Connection conn() throws SQLException {
        return DB.getInstance().connection();
    }

    // CREATE
    public void salvar(ProdutoModel p) throws SQLException {
        String sql = """
        INSERT INTO produto 
        (codigo_barras, descricao, preco_compra, preco_venda,
         unidade, categoria_id, fornecedor_id, estoque, observacoes, imagem, data_criacao)
        VALUES (?,?,?,?,?,?,?,?,?,?,?)
    """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.codigoBarras);
            ps.setString(2, p.descricao);
            ps.setBigDecimal(3, p.precoCompra);
            ps.setBigDecimal(4, p.precoVenda);
            ps.setString(5, p.unidade);
            ps.setLong(6, p.categoriaId);
            ps.setLong(7, p.fornecedorId);
            ps.setInt(8, p.estoque);
            ps.setString(9, p.observacoes);
            ps.setString(10, p.imagem);
            ps.setLong(11, p.dataCriacao != null ? p.dataCriacao : System.currentTimeMillis());
            ps.executeUpdate();
        }
    }


    // READ
    public ProdutoModel buscarPorCodigoBarras(String codigo) throws SQLException {
        String sql = "SELECT * FROM produto WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? ProdutoModel.fromResultSet(rs) : null;
        }
    }

    public ProdutoModel buscarPorCodigoBarrasComCategoria(String codigo) throws SQLException {
        ProdutoModel produto = buscarPorCodigoBarras(codigo);

        if (produto == null || produto.categoriaId == null) {
            return produto;
        }

        CategoriaRepository categoriaRepo = new CategoriaRepository();
        produto.categoria = categoriaRepo.buscarById(produto.categoriaId);

        return produto;
    }


    public List<ProdutoModel> listar() throws SQLException {
        var lista = new ArrayList<ProdutoModel>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM produto");
            while (rs.next()) lista.add(ProdutoModel.fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(ProdutoModel p) throws SQLException {
        String sql = """
        UPDATE produto SET
          descricao = ?, preco_compra = ?, preco_venda = ?,
          unidade = ?, categoria_id = ?, fornecedor_id = ?,
          estoque = ?, observacoes = ?, imagem = ?
        WHERE codigo_barras = ?
    """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.descricao);
            ps.setBigDecimal(2, p.precoCompra);
            ps.setBigDecimal(3, p.precoVenda);
            ps.setString(4, p.unidade);
            ps.setLong(5, p.categoriaId);
            ps.setLong(6, p.fornecedorId);
            ps.setInt(7, p.estoque);
            ps.setString(8, p.observacoes);
            ps.setString(9, p.imagem);
            ps.setString(10, p.codigoBarras);
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
}


package my_app.db.repositories;

import my_app.db.dto.ProdutoDto;
import my_app.db.models.ProdutoModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository extends BaseRepository<ProdutoDto, ProdutoModel> {
    // READ
    public ProdutoModel buscarPorCodigoBarras(String codigo) throws SQLException {
        String sql = "SELECT * FROM produtos WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? new ProdutoModel().fromResultSet(rs) : null;
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


    @Override
    public ProdutoModel salvar(ProdutoDto p) throws SQLException {
        String sql = """
                INSERT INTO produtos 
                (codigo_barras, descricao, preco_compra, preco_venda,
                 unidade, categoria_id, fornecedor_id, estoque, observacoes, 
                 imagem, data_criacao, marca, validade, comissao, garantia)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.codigoBarras);
            ps.setString(2, p.descricao);
            ps.setBigDecimal(3, p.precoCompra);
            ps.setBigDecimal(4, p.precoVenda);
            ps.setString(5, p.unidade);
            ps.setLong(6, p.categoriaId);
            ps.setLong(7, p.fornecedorId);
            ps.setBigDecimal(8, p.estoque);
            ps.setString(9, p.observacoes);
            ps.setString(10, p.imagem);
            ps.setLong(11, System.currentTimeMillis());
            ps.setString(12, p.marca);
            ps.setString(13, p.validade);
            ps.setString(14, p.comissao);
            ps.setString(15, p.garantia);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return new ProdutoModel().fromIdAndDto(id, p);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    public List<ProdutoModel> listar() throws SQLException {
        var lista = new ArrayList<ProdutoModel>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM produtos");
            while (rs.next()) lista.add(new ProdutoModel().fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(ProdutoModel p) throws SQLException {
        String sql = """
                    UPDATE produtos SET
                      descricao = ?, preco_compra = ?, preco_venda = ?,
                      unidade = ?, categoria_id = ?, fornecedor_id = ?,
                      estoque = ?, observacoes = ?, imagem = ?,
                      marca = ?, validade = ?,
                      comissao = ?, garantia = ?
                    WHERE codigo_barras = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.descricao);
            ps.setBigDecimal(2, p.precoCompra);
            ps.setBigDecimal(3, p.precoVenda);
            ps.setString(4, p.unidade);
            ps.setLong(5, p.categoriaId);
            ps.setLong(6, p.fornecedorId);
            ps.setBigDecimal(7, p.estoque);
            ps.setString(8, p.observacoes);
            ps.setString(9, p.imagem);
            ps.setString(10, p.marca);
            ps.setString(11, p.validade);
            ps.setString(12, p.comissao);
            ps.setString(13, p.garantia);
            ps.setString(14, p.codigoBarras);
            ps.executeUpdate();
        }
    }

    @Override
    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM produtos WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected ProdutoModel buscarById(Long id) throws SQLException {
        return null;
    }
}


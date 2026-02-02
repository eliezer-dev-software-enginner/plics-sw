package my_app.db.repositories;

import my_app.db.dto.ProdutoDto;
import my_app.db.models.ProdutoModel;

import java.math.BigDecimal;
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
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? new ProdutoModel().fromResultSet(rs) : null;
        }
    }

    /**
     * Atualiza o estoque de um produto somando ou subtraindo a quantidade informada
     * @param codigoBarras Código de barras do produto
     * @param quantidade Quantidade a ser adicionada (positiva) ou subtraída (negativa)
     * @throws SQLException Em caso de erro na operação
     */
    public void atualizarEstoque(String codigoBarras, BigDecimal quantidade) throws SQLException {
        // Primeiro busca o produto para obter o estoque atual
        ProdutoModel produto = buscarPorCodigoBarras(codigoBarras);
        if (produto == null) {
            throw new SQLException("Produto não encontrado: " + codigoBarras);
        }

        // Calcula novo estoque
        BigDecimal novoEstoque = produto.estoque.add(quantidade);
        
        // Garante que estoque não fique negativo
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ficar negativo. Estoque atual: " + produto.estoque + ", Tentativa de subtrair: " + quantidade.abs());
        }

        String sql = "UPDATE produtos SET estoque = ? WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, novoEstoque);
            ps.setString(2, codigoBarras);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao atualizar estoque. Produto não encontrado: " + codigoBarras);
            }
        }
    }

    /**
     * Define o estoque de um produto para um valor específico
     * @param codigoBarras Código de barras do produto
     * @param novoEstoque Novo valor do estoque
     * @throws SQLException Em caso de erro na operação
     */
    public void definirEstoque(String codigoBarras, BigDecimal novoEstoque) throws SQLException {
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ser negativo: " + novoEstoque);
        }

        String sql = "UPDATE produtos SET estoque = ? WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, novoEstoque);
            ps.setString(2, codigoBarras);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao definir estoque. Produto não encontrado: " + codigoBarras);
            }
        }
    }

    public void incrementarEstoque(String prodCod, BigDecimal quantidade) throws SQLException {
        if (quantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ser negativo: " + quantidade);
        }

        var produto = this.buscarPorCodigoBarras(prodCod);
        if(produto == null) throw new NullPointerException("Produto não encontrado para o código: " + prodCod);

        var estoqueAtual = produto.estoque;
        var novoEstoque = estoqueAtual.add(quantidade);

        String sql = "UPDATE produtos SET estoque = ? WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, novoEstoque);
            ps.setString(2, prodCod);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Falha ao definir estoque. Produto não encontrado: " + prodCod);
            }
        }
    }

    public void decrementarEstoque(String prodCod, BigDecimal quantidade) throws SQLException {
        if (quantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ser negativo: " + quantidade);
        }

        var produto = this.buscarPorCodigoBarras(prodCod);
        if(produto == null) throw new NullPointerException("Produto não encontrado para o código: " + prodCod);

        var estoqueAtual = produto.estoque;
        var novoEstoque = estoqueAtual.subtract(quantidade);

        String sql = "UPDATE produtos SET estoque = ? WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, novoEstoque);
            ps.setString(2, prodCod);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Falha ao definir estoque. Produto não encontrado: " + prodCod);
            }
        }
    }
}


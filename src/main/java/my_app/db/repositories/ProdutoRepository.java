package my_app.db.repositories;

import my_app.db.models.ProdutoModel;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class ProdutoRepository extends BaseRepository<ProdutoModel> {

    public ProdutoRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<ProdutoModel> modelClass() {
        return ProdutoModel.class;
    }

    public ProdutoModel buscarPorCodigoBarras(String codigoBarras) throws SQLException {
        return session().fetch(
                modelClass(),
                sql("SELECT * FROM produtos WHERE codigo_barras = ?"),
                params(codigoBarras)
        );
    }

    public void atualizarEstoque(String codigoBarras, BigDecimal quantidade) throws SQLException {
        var produto = buscarPorCodigoBarras(codigoBarras);
        if (produto == null) throw new SQLException("Produto não encontrado: " + codigoBarras);

        var novoEstoque = produto.getEstoque().add(quantidade);
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ficar negativo. Estoque atual: " +
                    produto.getEstoque() + ", Tentativa de subtrair: " + quantidade.abs());
        }

        produto.setEstoque(novoEstoque);
        session().update(produto);
    }

    public void definirEstoque(String codigoBarras, BigDecimal novoEstoque) throws SQLException {
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ser negativo: " + novoEstoque);
        }

        var produto = buscarPorCodigoBarras(codigoBarras);
        if (produto == null) throw new SQLException("Produto não encontrado: " + codigoBarras);

        produto.setEstoque(novoEstoque);
        session().update(produto);
    }

    public void incrementarEstoque(String codigoBarras, BigDecimal quantidade) throws SQLException {
        if (quantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Quantidade não pode ser negativa: " + quantidade);
        }

        var produto = buscarPorCodigoBarras(codigoBarras);
        if (produto == null) throw new SQLException("Produto não encontrado: " + codigoBarras);

        var novoEstoque = produto.getEstoque().add(quantidade);
        produto.setEstoque(novoEstoque);
        session().update(produto);
    }

    public void decrementarEstoque(String codigoBarras, BigDecimal quantidade) throws SQLException {
        if (quantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Quantidade não pode ser negativa: " + quantidade);
        }

        var produto = buscarPorCodigoBarras(codigoBarras);
        if (produto == null) throw new SQLException("Produto não encontrado: " + codigoBarras);

        var novoEstoque = produto.getEstoque().subtract(quantidade);
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new SQLException("Estoque não pode ficar negativo. Estoque atual: " +
                    produto.getEstoque() + ", Tentativa de subtrair: " + quantidade);
        }

        produto.setEstoque(novoEstoque);
        session().update(produto);
    }
}

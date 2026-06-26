package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.ProdutoRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProdutoService extends BaseService<ProdutoModel> {

    private final ProdutoRepository produtoRepository;

    public ProdutoService() throws SQLException {
        this(DB.getPersismSession());
    }

    public ProdutoService(Session session) {
        super(new ProdutoRepository(session));
        this.produtoRepository = (ProdutoRepository) repository;
    }

    @Override
    public ProdutoModel salvar(ProdutoModel model) throws SQLException {
        validar(model);

        var produtoModel = produtoRepository.buscarPorCodigoBarras(model.getCodigoBarras().trim());
        if(produtoModel != null)throw new IllegalArgumentException("Código de barras já cadastrado");
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(ProdutoModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
    }

    private void validar(ProdutoModel model) {
        if (model.getCodigoBarras() == null || model.getCodigoBarras().isBlank())
            throw new IllegalArgumentException("Código de barras/SKU é obrigatório");
        if (model.getDescricao() == null || model.getDescricao().isBlank())
            throw new IllegalArgumentException("Adicione descrição ao produto");
        if (model.getUnidade() == null || model.getUnidade().isBlank())
            throw new IllegalArgumentException("Adicione Unidade ao produto");
        if (model.getFornecedorId() == null || model.getFornecedorId() <= 0)
            throw new IllegalArgumentException("Fornecedor não encontrado");
    }

    public ProdutoModel buscarPorCodigoBarras(String codigoBarras) throws SQLException {
        return produtoRepository.buscarPorCodigoBarras(codigoBarras);
    }

    public void atualizarEstoque(String codigoBarras, java.math.BigDecimal quantidade) throws SQLException {
        produtoRepository.atualizarEstoque(codigoBarras, quantidade);
    }

    public void definirEstoque(String codigoBarras, java.math.BigDecimal novoEstoque) throws SQLException {
        produtoRepository.definirEstoque(codigoBarras, novoEstoque);
    }

    public void incrementarEstoque(String codigoBarras, java.math.BigDecimal quantidade) throws SQLException {
        produtoRepository.incrementarEstoque(codigoBarras, quantidade);
    }

    public void decrementarEstoque(String codigoBarras, java.math.BigDecimal quantidade) throws SQLException {
        produtoRepository.decrementarEstoque(codigoBarras, quantidade);
    }
}

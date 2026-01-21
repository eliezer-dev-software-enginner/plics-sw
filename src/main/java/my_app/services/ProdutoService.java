package my_app.services;

import my_app.db.dto.ProdutoDto;
import my_app.db.models.ProdutoModel;
import my_app.db.repositories.ProdutoRepository;

import java.math.BigDecimal;

public class ProdutoService {
    private final ProdutoRepository repo = new ProdutoRepository();

    public void salvar(ProdutoDto p) throws Exception {
        if(p.codigoBarras.trim().isEmpty()) throw new RuntimeException("Adicione código ao produto");

        if (repo.buscarPorCodigoBarras(p.codigoBarras) != null) {
            throw new IllegalStateException("Produto já existe");
        }

        if(p.descricao.trim().isEmpty()) throw new RuntimeException("Adicione descrição ao produto");
        if(p.unidade.trim().isEmpty()) throw new RuntimeException("Adicione Unidade ao produto");
//        if(p.precoCompra.trim().isEmpty()) throw new RuntimeException("Adicione preço de compra");
//        if(p.precoVenda.trim().isEmpty()) throw new RuntimeException("Adicione preço de venda");
        if(p.fornecedorId <= 0) throw new RuntimeException("Fornecedor não encontrado");

        repo.salvar(p);
    }

    public void atualizar(ProdutoModel p) throws Exception {
        if (repo.buscarPorCodigoBarras(p.codigoBarras) == null) {
            throw new IllegalStateException("Produto não encontrado");
        }
        repo.atualizar(p);
    }

    public ProdutoModel buscar(String codigoBarras) throws Exception {
        return repo.buscarPorCodigoBarras(codigoBarras);
    }

    public void excluir(Long id) throws Exception {
        repo.excluirById(id);
    }
}

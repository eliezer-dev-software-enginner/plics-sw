package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.VendaModel;
import my_app.db.repositories.VendaRepository;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class VendaService extends BaseService<VendaModel> {

    private final VendaRepository vendaRepository;
    private final ProdutoService produtoService;

    public VendaService() throws SQLException {
        this(DB.getPersismSession());
    }

    public VendaService(Session session) {
        super(new VendaRepository(session));
        this.vendaRepository = (VendaRepository) repository;
        this.produtoService = new ProdutoService(session);
    }

    public VendaModel salvar(VendaModel model, boolean atualizarEstoque) throws SQLException {
        validar(model);
        model.setDataCriacao(LocalDateTime.now());
        model.setAfetaEstoque(atualizarEstoque);

        if (atualizarEstoque) {
            var produto = produtoService.buscarPorCodigoBarras(model.getProdutoCod());
            if (produto == null) throw new SQLException("Produto não encontrado: " + model.getProdutoCod());
            var estoqueApos = produto.getEstoque().subtract(model.getQuantidade());
            if (estoqueApos.compareTo(BigDecimal.ZERO) < 0) {
                throw new SQLException("Estoque não pode ficar negativo. Estoque atual: " +
                        produto.getEstoque() + ", Tentativa de subtrair: " + model.getQuantidade());
            }
        }

        var salvo = repository.salvar(model);

        if (atualizarEstoque) {
            produtoService.decrementarEstoque(model.getProdutoCod(), model.getQuantidade());
        }

        return salvo;
    }

    @Override
    public void atualizar(VendaModel model) throws SQLException {
        atualizar(model, false);
    }

    /**
     * @param atualizarEstoque se esta venda, DAQUI PRA FRENTE, deve refletir no estoque.
     *                         Comparado com o afetaEstoque JÁ PERSISTIDO da venda (não com
     *                         nada vindo do formulário) pra decidir o ajuste real: devolver,
     *                         descontar, ajustar pela diferença de quantidade, ou nada.
     */
    public void atualizar(VendaModel model, boolean atualizarEstoque) throws SQLException {
        validar(model);

        var vendaAnterior = repository.buscarById(model.getId());
        if (vendaAnterior == null) throw new IllegalArgumentException("Venda não encontrada");

        boolean afetavaAntes = Boolean.TRUE.equals(vendaAnterior.getAfetaEstoque());
        boolean mesmoProduto = vendaAnterior.getProdutoCod().equals(model.getProdutoCod());
        boolean ajustePorDelta = afetavaAntes && atualizarEstoque && mesmoProduto;

        // Validar ANTES de gravar: se vai faltar estoque, nada deve ser persistido.
        if (ajustePorDelta) {
            var delta = model.getQuantidade().subtract(vendaAnterior.getQuantidade());
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                var produto = produtoService.buscarPorCodigoBarras(model.getProdutoCod());
                if (produto == null) throw new SQLException("Produto não encontrado: " + model.getProdutoCod());
                if (produto.getEstoque().subtract(delta).compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Estoque não pode ficar negativo. Estoque atual: " +
                            produto.getEstoque() + ", Tentativa de subtrair: " + delta);
                }
            }
        } else if (atualizarEstoque && !(afetavaAntes && mesmoProduto)) {
            // passou a afetar (ou trocou de produto): vai descontar a quantidade cheia do produto novo
            var novoProduto = produtoService.buscarPorCodigoBarras(model.getProdutoCod());
            if (novoProduto == null) throw new SQLException("Produto não encontrado: " + model.getProdutoCod());
            if (novoProduto.getEstoque().subtract(model.getQuantidade()).compareTo(BigDecimal.ZERO) < 0) {
                throw new SQLException("Estoque não pode ficar negativo. Estoque atual: " +
                        novoProduto.getEstoque() + ", Tentativa de subtrair: " + model.getQuantidade());
            }
        }

        model.setAfetaEstoque(atualizarEstoque);
        repository.atualizar(model);

        if (ajustePorDelta) {
            var delta = model.getQuantidade().subtract(vendaAnterior.getQuantidade());
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                produtoService.decrementarEstoque(model.getProdutoCod(), delta);
            } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
                produtoService.incrementarEstoque(model.getProdutoCod(), delta.negate());
            }
        } else {
            if (afetavaAntes) {
                produtoService.incrementarEstoque(vendaAnterior.getProdutoCod(), vendaAnterior.getQuantidade());
            }
            if (atualizarEstoque) {
                produtoService.decrementarEstoque(model.getProdutoCod(), model.getQuantidade());
            }
        }
    }

    public void excluir(long id) throws SQLException {
        var venda = repository.buscarById(id);
        if (venda == null) throw new IllegalArgumentException("Venda não encontrada");

        if (Boolean.TRUE.equals(venda.getAfetaEstoque())) {
            produtoService.incrementarEstoque(venda.getProdutoCod(), venda.getQuantidade());
        }

        repository.excluirById(id);
    }

    public BigDecimal somarVendasHoje() throws SQLException {
        return vendaRepository.somarVendasHoje();
    }

    public BigDecimal somarVendasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return vendaRepository.somarVendasPorPeriodo(dataInicio, dataFim);
    }

    public java.util.List<VendaModel> listarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return vendaRepository.listarPorPeriodo(dataInicio, dataFim);
    }

    public java.util.List<VendaModel> buscarPorCliente(Integer clienteId) throws SQLException {
        return vendaRepository.buscarPorCliente(clienteId);
    }

    private void validar(VendaModel model) {
        if (model.getProdutoCod() == null || model.getProdutoCod().trim().isEmpty())
            throw new IllegalArgumentException("Produto é obrigatório");
        if (model.getClienteId() == null)
            throw new IllegalArgumentException("Cliente é obrigatório");
        if (model.getQuantidade() == null || model.getQuantidade().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        if (model.getPrecoUnitario() == null || model.getPrecoUnitario().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero");
    }
}

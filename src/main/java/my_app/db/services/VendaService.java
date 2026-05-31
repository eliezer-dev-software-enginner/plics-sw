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
        var salvo = repository.salvar(model);

        if (atualizarEstoque) {
            produtoService.decrementarEstoque(model.getProdutoCod(), model.getQuantidade());
        }

        return salvo;
    }

    @Override
    public void atualizar(VendaModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
    }

    public void excluir(long id, boolean devolverEstoque) throws SQLException {
        var venda = repository.buscarById(id);
        if (venda == null) throw new IllegalArgumentException("Venda não encontrada");

        if (devolverEstoque) {
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

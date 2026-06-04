package my_app.services;

import my_app.db.models.CompraModel;
import my_app.db.models.ContasPagarModel;
import my_app.db.services.CompraService;
import my_app.db.services.FornecedorService;
import my_app.domain.Parcela;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ContasPagarService {
    private final my_app.db.services.ContasPagarService repo;
    private final CompraService compraService;
    private final FornecedorService fornecedorService;

    public ContasPagarService() throws SQLException {
        this.repo = new my_app.db.services.ContasPagarService();
        this.compraService = new CompraService();
        this.fornecedorService = new FornecedorService();
    }

    public void salvar(ContasPagarModel conta) throws SQLException {
        repo.salvar(conta);
    }

    public List<ContasPagarModel> listar() throws SQLException {
        return repo.listar();
    }

    public ContasPagarModel buscarPorId(Long id) throws SQLException {
        return repo.buscarById(id);
    }

    public void atualizar(ContasPagarModel conta) throws SQLException {
        repo.atualizar(conta);
    }

    public void excluir(Long id) throws SQLException {
        repo.excluir(id);
    }

    public void registrarPagamento(Long id, BigDecimal valorPago) throws SQLException {
        repo.registrarPagamento(id, valorPago);
    }

    public void cancelarPagamento(Long id) throws SQLException {
        repo.cancelarPagamento(id);
    }

    public List<ContasPagarModel> buscarPorFornecedor(Long fornecedorId) throws SQLException {
        return repo.buscarPorFornecedor(fornecedorId != null ? fornecedorId.intValue() : null);
    }

    public List<ContasPagarModel> buscarPorStatus(String status) throws SQLException {
        return repo.buscarPorStatus(status);
    }

    public List<ContasPagarModel> buscarVencidas() throws SQLException {
        return repo.buscarVencidas();
    }

    public List<ContasPagarModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return repo.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<ContasPagarModel> buscarPorCompra(Long compraId) throws SQLException {
        return repo.buscarPorCompra(compraId != null ? compraId.intValue() : null);
    }

    public void gerarContasDeCompra(CompraModel compra, List<Parcela> parcelas) throws SQLException {
        if (compra == null) {
            throw new IllegalStateException("Compra inválida");
        }

        if (parcelas == null || parcelas.isEmpty()) {
            throw new IllegalStateException("Parcelas não informadas");
        }

        for (Parcela parcela : parcelas) {
            var model = new ContasPagarModel();
            model.setDescricao("Parcela " + parcela.numero() + " - Compra #" + compra.getId());
            model.setValorOriginal(parcela.valor());
            model.setValorPago(BigDecimal.ZERO);
            model.setValorRestante(parcela.valor());
            model.setDataVencimento(parcela.dataVencimento());
            model.setStatus("PENDENTE");
            model.setFornecedorId(compra.getFornecedorId());
            model.setCompraId((int) compra.getId());
            model.setNumeroDocumento("PARC/" + parcela.numero());
            model.setTipoDocumento("DUPLICATA");
            model.setObservacao("Gerado automaticamente da compra #" + compra.getId());
            repo.salvar(model);
        }
    }

    public BigDecimal getTotalEmAberto() throws SQLException {
        return repo.getTotalEmAberto();
    }

    public BigDecimal getTotalVencidas() throws SQLException {
        return repo.getTotalVencidas();
    }

    public BigDecimal getTotalPagoNoMes(Long dataInicioMes, Long dataFimMes) throws SQLException {
        return BigDecimal.ZERO;
    }

    public void excluirPorCompraId(Long compraId) throws SQLException {
        repo.excluirPorCompraId(compraId != null ? compraId.intValue() : null);
    }
}

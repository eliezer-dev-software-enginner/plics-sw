package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.ContasPagarModel;
import my_app.db.repositories.ContasPagarRepository;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ContasPagarService extends BaseService<ContasPagarModel> {

    private final ContasPagarRepository contasPagarRepository;

    public ContasPagarService() throws SQLException {
        this(DB.getPersismSession());
    }

    public ContasPagarService(Session session) {
        super(new ContasPagarRepository(session));
        this.contasPagarRepository = (ContasPagarRepository) repository;
    }

    @Override
    public ContasPagarModel salvar(ContasPagarModel model) throws SQLException {
        validar(model, true);
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(ContasPagarModel model) throws SQLException {
        validar(model, false);
        repository.atualizar(model);
    }

    public void excluir(long id) throws SQLException {
        var conta = repository.buscarById(id);
        if (conta == null) throw new IllegalArgumentException("Conta a pagar não encontrada");
        if ("PAGO".equals(conta.getStatus()))
            throw new IllegalArgumentException("Não é possível excluir contas já pagas");
        repository.excluirById(id);
    }

    public void registrarPagamento(long id, BigDecimal valorPago) throws SQLException {
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Valor pago deve ser maior que zero");

        var conta = repository.buscarById(id);
        if (conta == null) throw new IllegalArgumentException("Conta a pagar não encontrada");
        if ("PAGO".equals(conta.getStatus()))
            throw new IllegalArgumentException("Esta conta já está paga");
        if (valorPago.compareTo(conta.getValorRestante()) > 0)
            throw new IllegalArgumentException("Valor pago não pode ser maior que o valor restante");

        var novoValorPago = conta.getValorPago().add(valorPago);
        var novoValorRestante = conta.getValorRestante().subtract(valorPago);

        String novoStatus;
        if (novoValorRestante.compareTo(BigDecimal.ZERO) <= 0) {
            novoStatus = "PAGO";
            novoValorRestante = BigDecimal.ZERO;
        } else {
            novoStatus = "PARCIAL";
        }

        conta.setValorPago(novoValorPago);
        conta.setValorRestante(novoValorRestante);
        conta.setStatus(novoStatus);
        conta.setDataPagamento(System.currentTimeMillis());

        repository.atualizar(conta);
    }

    public void cancelarPagamento(long id) throws SQLException {
        var conta = repository.buscarById(id);
        if (conta == null) throw new IllegalArgumentException("Conta a pagar não encontrada");

        var status = conta.getStatus();
        if (!"PAGO".equals(status) && !"PARCIAL".equals(status))
            throw new IllegalArgumentException("Esta conta não possui pagamentos para cancelar");

        conta.setValorPago(BigDecimal.ZERO);
        conta.setValorRestante(conta.getValorOriginal());
        conta.setStatus("PENDENTE");
        conta.setDataPagamento(null);

        repository.atualizar(conta);
    }

    public List<ContasPagarModel> buscarPorFornecedor(Integer fornecedorId) throws SQLException {
        return contasPagarRepository.buscarPorFornecedor(fornecedorId);
    }

    public List<ContasPagarModel> buscarPorStatus(String status) throws SQLException {
        return contasPagarRepository.buscarPorStatus(status);
    }

    public List<ContasPagarModel> buscarVencidas() throws SQLException {
        return contasPagarRepository.buscarVencidas();
    }

    public List<ContasPagarModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return contasPagarRepository.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<ContasPagarModel> buscarPorCompra(Integer compraId) throws SQLException {
        return contasPagarRepository.buscarPorCompra(compraId);
    }

    public BigDecimal getTotalEmAberto() throws SQLException {
        var contas = contasPagarRepository.buscarPorStatus("PENDENTE");
        return contas.stream()
                .map(ContasPagarModel::getValorRestante)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalVencidas() throws SQLException {
        var contas = contasPagarRepository.buscarVencidas();
        return contas.stream()
                .map(ContasPagarModel::getValorRestante)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validar(ContasPagarModel model, boolean isNew) {
        if (model.getDescricao() == null || model.getDescricao().trim().isEmpty())
            throw new IllegalArgumentException("Descrição é obrigatória");

        if (model.getValorOriginal() == null || model.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Valor original deve ser maior que zero");

        if (model.getDataVencimento() == null)
            throw new IllegalArgumentException("Data de vencimento é obrigatória");

        if (model.getValorPago() == null) model.setValorPago(BigDecimal.ZERO);
        if (model.getValorRestante() == null)
            model.setValorRestante(model.getValorOriginal().subtract(model.getValorPago()));
        if (model.getStatus() == null) model.setStatus("PENDENTE");

        if (model.getValorPago().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Valor pago não pode ser negativo");
        if (model.getValorRestante().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Valor restante não pode ser negativo");
    }
}

package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.ContaAreceberModel;
import my_app.db.repositories.ContasAReceberRepository;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ContaAreceberService extends BaseService<ContaAreceberModel> {

    private final ContasAReceberRepository contasAReceberRepository;

    public ContaAreceberService() throws SQLException {
        this(DB.getPersismSession());
    }

    public ContaAreceberService(Session session) {
        super(new ContasAReceberRepository(session));
        this.contasAReceberRepository = (ContasAReceberRepository) repository;
    }

    @Override
    public ContaAreceberModel salvar(ContaAreceberModel model) throws SQLException {
        validar(model, true);
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(ContaAreceberModel model) throws SQLException {
        validar(model, false);
        repository.atualizar(model);
    }

    public void excluir(long id) throws SQLException {
        var conta = repository.buscarById(id);
        if (conta == null) throw new IllegalArgumentException("Conta a receber não encontrada");
        if ("PAGO".equals(conta.getStatus()))
            throw new IllegalArgumentException("Não é possível excluir contas já recebidas");
        repository.excluirById(id);
    }

    public void registrarRecebimento(long id, BigDecimal valorRecebido) throws SQLException {
        if (valorRecebido == null || valorRecebido.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Valor recebido deve ser maior que zero");

        var conta = repository.buscarById(id);
        if (conta == null) throw new IllegalArgumentException("Conta a receber não encontrada");
        if ("PAGO".equals(conta.getStatus()))
            throw new IllegalArgumentException("Esta conta já está paga");
        if (valorRecebido.compareTo(conta.getValorRestante()) > 0)
            throw new IllegalArgumentException("Valor recebido não pode ser maior que o valor restante");

        var novoValorRecebido = conta.getValorRecebido().add(valorRecebido);
        var novoValorRestante = conta.getValorRestante().subtract(valorRecebido);

        String novoStatus;
        if (novoValorRestante.compareTo(BigDecimal.ZERO) <= 0) {
            novoStatus = "PAGO";
            novoValorRestante = BigDecimal.ZERO;
        } else {
            novoStatus = "PARCIAL";
        }

        conta.setValorRecebido(novoValorRecebido);
        conta.setValorRestante(novoValorRestante);
        conta.setStatus(novoStatus);
        conta.setDataRecebimento(System.currentTimeMillis());

        repository.atualizar(conta);
    }

    public void cancelarRecebimento(long id) throws SQLException {
        var conta = repository.buscarById(id);
        if (conta == null) throw new IllegalArgumentException("Conta a receber não encontrada");

        var status = conta.getStatus();
        if (!"PAGO".equals(status) && !"PARCIAL".equals(status))
            throw new IllegalArgumentException("Esta conta não possui recebimentos para cancelar");

        conta.setValorRecebido(BigDecimal.ZERO);
        conta.setValorRestante(conta.getValorOriginal());
        conta.setStatus("PENDENTE");
        conta.setDataRecebimento(null);

        repository.atualizar(conta);
    }

    public List<ContaAreceberModel> buscarPorCliente(Integer clienteId) throws SQLException {
        return contasAReceberRepository.buscarPorCliente(clienteId);
    }

    public List<ContaAreceberModel> buscarPorStatus(String status) throws SQLException {
        return contasAReceberRepository.buscarPorStatus(status);
    }

    public List<ContaAreceberModel> buscarVencidas() throws SQLException {
        return contasAReceberRepository.buscarVencidas();
    }

    public List<ContaAreceberModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return contasAReceberRepository.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<ContaAreceberModel> buscarPorVenda(Integer vendaId) throws SQLException {
        return contasAReceberRepository.buscarPorVenda(vendaId);
    }

    public BigDecimal getTotalEmAberto() throws SQLException {
        var contas = contasAReceberRepository.buscarPorStatus("PENDENTE");
        return contas.stream()
                .map(ContaAreceberModel::getValorRestante)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void gerarContasDeVenda(Integer vendaId, Integer clienteId, java.util.List<my_app.domain.Parcela> parcelas) throws SQLException {
        if (vendaId == null) throw new IllegalArgumentException("Venda inválida");
        if (parcelas == null || parcelas.isEmpty()) throw new IllegalArgumentException("Parcelas não informadas");

        for (var parcela : parcelas) {
            var model = new my_app.db.models.ContaAreceberModel();
            model.setDescricao("Parcela " + parcela.numero() + " - Venda #" + vendaId);
            model.setValorOriginal(parcela.valor());
            model.setValorRecebido(java.math.BigDecimal.ZERO);
            model.setValorRestante(parcela.valor());
            model.setDataVencimento(parcela.dataVencimento());
            model.setDataRecebimento(null);
            model.setStatus("PENDENTE");
            model.setClienteId(clienteId);
            model.setVendaId(vendaId);
            model.setNumeroDocumento("PARC/" + parcela.numero());
            model.setTipoDocumento("DUPLICATA");
            model.setObservacao("Gerado automaticamente da venda #" + vendaId);
            model.setDataCriacao(LocalDateTime.now());
            contasAReceberRepository.salvar(model);
        }
    }

    public BigDecimal somarReceitasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return contasAReceberRepository.somarReceitasPorPeriodo(dataInicio, dataFim);
    }

    public void excluirPorVendaId(Integer vendaId) throws SQLException {
        contasAReceberRepository.excluirPorVendaId(vendaId);
    }

    public BigDecimal getTotalVencidas() throws SQLException {
        var contas = contasAReceberRepository.buscarVencidas();
        return contas.stream()
                .map(ContaAreceberModel::getValorRestante)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validar(ContaAreceberModel model, boolean isNew) {
        if (model.getDescricao() == null || model.getDescricao().trim().isEmpty())
            throw new IllegalArgumentException("Descrição é obrigatória");

        if (model.getValorOriginal() == null || model.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Valor original deve ser maior que zero");

        if (model.getDataVencimento() == null)
            throw new IllegalArgumentException("Data de vencimento é obrigatória");

        if (model.getValorRecebido() == null) model.setValorRecebido(BigDecimal.ZERO);
        if (model.getValorRestante() == null)
            model.setValorRestante(model.getValorOriginal().subtract(model.getValorRecebido()));
        if (model.getStatus() == null) model.setStatus("PENDENTE");

        if (model.getValorRecebido().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Valor recebido não pode ser negativo");
        if (model.getValorRestante().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Valor restante não pode ser negativo");
    }
}

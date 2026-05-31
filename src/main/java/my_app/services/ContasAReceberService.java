package my_app.services;

import my_app.db.models.ContaAreceberModel;
import my_app.db.models.VendaModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.VendaService;
import my_app.domain.Parcela;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ContasAReceberService {
    private final ContaAreceberService repo;
    private final VendaService vendaService;
    private final ClienteService clienteService;

    public ContasAReceberService(VendaService vendaService, ClienteService clienteService) throws SQLException {
        this.repo = new ContaAreceberService();
        this.vendaService = vendaService;
        this.clienteService = clienteService;
    }

    public void salvar(ContaAreceberModel conta) throws SQLException {
        validar(conta);
        repo.salvar(conta);
    }

    public List<ContaAreceberModel> listar() throws SQLException {
        return repo.listar();
    }

    public ContaAreceberModel buscarPorId(Long id) throws SQLException {
        return repo.buscarById(id);
    }

    public void atualizar(ContaAreceberModel conta) throws SQLException {
        validar(conta);
        repo.atualizar(conta);
    }

    public void excluir(Long id) throws SQLException {
        var conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a receber não encontrada");
        }
        if ("PAGO".equals(conta.getStatus())) {
            throw new IllegalStateException("Não é possível excluir contas já recebidas");
        }
        repo.excluirById(id);
    }

    public void registrarRecebimento(Long id, BigDecimal valorRecebido) throws SQLException {
        repo.registrarRecebimento(id, valorRecebido);
    }

    public void cancelarRecebimento(Long id) throws SQLException {
        repo.cancelarRecebimento(id);
    }

    public List<ContaAreceberModel> buscarPorCliente(Long clientId) throws SQLException {
        return repo.buscarPorCliente(clientId != null ? clientId.intValue() : null);
    }

    public List<ContaAreceberModel> buscarPorStatus(String status) throws SQLException {
        return repo.buscarPorStatus(status);
    }

    public List<ContaAreceberModel> buscarVencidas() throws SQLException {
        return repo.buscarVencidas();
    }

    public List<ContaAreceberModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return repo.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<ContaAreceberModel> buscarPorVenda(Long vendaId) throws SQLException {
        return repo.buscarPorVenda(vendaId != null ? vendaId.intValue() : null);
    }

    public void gerarContasDeVenda(VendaModel venda, List<Parcela> parcelas) throws SQLException {
        if (venda == null || venda.getId() == null) {
            throw new IllegalStateException("Venda inválida");
        }

        if (parcelas == null || parcelas.isEmpty()) {
            throw new IllegalStateException("Parcelas não informadas");
        }

        repo.gerarContasDeVenda(venda.getId(), venda.getClienteId(), parcelas);
    }

    public BigDecimal getTotalEmAberto() throws SQLException {
        return repo.getTotalEmAberto();
    }

    public BigDecimal getTotalVencidas() throws SQLException {
        return repo.getTotalVencidas();
    }

    private void validar(ContaAreceberModel conta) {
        if (conta.getDescricao() == null || conta.getDescricao().trim().isEmpty()) {
            throw new IllegalStateException("Descrição é obrigatória");
        }

        if (conta.getValorOriginal() == null || conta.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor original deve ser maior que zero");
        }

        if (conta.getDataVencimento() == null) {
            throw new IllegalStateException("Data de vencimento é obrigatória");
        }

        if (conta.getValorRecebido() == null) {
            conta.setValorRecebido(BigDecimal.ZERO);
        }

        if (conta.getValorRestante() == null) {
            conta.setValorRestante(conta.getValorOriginal().subtract(conta.getValorRecebido()));
        }

        if (conta.getStatus() == null) {
            conta.setStatus("PENDENTE");
        }

        if (conta.getDataRecebimento() == null && !"PENDENTE".equals(conta.getStatus()) && !"ATRASADO".equals(conta.getStatus())) {
            throw new IllegalStateException("Data de recebimento é obrigatória para contas recebidas ou recebidas parcialmente");
        }

        if (conta.getValorRecebido().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valor recebido não pode ser negativo");
        }

        if (conta.getValorRestante().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valor restante não pode ser negativo");
        }
    }
}

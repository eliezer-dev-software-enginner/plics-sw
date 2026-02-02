package my_app.services;

import my_app.db.dto.ContaAreceberDto;
import my_app.db.dto.ContasPagarDto;
import my_app.db.models.VendaModel;
import my_app.db.models.ContaAreceberModel;
import my_app.db.models.VendaModel;
import my_app.db.repositories.*;
import my_app.domain.Parcela;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ContasAReceberService {
    private final ContasAReceberRepository repo;
    private final VendaRepository vendaRepo;
    private final ClienteRepository clienteRepo;

    public ContasAReceberService(VendaRepository vendaRepository,  ClienteRepository clienteRepository) {
        this.repo = new ContasAReceberRepository();
        this.vendaRepo = vendaRepository;
        this.clienteRepo = clienteRepository;
    }

    public void salvar(ContaAreceberModel conta) throws SQLException {
        validar(conta);
        ContaAreceberDto dto = new ContaAreceberDto(
            conta.descricao,
            conta.valorOriginal,
            conta.valorRecebido,
            conta.valorRestante,
            conta.dataVencimento,
            conta.dataRecebimento,
            conta.status,
            conta.clienteId,
            conta.vendaId,
            conta.numeroDocumento,
            conta.tipoDocumento,
            conta.observacao
        );
        repo.salvar(dto);
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
        ContaAreceberModel conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a pagar não encontrada");
        }
        
        if ("PAGO".equals(conta.status)) {
            throw new IllegalStateException("Não é possível excluir contas já pagas");
        }
        
        repo.excluirById(id);
    }

    public void registrarPagamento(Long id, BigDecimal valorRecebido) throws SQLException {
        if (valorRecebido == null || valorRecebido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor recebido deve ser maior que zero");
        }

        ContaAreceberModel conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a receber não encontrada");
        }

        if ("PAGO".equals(conta.status)) {
            throw new IllegalStateException("Esta conta já está paga");
        }

        if (valorRecebido.compareTo(conta.valorRestante) > 0) {
            throw new IllegalStateException("Valor recebido não pode ser maior que o valor restante");
        }

        repo.registrarRecebimento(id, valorRecebido);
    }

    public void cancelarRecebimento(Long id) throws SQLException {
        ContaAreceberModel conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a receber não encontrada");
        }

        if (!"RECEBIDO".equals(conta.status) && !"PARCIAL".equals(conta.status)) {
            throw new IllegalStateException("Esta conta não possui recebimentos para cancelar");
        }

        conta.valorRecebido = BigDecimal.ZERO;
        conta.valorRestante = conta.valorOriginal;
        conta.status = "PENDENTE";
        conta.dataRecebimento = null;

        repo.atualizar(conta);
    }

    public List<ContaAreceberModel> buscarPorCliente(Long clientId) throws SQLException {
        return repo.buscarPorCliente(clientId);
    }

    public List<ContaAreceberModel> buscarPorStatus(String status) throws SQLException {
        return repo.buscarPorStatus(status);
    }

    public List<ContaAreceberModel> buscarVencidas() throws SQLException {
        return repo.buscarVencidas();
    }

    public List<ContaAreceberModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim) {
            throw new IllegalStateException("Data de início deve ser anterior à data de fim");
        }
        return repo.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<ContaAreceberModel> buscarPorVenda(Long vendaId) throws SQLException {
        return repo.buscarPorVenda(vendaId);
    }

    public void gerarContasDeVenda(VendaModel venda, List<Parcela> parcelas) throws SQLException {
        if (venda == null || venda.id == null) {
            throw new IllegalStateException("Venda inválida");
        }

        if (parcelas == null || parcelas.isEmpty()) {
            throw new IllegalStateException("Parcelas não informadas");
        }

        for (Parcela parcela : parcelas) {
            var dto = new ContaAreceberDto(
                    "Parcela " + parcela.numero() + " - Venda #" + venda.id,
                    parcela.valor(),
                    BigDecimal.ZERO,
                    parcela.valor(),
                    parcela.dataVencimento(),
                    null,
                    "PENDENTE",
                    venda.clienteId,
                    venda.id,
                    "PARC/" + parcela.numero(),
                    "DUPLICATA",
                    "Gerado automaticamente da venda #" + venda.id
            );
            repo.salvar(dto);
        }
    }

    public BigDecimal getTotalEmAberto() throws SQLException {
        List<ContaAreceberModel> contas = repo.buscarPorStatus("PENDENTE");
        BigDecimal total = BigDecimal.ZERO;
        for (ContaAreceberModel conta : contas) {
            total = total.add(conta.valorRestante);
        }
        return total;
    }

    public BigDecimal getTotalVencidas() throws SQLException {
        List<ContaAreceberModel> contas = repo.buscarVencidas();
        BigDecimal total = BigDecimal.ZERO;
        for (ContaAreceberModel conta : contas) {
            total = total.add(conta.valorRestante);
        }
        return total;
    }

    public BigDecimal getTotalRecebidoNoMes(Long dataInicioMes, Long dataFimMes) throws SQLException {
        String sql = """
            SELECT SUM(valor_recebido) as total FROM contas_a_receber 
            WHERE data_recebimento BETWEEN ? AND ? AND status = 'RECEBIDO'
            """;
        
        try (Connection conn = my_app.db.DB.getInstance().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dataInicioMes);
            ps.setLong(2, dataFimMes);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private void validar(ContaAreceberModel conta) {
        if (conta.descricao == null || conta.descricao.trim().isEmpty()) {
            throw new IllegalStateException("Descrição é obrigatória");
        }

        if (conta.valorOriginal == null || conta.valorOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor original deve ser maior que zero");
        }

        if (conta.dataVencimento == null) {
            throw new IllegalStateException("Data de vencimento é obrigatória");
        }

        if (conta.valorRecebido == null) {
            conta.valorRecebido = BigDecimal.ZERO;
        }

        if (conta.valorRestante == null) {
            conta.valorRestante = conta.valorOriginal.subtract(conta.valorRecebido);
        }

        if (conta.status == null) {
            conta.status = "PENDENTE";
        }

        if (conta.dataRecebimento == null && !"PENDENTE".equals(conta.status) && !"ATRASADO".equals(conta.status)) {
            throw new IllegalStateException("Data de recebimento é obrigatória para contas recebidas ou recebidas parcialmente");
        }

        if (conta.valorRecebido.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valor recebido não pode ser negativo");
        }

        if (conta.valorRestante.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valor restante não pode ser negativo");
        }
    }
}
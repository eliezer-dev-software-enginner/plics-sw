package my_app.services;

import my_app.db.models.ContasPagarModel;
import my_app.db.dto.ContasPagarDto;
import my_app.db.models.CompraModel;
import my_app.db.models.FornecedorModel;
import my_app.db.repositories.ContasPagarRepository;
import my_app.db.repositories.ComprasRepository;
import my_app.db.repositories.FornecedorRepository;
import my_app.domain.Parcela;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;

public class ContasPagarService {
    private final ContasPagarRepository repo;
    private final ComprasRepository compraRepo;
    private final FornecedorRepository fornecedorRepo;

    public ContasPagarService() {
        this.repo = new ContasPagarRepository();
        this.compraRepo = new ComprasRepository();
        this.fornecedorRepo = new FornecedorRepository();
    }

    public void salvar(ContasPagarModel conta) throws SQLException {
        validar(conta);
        ContasPagarDto dto = new ContasPagarDto(
            conta.descricao,
            conta.valorOriginal,
            conta.valorPago,
            conta.valorRestante,
            conta.dataVencimento,
            conta.dataPagamento,
            conta.status,
            conta.fornecedorId,
            conta.compraId,
            conta.numeroDocumento,
            conta.tipoDocumento,
            conta.observacao
        );
        repo.salvar(dto);
    }

    public List<ContasPagarModel> listar() throws SQLException {
        return repo.listar();
    }

    public ContasPagarModel buscarPorId(Long id) throws SQLException {
        return repo.buscarById(id);
    }

    public void atualizar(ContasPagarModel conta) throws SQLException {
        validar(conta);
        repo.atualizar(conta);
    }

    public void excluir(Long id) throws SQLException {
        ContasPagarModel conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a pagar não encontrada");
        }
        
        if ("PAGO".equals(conta.status)) {
            throw new IllegalStateException("Não é possível excluir contas já pagas");
        }
        
        repo.excluirById(id);
    }

    public void registrarPagamento(Long id, BigDecimal valorPago) throws SQLException {
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor pago deve ser maior que zero");
        }

        ContasPagarModel conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a pagar não encontrada");
        }

        if ("PAGO".equals(conta.status)) {
            throw new IllegalStateException("Esta conta já está paga");
        }

        if (valorPago.compareTo(conta.valorRestante) > 0) {
            throw new IllegalStateException("Valor pago não pode ser maior que o valor restante");
        }

        repo.registrarPagamento(id, valorPago);
    }

    public void cancelarPagamento(Long id) throws SQLException {
        ContasPagarModel conta = repo.buscarById(id);
        if (conta == null) {
            throw new IllegalStateException("Conta a pagar não encontrada");
        }

        if (!"PAGO".equals(conta.status) && !"PARCIAL".equals(conta.status)) {
            throw new IllegalStateException("Esta conta não possui pagamentos para cancelar");
        }

        conta.valorPago = BigDecimal.ZERO;
        conta.valorRestante = conta.valorOriginal;
        conta.status = "PENDENTE";
        conta.dataPagamento = null;

        repo.atualizar(conta);
    }

    public List<ContasPagarModel> buscarPorFornecedor(Long fornecedorId) throws SQLException {
        return repo.buscarPorFornecedor(fornecedorId);
    }

    public List<ContasPagarModel> buscarPorStatus(String status) throws SQLException {
        return repo.buscarPorStatus(status);
    }

    public List<ContasPagarModel> buscarVencidas() throws SQLException {
        return repo.buscarVencidas();
    }

    public List<ContasPagarModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim) {
            throw new IllegalStateException("Data de início deve ser anterior à data de fim");
        }
        return repo.buscarPorPeriodo(dataInicio, dataFim);
    }

    public List<ContasPagarModel> buscarPorCompra(Long compraId) throws SQLException {
        return repo.buscarPorCompra(compraId);
    }

    public void gerarContasDeCompra(CompraModel compra, List<Parcela> parcelas) throws SQLException {
        if (compra == null || compra.id == null) {
            throw new IllegalStateException("Compra inválida");
        }

        if (parcelas == null || parcelas.isEmpty()) {
            throw new IllegalStateException("Parcelas não informadas");
        }

        for (Parcela parcela : parcelas) {
            ContasPagarDto dto = new ContasPagarDto(
                    "Parcela " + parcela.numero() + " - Compra #" + compra.id,
                    parcela.valor(),
                    BigDecimal.ZERO,
                    parcela.valor(),
                    parcela.dataVencimento(),
                    null,
                    "PENDENTE",
                    compra.fornecedorId,
                    compra.id,
                    "PARC/" + parcela.numero(),
                    "DUPLICATA",
                    "Gerado automaticamente da compra #" + compra.id
            );
            repo.salvar(dto);
        }
    }

    public BigDecimal getTotalEmAberto() throws SQLException {
        List<ContasPagarModel> contas = repo.buscarPorStatus("PENDENTE");
        BigDecimal total = BigDecimal.ZERO;
        for (ContasPagarModel conta : contas) {
            total = total.add(conta.valorRestante);
        }
        return total;
    }

    public BigDecimal getTotalVencidas() throws SQLException {
        List<ContasPagarModel> contas = repo.buscarVencidas();
        BigDecimal total = BigDecimal.ZERO;
        for (ContasPagarModel conta : contas) {
            total = total.add(conta.valorRestante);
        }
        return total;
    }

    public BigDecimal getTotalPagoNoMes(Long dataInicioMes, Long dataFimMes) throws SQLException {
        String sql = """
            SELECT SUM(valor_pago) as total FROM contas_pagar 
            WHERE data_pagamento BETWEEN ? AND ? AND status = 'PAGO'
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

    private void validar(ContasPagarModel conta) {
        if (conta.descricao == null || conta.descricao.trim().isEmpty()) {
            throw new IllegalStateException("Descrição é obrigatória");
        }

        if (conta.valorOriginal == null || conta.valorOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor original deve ser maior que zero");
        }

        if (conta.dataVencimento == null) {
            throw new IllegalStateException("Data de vencimento é obrigatória");
        }

        if (conta.valorPago == null) {
            conta.valorPago = BigDecimal.ZERO;
        }

        if (conta.valorRestante == null) {
            conta.valorRestante = conta.valorOriginal.subtract(conta.valorPago);
        }

        if (conta.status == null) {
            conta.status = "PENDENTE";
        }

        if (conta.dataPagamento == null && !"PENDENTE".equals(conta.status) && !"ATRASADO".equals(conta.status)) {
            throw new IllegalStateException("Data de pagamento é obrigatória para contas pagas ou pagas parcialmente");
        }

        if (conta.valorPago.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valor pago não pode ser negativo");
        }

        if (conta.valorRestante.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valor restante não pode ser negativo");
        }
    }
}
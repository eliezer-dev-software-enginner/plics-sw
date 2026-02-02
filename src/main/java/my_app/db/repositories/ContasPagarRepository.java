package my_app.db.repositories;

import my_app.db.repositories.BaseRepository;
import my_app.db.models.ContasPagarModel;
import my_app.db.dto.ContasPagarDto;
import my_app.db.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ContasPagarRepository extends BaseRepository<ContasPagarDto, ContasPagarModel> {

    @Override
    public ContasPagarModel salvar(ContasPagarDto dto) throws SQLException {
        String sql = """
            INSERT INTO contas_pagar (
                descricao, valor_original, valor_pago, valor_restante, 
                data_vencimento, data_pagamento, status, fornecedor_id, 
                compra_id, numero_documento, tipo_documento, observacao, 
                data_criacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.descricao());
            ps.setBigDecimal(2, dto.valorOriginal());
            ps.setBigDecimal(3, dto.valorPago() != null ? dto.valorPago() : BigDecimal.ZERO);
            ps.setBigDecimal(4, dto.valorRestante() != null ? dto.valorRestante() : dto.valorOriginal());
            ps.setLong(5, dto.dataVencimento());
            
            if (dto.dataPagamento() != null) {
                ps.setLong(6, dto.dataPagamento());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setString(7, dto.status() != null ? dto.status() : "PENDENTE");
            
            if (dto.fornecedorId() != null) {
                ps.setLong(8, dto.fornecedorId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            
            if (dto.compraId() != null) {
                ps.setLong(9, dto.compraId());
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            
            ps.setString(10, dto.numeroDocumento());
            ps.setString(11, dto.tipoDocumento());
            ps.setString(12, dto.observacao());
            ps.setLong(13, System.currentTimeMillis());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return new ContasPagarModel().fromIdAndDto(id, dto);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    @Override
    public List<ContasPagarModel> listar() throws SQLException {
        String sql = "SELECT * FROM contas_pagar ORDER BY data_vencimento ASC";
        List<ContasPagarModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                contas.add(new ContasPagarModel().fromResultSet(rs));
            }
        }
        return contas;
    }

    @Override
    public void atualizar(ContasPagarModel model) throws SQLException {
        String sql = """
            UPDATE contas_pagar SET 
                descricao = ?, valor_original = ?, valor_pago = ?, valor_restante = ?,
                data_vencimento = ?, data_pagamento = ?, status = ?, fornecedor_id = ?,
                compra_id = ?, numero_documento = ?, tipo_documento = ?, observacao = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.descricao);
            ps.setBigDecimal(2, model.valorOriginal);
            ps.setBigDecimal(3, model.valorPago);
            ps.setBigDecimal(4, model.valorRestante);
            ps.setLong(5, model.dataVencimento);
            
            if (model.dataPagamento != null) {
                ps.setLong(6, model.dataPagamento);
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setString(7, model.status);
            
            if (model.fornecedorId != null) {
                ps.setLong(8, model.fornecedorId);
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            
            if (model.compraId != null) {
                ps.setLong(9, model.compraId);
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            
            ps.setString(10, model.numeroDocumento);
            ps.setString(11, model.tipoDocumento);
            ps.setString(12, model.observacao);
            ps.setLong(13, model.id);

            ps.executeUpdate();
        }
    }

    @Override
    public void excluirById(Long id) throws SQLException {
        String sql = "DELETE FROM contas_pagar WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public ContasPagarModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM contas_pagar WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ContasPagarModel().fromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Métodos de negócio específicos
    public List<ContasPagarModel> buscarPorFornecedor(Long fornecedorId) throws SQLException {
        String sql = "SELECT * FROM contas_pagar WHERE fornecedor_id = ? ORDER BY data_vencimento ASC";
        List<ContasPagarModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, fornecedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContasPagarModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContasPagarModel> buscarPorStatus(String status) throws SQLException {
        String sql = "SELECT * FROM contas_pagar WHERE status = ? ORDER BY data_vencimento ASC";
        List<ContasPagarModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContasPagarModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContasPagarModel> buscarVencidas() throws SQLException {
        long agora = System.currentTimeMillis();
        String sql = """
            SELECT * FROM contas_pagar 
            WHERE status != 'PAGO' AND data_vencimento < ? 
            ORDER BY data_vencimento ASC
            """;
        List<ContasPagarModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, agora);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContasPagarModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContasPagarModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        String sql = """
            SELECT * FROM contas_pagar 
            WHERE data_vencimento BETWEEN ? AND ? 
            ORDER BY data_vencimento ASC
            """;
        List<ContasPagarModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, dataInicio);
            ps.setLong(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContasPagarModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContasPagarModel> buscarPorCompra(Long compraId) throws SQLException {
        String sql = "SELECT * FROM contas_pagar WHERE compra_id = ? ORDER BY data_vencimento ASC";
        List<ContasPagarModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContasPagarModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public void excluirPorCompraId(Long compraId) throws SQLException {
        String sql = "DELETE FROM contas_pagar WHERE compra_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, compraId);
            ps.executeUpdate();
        }
    }

    public void registrarPagamento(Long id, BigDecimal valorPago) throws SQLException {
        ContasPagarModel conta = buscarById(id);
        if (conta == null) {
            throw new SQLException("Conta a pagar não encontrada");
        }

        BigDecimal novoValorPago = conta.valorPago.add(valorPago);
        BigDecimal novoValorRestante = conta.valorRestante.subtract(valorPago);

        String novoStatus;
        if (novoValorRestante.compareTo(BigDecimal.ZERO) <= 0) {
            novoStatus = "PAGO";
            novoValorRestante = BigDecimal.ZERO;
        } else {
            novoStatus = "PARCIAL";
        }

        String sql = """
            UPDATE contas_pagar SET 
                valor_pago = ?, valor_restante = ?, status = ?, data_pagamento = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, novoValorPago);
            ps.setBigDecimal(2, novoValorRestante);
            ps.setString(3, novoStatus);
            if ("PAGO".equals(novoStatus)) {
                ps.setLong(4, System.currentTimeMillis());
            } else if ("PARCIAL".equals(novoStatus)) {
                ps.setLong(4, System.currentTimeMillis());
            } else if (conta.dataPagamento != null) {
                ps.setLong(4, conta.dataPagamento);
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    public void atualizarStatus(Long id, String novoStatus) throws SQLException {
        String sql = "UPDATE contas_pagar SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
}
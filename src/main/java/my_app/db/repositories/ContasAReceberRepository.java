package my_app.db.repositories;

import my_app.db.dto.ContaAreceberDto;
import my_app.db.models.ContaAreceberModel;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContasAReceberRepository extends BaseRepository<ContaAreceberDto, ContaAreceberModel> {

    @Override
    public ContaAreceberModel salvar(ContaAreceberDto dto) throws SQLException {
        String sql = """
            INSERT INTO contas_a_receber (
                descricao, valor_original, valor_recebido, valor_restante, 
                data_vencimento, data_recebimento, status, cliente_id, 
                venda_id, numero_documento, tipo_documento, observacao, 
                data_criacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.descricao());
            ps.setBigDecimal(2, dto.valorOriginal());
            ps.setBigDecimal(3, dto.valorRecebido() != null ? dto.valorRecebido() : BigDecimal.ZERO);
            ps.setBigDecimal(4, dto.valorRestante() != null ? dto.valorRestante() : dto.valorOriginal());
            ps.setLong(5, dto.dataVencimento());
            
            if (dto.dataRecebimento() != null) {
                ps.setLong(6, dto.dataRecebimento());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setString(7, dto.status() != null ? dto.status() : "PENDENTE");
            
            if (dto.clienteId() != null) {
                ps.setLong(8, dto.clienteId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            
            if (dto.vendaId() != null) {
                ps.setLong(9, dto.vendaId());
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
                    return new ContaAreceberModel().fromIdAndDto(id, dto);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    @Override
    public List<ContaAreceberModel> listar() throws SQLException {
        String sql = "SELECT * FROM contas_a_receber ORDER BY data_vencimento ASC";
        List<ContaAreceberModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                contas.add(new ContaAreceberModel().fromResultSet(rs));
            }
        }
        return contas;
    }

    @Override
    public void atualizar(ContaAreceberModel model) throws SQLException {
        String sql = """
            UPDATE contas_a_receber SET 
                descricao = ?, valor_original = ?, valor_recebido = ?, valor_restante = ?,
                data_vencimento = ?, data_recebimento = ?, status = ?, cliente_id = ?,
                venda_id = ?, numero_documento = ?, tipo_documento = ?, observacao = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.descricao);
            ps.setBigDecimal(2, model.valorOriginal);
            ps.setBigDecimal(3, model.valorRecebido);
            ps.setBigDecimal(4, model.valorRestante);
            ps.setLong(5, model.dataVencimento);
            
            if (model.dataRecebimento != null) {
                ps.setLong(6, model.dataRecebimento);
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setString(7, model.status);
            
            if (model.clienteId != null) {
                ps.setLong(8, model.clienteId);
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            
            if (model.vendaId != null) {
                ps.setLong(9, model.vendaId);
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
        String sql = "DELETE FROM contas_a_receber WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public ContaAreceberModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM contas_a_receber WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ContaAreceberModel().fromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Métodos de negócio específicos
    public List<ContaAreceberModel> buscarPorCliente(Long clienteId) throws SQLException {
        String sql = "SELECT * FROM contas_a_receber WHERE cliente_id = ? ORDER BY data_vencimento ASC";
        List<ContaAreceberModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContaAreceberModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContaAreceberModel> buscarPorStatus(String status) throws SQLException {
        String sql = "SELECT * FROM contas_a_receber WHERE status = ? ORDER BY data_vencimento ASC";
        List<ContaAreceberModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContaAreceberModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContaAreceberModel> buscarVencidas() throws SQLException {
        long agora = System.currentTimeMillis();
        String sql = """
            SELECT * FROM contas_a_receber 
            WHERE status != 'RECEBIDO' AND data_vencimento < ? 
            ORDER BY data_vencimento ASC
            """;
        List<ContaAreceberModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, agora);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContaAreceberModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContaAreceberModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        String sql = """
            SELECT * FROM contas_a_receber 
            WHERE data_vencimento BETWEEN ? AND ? 
            ORDER BY data_vencimento ASC
            """;
        List<ContaAreceberModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, dataInicio);
            ps.setLong(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContaAreceberModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public List<ContaAreceberModel> buscarPorVenda(Long vendaId) throws SQLException {
        String sql = "SELECT * FROM contas_a_receber WHERE venda_id = ? ORDER BY data_vencimento ASC";
        List<ContaAreceberModel> contas = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, vendaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(new ContaAreceberModel().fromResultSet(rs));
                }
            }
        }
        return contas;
    }

    public void excluirPorCompra(Long vendaId) throws SQLException {
        String sql = "DELETE FROM contas_a_receber WHERE venda_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, vendaId);
            ps.executeUpdate();
        }
    }

    public void registrarRecebimento(Long id, BigDecimal valorRecebido) throws SQLException {
        ContaAreceberModel conta = buscarById(id);
        if (conta == null) {
            throw new SQLException("Conta a pagar não encontrada");
        }

        BigDecimal novoValorRecebido = conta.valorRecebido.add(valorRecebido);
        BigDecimal novoValorRestante = conta.valorRestante.subtract(valorRecebido);

        String novoStatus;
        if (novoValorRestante.compareTo(BigDecimal.ZERO) <= 0) {
            novoStatus = "RECEBIDO";
            novoValorRestante = BigDecimal.ZERO;
        } else {
            novoStatus = "PARCIAL";
        }

        String sql = """
            UPDATE contas_a_receber SET 
                valor_recebido = ?, valor_restante = ?, status = ?, data_recebimento = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, novoValorRecebido);
            ps.setBigDecimal(2, novoValorRestante);
            ps.setString(3, novoStatus);
            if ("RECEBIDO".equals(novoStatus)) {
                ps.setLong(4, System.currentTimeMillis());
            } else if ("PARCIAL".equals(novoStatus)) {
                ps.setLong(4, System.currentTimeMillis());
            } else if (conta.dataRecebimento != null) {
                ps.setLong(4, conta.dataRecebimento);
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    public void atualizarStatus(Long id, String novoStatus) throws SQLException {
        String sql = "UPDATE contas_a_receber SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
}
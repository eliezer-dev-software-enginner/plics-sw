package my_app.db.repositories;

import my_app.db.dto.PedidoDto;
import my_app.db.models.PedidoModel;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// PedidoRepository.java
public class PedidoRepository extends BaseRepository<PedidoDto, PedidoModel> {

    @Override
    public PedidoModel salvar(PedidoDto dto) throws SQLException {
        String sql = """
            INSERT INTO pedidos (cliente_id, forma_pagamento, total_liquido,
                desconto, observacao, is_fiado, data_criacao)
            VALUES (?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (dto.clienteId() != null) ps.setLong(1, dto.clienteId());
            else ps.setNull(1, Types.BIGINT);
            ps.setString(2, dto.formaPagamento());
            ps.setBigDecimal(3, dto.totalLiquido());
            ps.setBigDecimal(4, dto.desconto() != null ? dto.desconto() : BigDecimal.ZERO);
            ps.setString(5, dto.observacao());
            ps.setInt(6, dto.isFiado() ? 1 : 0);
            ps.setLong(7, System.currentTimeMillis());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return new PedidoModel().fromIdAndDto(keys.getLong(1), dto);
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    @Override
    public List<PedidoModel> listar() throws SQLException {
        List<PedidoModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM pedidos");
            while (rs.next()) lista.add(new PedidoModel().fromResultSet(rs));
        }
        return lista;
    }

    @Override
    protected void atualizar(PedidoModel pedidoModel) throws SQLException {

    }

    @Override
    protected void excluirById(Long id) throws SQLException {

    }

    @Override
    protected PedidoModel buscarById(Long id) throws SQLException {
        return null;
    }

    // buscarById, listar, excluirById — padrão igual aos outros repos
}


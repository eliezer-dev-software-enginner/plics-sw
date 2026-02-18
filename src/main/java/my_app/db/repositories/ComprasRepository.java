package my_app.db.repositories;

import my_app.db.dto.CompraDto;
import my_app.db.models.CompraModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ComprasRepository extends BaseRepository<CompraDto, CompraModel> {

    // CREATE
    public CompraModel salvar(CompraDto dto) throws SQLException {
        String sql = """
        INSERT INTO compras 
        (produto_cod, fornecedor_id, quantidade, preco_compra, desconto_em_reais, tipo_pagamento, 
         observacao, data_criacao, data_compra, numero_nota, data_validade, total_liquido) 
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.produtoCod());
            ps.setLong(2, dto.fornecedorId());
            ps.setBigDecimal(3, dto.quantidade());
            ps.setBigDecimal(4, dto.precoCompra());
            ps.setBigDecimal(5, dto.descontoEmReais());
            ps.setString(6, dto.tipoPagamento());
            ps.setString(7, dto.observacao());
            ps.setLong(8, System.currentTimeMillis());
            ps.setLong(9, dto.dataCompra());
            ps.setString(10, dto.numeroNota());
            if (dto.dataValidade() != null) {
                ps.setLong(11, dto.dataValidade());
            } else {
                ps.setNull(11, java.sql.Types.BIGINT);
            }
            ps.setBigDecimal(12, dto.totalLiquido());
            ps.executeUpdate();
            // Recupera o ID gerado e cria nova instância
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    return new CompraModel().fromIdAndDto(idGerado, dto);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    public List<CompraModel> listar() throws SQLException {
        List<CompraModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM compras");
            while (rs.next()) lista.add(new CompraModel().fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(CompraModel model) throws SQLException {
        String sql = """
        UPDATE compras SET produto_cod = ?, fornecedor_id = ?, quantidade = ?,
        preco_compra = ?, desconto_em_reais = ?, tipo_pagamento = ?, observacao = ?,
        data_compra = ?, numero_nota = ?, data_validade = ?, total_liquido = ?
        WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, model.produtoCod);
            ps.setLong(2, model.fornecedorId);
            ps.setBigDecimal(3, model.quantidade);
            ps.setBigDecimal(4, model.precoDeCompra);
            ps.setBigDecimal(5, model.descontoEmReais);
            ps.setString(6, model.tipoPagamento);
            ps.setString(7, model.observacao);
            ps.setLong(8, model.dataCompra);
            ps.setString(9, model.numeroNota);
            if (model.dataValidade != null) {
                ps.setLong(10, model.dataValidade);
            } else {
                ps.setNull(10, java.sql.Types.BIGINT);
            }
            ps.setLong(11, model.id);
            ps.setBigDecimal(12, model.totalLiquido);

            ps.executeUpdate();
        }
    }

    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM compras WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected CompraModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM compras WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? new CompraModel().fromResultSet(rs) : null;
        }
    }

    public BigDecimal somarComprasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(total_liquido), 0) as total 
            FROM compras 
            WHERE data_criacao BETWEEN ? AND ?
            AND tipo_pagamento != 'A PRAZO'
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, dataInicio);
            ps.setLong(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        return BigDecimal.ZERO;
    }
}

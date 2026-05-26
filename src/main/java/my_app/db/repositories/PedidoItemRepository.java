package my_app.db.repositories;

import my_app.db.dto.PedidoItemDto;
import my_app.db.models.PedidoItemModel;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// PedidoItemRepository.java
public class PedidoItemRepository extends BaseRepository<PedidoItemDto, PedidoItemModel> {

    public PedidoItemModel salvar(PedidoItemDto dto) throws SQLException {
        String sql = """
            INSERT INTO pedido_itens (pedido_id, produto_cod, quantidade,
                preco_unitario, desconto, total_item, data_criacao)
            VALUES (?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, dto.pedidoId());
            ps.setString(2, dto.produtoCod());
            ps.setBigDecimal(3, dto.quantidade());
            ps.setBigDecimal(4, dto.precoUnitario());
            ps.setBigDecimal(5, dto.desconto() != null ? dto.desconto() : BigDecimal.ZERO);
            ps.setBigDecimal(6, dto.totalItem());
            ps.setLong(7, System.currentTimeMillis());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return (PedidoItemModel) new PedidoItemModel().fromIdAndDto(keys.getLong(1), dto);
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    @Override
    protected List<PedidoItemModel> listar() throws SQLException {
        return List.of();
    }

    @Override
    protected void atualizar(PedidoItemModel pedidoItemModel) throws SQLException {

    }

    @Override
    protected void excluirById(Long id) throws SQLException {

    }

    @Override
    protected PedidoItemModel buscarById(Long id) throws SQLException {
        return null;
    }

    public List<PedidoItemModel> listarPorPedido(Long pedidoId) throws SQLException {
        String sql = "SELECT * FROM pedido_itens WHERE pedido_id = ? ORDER BY data_criacao ASC";
        var lista = new ArrayList<PedidoItemModel>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(new PedidoItemModel().fromResultSet(rs));
        }
        return lista;
    }
}
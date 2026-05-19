package my_app.db.models;

import my_app.db.dto.PedidoItemDto;
import my_app.domain.ModelBase;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PedidoItemModel extends ModelBase<PedidoItemDto> {
    public Long pedidoId;
    public String produtoCod;
    public BigDecimal quantidade;
    public BigDecimal precoUnitario;
    public BigDecimal desconto;
    public BigDecimal totalItem;

    @Override
    public PedidoItemModel fromResultSet(ResultSet rs) throws SQLException {
        var m = new PedidoItemModel();
        m.id = rs.getLong("id");
        m.pedidoId = rs.getLong("pedido_id");
        m.produtoCod = rs.getString("produto_cod");
        m.quantidade = rs.getBigDecimal("quantidade");
        m.precoUnitario = rs.getBigDecimal("preco_unitario");
        m.desconto = rs.getBigDecimal("desconto");
        m.totalItem = rs.getBigDecimal("total_item");
        m.dataCriacao = rs.getLong("data_criacao");
        return m;
    }

    @Override
    public PedidoItemModel fromIdAndDto(Long id, PedidoItemDto dto) {
        var m = new PedidoItemModel();
        m.id = id;
        m.pedidoId = dto.pedidoId();
        m.produtoCod = dto.produtoCod();
        m.quantidade = dto.quantidade();
        m.precoUnitario = dto.precoUnitario();
        m.desconto = dto.desconto();
        m.totalItem = dto.totalItem();
        m.dataCriacao = System.currentTimeMillis();
        return m;
    }
}
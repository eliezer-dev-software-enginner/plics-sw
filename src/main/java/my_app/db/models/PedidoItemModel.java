package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.PedidoItemDto;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
@Setter
@Getter
@Table("pedido_itens")
public class PedidoItemModel {
    private long id;
    private long data_criacao_millis;
    private String produtoCod;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal desconto;
    private BigDecimal totalItem;
}
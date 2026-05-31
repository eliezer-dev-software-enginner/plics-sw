package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Table("pedido_itens")
public class PedidoItemModel {

    @Column(primary = true)
    private Integer id;

    @Column(name = "pedido_id")
    private Integer pedidoId;

    @Column(name = "produto_cod")
    private String produtoCod;

    private BigDecimal quantidade;

    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    private BigDecimal desconto;

    @Column(name = "total_item")
    private BigDecimal totalItem;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;
}

package my_app.db.models;

import lombok.Getter;
import lombok.Setter;
import my_app.db.dto.PedidoDto;
import my_app.domain.ModelBase;
import net.sf.persism.annotations.Table;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Setter
@Getter
@Table("pedidos")
public class PedidoModel {
    private long id;
    private long data_criacao_millis;
    private Long clienteId;
    private String formaPagamento;
    private BigDecimal totalLiquido;
    private BigDecimal desconto;
    private String observacao;
    private boolean isFiado;

    private List<PedidoItemModel> itens; // composição, não vem do banco diretamente
}

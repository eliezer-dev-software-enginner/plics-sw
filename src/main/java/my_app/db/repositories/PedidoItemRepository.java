package my_app.db.repositories;

import my_app.db.models.PedidoItemModel;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class PedidoItemRepository extends BaseRepository<PedidoItemModel> {

    public PedidoItemRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<PedidoItemModel> modelClass() {
        return PedidoItemModel.class;
    }

    public List<PedidoItemModel> listarPorPedido(Integer pedidoId) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM pedido_itens WHERE pedido_id = ? ORDER BY dataCriacao ASC"),
                params(pedidoId)
        );
    }
}

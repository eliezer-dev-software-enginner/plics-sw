package my_app.db.repositories;

import my_app.db.models.PedidoModel;
import net.sf.persism.Session;

public class PedidoRepository extends BaseRepository<PedidoModel> {

    public PedidoRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<PedidoModel> modelClass() {
        return PedidoModel.class;
    }
}

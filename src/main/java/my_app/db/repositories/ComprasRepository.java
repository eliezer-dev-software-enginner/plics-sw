package my_app.db.repositories;

import my_app.db.models.CompraModel;
import net.sf.persism.Session;

public class ComprasRepository extends BaseRepository<CompraModel> {

    public ComprasRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<CompraModel> modelClass() {
        return CompraModel.class;
    }
}

package my_app.core.db.repositories;

import my_app.core.db.models.CorModel;
import net.sf.persism.Session;

public class CorRepository extends BaseRepository<CorModel> {

    public CorRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<CorModel> modelClass() {
        return CorModel.class;
    }
}

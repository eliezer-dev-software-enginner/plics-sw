package my_app.db.repositories;

import my_app.db.models.TecnicoModel;
import net.sf.persism.Session;

public class TecnicoRepository extends BaseRepository<TecnicoModel> {

    public TecnicoRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<TecnicoModel> modelClass() {
        return TecnicoModel.class;
    }
}

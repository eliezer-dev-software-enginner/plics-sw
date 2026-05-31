package my_app.db.repositories;

import my_app.db.models.PreferenciasModel;
import net.sf.persism.Session;

public class PreferenciasRepository extends BaseRepository<PreferenciasModel> {

    public PreferenciasRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<PreferenciasModel> modelClass() {
        return PreferenciasModel.class;
    }
}
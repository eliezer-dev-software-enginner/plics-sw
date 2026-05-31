package my_app.db.repositories;

import my_app.db.models.CategoriaModel;
import net.sf.persism.Session;

public class CategoriaRepository extends BaseRepository<CategoriaModel> {

    public CategoriaRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<CategoriaModel> modelClass() {
        return CategoriaModel.class;
    }
}
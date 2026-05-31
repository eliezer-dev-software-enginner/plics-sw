package my_app.db.repositories;

import my_app.db.models.FornecedorModel;
import net.sf.persism.Session;

public class FornecedorRepository extends BaseRepository<FornecedorModel> {

    public FornecedorRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<FornecedorModel> modelClass() {
        return FornecedorModel.class;
    }
}
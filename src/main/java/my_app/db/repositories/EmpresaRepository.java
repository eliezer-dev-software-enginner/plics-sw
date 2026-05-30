package my_app.db.repositories;

import my_app.db.models.EmpresaModel;
import net.sf.persism.Session;

public class EmpresaRepository  extends BaseRepository<EmpresaModel>  {

    public EmpresaRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<EmpresaModel> modelClass() {
        return EmpresaModel.class;
    }
}


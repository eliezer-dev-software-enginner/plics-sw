package my_app.db.repositories;

import my_app.db.models.EmpresaModel;
import net.sf.persism.Session;

import java.sql.SQLException;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class EmpresaRepository extends BaseRepository<EmpresaModel> {

    public EmpresaRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<EmpresaModel> modelClass() {
        return EmpresaModel.class;
    }

    public EmpresaModel buscarUnico() throws SQLException {
        return session().fetch(
                EmpresaModel.class,
                sql("SELECT * FROM empresas LIMIT 1")
        );
    }
}


package my_app.db.repositories;

import my_app.db.DB;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public abstract class BaseRepository<M> {

    private Session session;

    public BaseRepository(Session session) {
        this.session = session;
    }

    protected Session session() {
        return session;
    }

    protected abstract Class<M> modelClass();

    public M salvar(M model) throws SQLException {
       var result = session().insert(model);
        return result.dataObject();
    }

    public List<M> listar() throws SQLException {
        return session().query(modelClass());
    }

    public void atualizar(M model) throws SQLException {
        session().update(model);
    }

    public void excluirById(long id) throws SQLException {
        M model = buscarById(id);
        if (model != null) session().delete(model);
    }

    public M buscarById(long id) throws SQLException {
        return session().fetch(
                modelClass(),
                sql("SELECT * FROM categorias WHERE id = ?"),
                params(id)
        );
    }
}
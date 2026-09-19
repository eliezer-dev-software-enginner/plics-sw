package my_app.core.db.repositories;

import my_app.core.Identifier;
import my_app.core.db.DB;
import net.sf.persism.Session;
import net.sf.persism.annotations.Table;

import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public abstract class BaseRepository<M extends Identifier> {

    private Session session;

    public BaseRepository(Session session) {
        this.session = session;
    }

    protected Session session() {
        return session;
    }

    protected abstract Class<M> modelClass();

    public M salvar(M model) throws SQLException {
        if (model.getId() == null) {
            model.setId(proximoId());
        }
        var result = session().insert(model);
        return result.dataObject();
    }

    private Long proximoId() throws SQLException {
        String tableName = modelClass().getAnnotation(Table.class).value();
        Long max = session().fetch(
                Long.class,
                sql("SELECT id FROM " + tableName + " ORDER BY id DESC LIMIT 1"),
                params()
        );
        return (max == null) ? 1L : max + 1;
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
        String tableName = modelClass().getAnnotation(Table.class).value();
        return session().fetch(
                modelClass(),
                sql("SELECT * FROM " + tableName + " WHERE id = ?"),
                params(id)
        );
    }

    public void close() {
        if (session != null) {
            DB.unregister(session);
            session.close();
            session = null;
        }
    }
}
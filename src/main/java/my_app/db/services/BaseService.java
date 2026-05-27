package my_app.db.services;

import my_app.db.DB;
import my_app.db.repositories.BaseRepository;
import net.sf.persism.Session;
import org.flywaydb.core.Flyway;

import java.sql.SQLException;
import java.util.List;

public abstract class BaseService<M> {
    Session session;

    public BaseService(Session session) {
        this.session = session;
    }

    public abstract M salvar(M model) throws SQLException;
    public abstract List<M> listar() throws SQLException;
    public abstract void atualizar(M model) throws SQLException;
    public abstract void excluirById(long id) throws SQLException;
    public abstract M buscarById(long id) throws SQLException;
}
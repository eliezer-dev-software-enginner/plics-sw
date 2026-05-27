package my_app.db.services;

import java.sql.SQLException;
import java.util.List;
import my_app.db.repositories.BaseRepository;

public abstract class BaseService<M> {

    protected final BaseRepository<M> repository;

    public BaseService(BaseRepository<M> repository) {
        this.repository = repository;
    }

    public M salvar(M model) throws SQLException {
        return repository.salvar(model);
    }

    public List<M> listar() throws SQLException {
        return repository.listar();
    }

    public void atualizar(M model) throws SQLException {
        repository.atualizar(model);
    }

    public void excluirById(long id) throws SQLException {
        repository.excluirById(id);
    }

    public M buscarById(long id) throws SQLException {
        return repository.buscarById(id);
    }
}
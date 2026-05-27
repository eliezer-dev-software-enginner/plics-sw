package my_app.db.services;

import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CategoriaService extends BaseService<CategoriaModel> {
    private final CategoriaRepository repository;

    public CategoriaService(Session session) throws SQLException {
        super(session);
        this.repository = new CategoriaRepository(session);
    }

    @Override
    public CategoriaModel salvar(CategoriaModel model) throws SQLException {
        model.setDataCriacao(LocalDateTime.now());
        repository.salvar(model);
        return model;
    }

    @Override
    public List<CategoriaModel> listar() throws SQLException {
        return repository.listar();
    }

    @Override
    public void atualizar(CategoriaModel model) throws SQLException {
        repository.atualizar(model);
    }

    @Override
    public void excluirById(long id) throws SQLException {
        repository.excluirById(id);
    }

    @Override
    public CategoriaModel buscarById(long id) throws SQLException {
        return repository.buscarById(id);
    }
}

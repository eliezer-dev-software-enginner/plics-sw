package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.CorModel;
import my_app.db.repositories.CorRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class CorService extends BaseService<CorModel> {

    public CorService() throws SQLException {
        this(DB.getPersismSession());
    }

    public CorService(Session session) {
        super(new CorRepository(session));
    }

    @Override
    public CorModel salvar(CorModel model) throws SQLException {
        validar(model);
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }

    @Override
    public void atualizar(CorModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
    }

    private void validar(CorModel model) {
        if (model.getNome() == null || model.getNome().isBlank())
            throw new IllegalArgumentException("Nome da cor é obrigatório");
    }
}

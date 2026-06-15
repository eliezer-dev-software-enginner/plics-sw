package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.TecnicoModel;
import my_app.db.repositories.TecnicoRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TecnicoService extends BaseService<TecnicoModel> {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService() throws SQLException {
        this(DB.getPersismSession());
    }

    public TecnicoService(Session session) {
        super(new TecnicoRepository(session));
        this.tecnicoRepository = (TecnicoRepository) repository;
    }

    @Override
    public TecnicoModel salvar(TecnicoModel model) throws SQLException {
        validar(model);
        model.setDataCriacao(LocalDateTime.now());
        try {
            return repository.salvar(model);
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 && e.getMessage() != null && e.getMessage().contains("UNIQUE"))
                throw new IllegalArgumentException("Já existe um técnico com este nome");
            throw e;
        }
    }

    @Override
    public void atualizar(TecnicoModel model) throws SQLException {
        validar(model);
        try {
            repository.atualizar(model);
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 && e.getMessage() != null && e.getMessage().contains("UNIQUE"))
                throw new IllegalArgumentException("Já existe um técnico com este nome");
            throw e;
        }
    }

    private void validar(TecnicoModel model) {
        if (model.getNome() == null || model.getNome().isBlank())
            throw new IllegalArgumentException("Adicione nome ao técnico");
    }
}

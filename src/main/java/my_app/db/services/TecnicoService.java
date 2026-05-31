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
        return repository.salvar(model);
    }

    @Override
    public void atualizar(TecnicoModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
    }

    private void validar(TecnicoModel model) {
        if (model.getNome() == null || model.getNome().isBlank())
            throw new IllegalArgumentException("Adicione nome ao técnico");
    }
}

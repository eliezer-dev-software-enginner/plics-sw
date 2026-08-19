package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.CorModel;
import my_app.db.repositories.CorRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class CorService extends BaseService<CorModel> {

    private static final Logger log = LoggerFactory.getLogger(CorService.class);

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
        var salvo = repository.salvar(model);
        log.info("Cor salva: id={} nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    @Override
    public void atualizar(CorModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
        log.info("Cor atualizada: id={} nome={}", model.getId(), model.getNome());
    }

    private void validar(CorModel model) {
        if (model.getNome() == null || model.getNome().isBlank())
            throw new IllegalArgumentException("Nome da cor é obrigatório");
    }
}

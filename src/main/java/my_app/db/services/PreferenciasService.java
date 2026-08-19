package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PreferenciasModel;
import my_app.db.repositories.PreferenciasRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class PreferenciasService extends BaseService<PreferenciasModel> {

    // NUNCA logar model.getSenha()/getLogin() em lugar nenhum desta classe — são as
    // credenciais de acesso ao app inteiro (ver AuthScreenViewModel), não precisam
    // (e não podem) aparecer em texto no arquivo de log.
    private static final Logger log = LoggerFactory.getLogger(PreferenciasService.class);

    public PreferenciasService() throws SQLException {
        this(DB.getPersismSession());
    }

    public PreferenciasService(Session session) {
        super(new PreferenciasRepository(session));
    }

    @Override
    public PreferenciasModel salvar(PreferenciasModel model) throws SQLException {
        validar(model);
        var salvo = repository.salvar(model);
        log.info("Preferências salvas: id={} credenciaisHabilitadas={}", salvo.getId(), salvo.getCredenciaisHabilitadas());
        return salvo;
    }

    @Override
    public void atualizar(PreferenciasModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
        log.info("Preferências atualizadas: id={} credenciaisHabilitadas={}", model.getId(), model.getCredenciaisHabilitadas());
    }

    private void validar(PreferenciasModel model) {
        boolean habilitar = model.getCredenciaisHabilitadas() != null && model.getCredenciaisHabilitadas() == 1;
        if (!habilitar) return;
        if (model.getLogin() == null || model.getLogin().isBlank())
            throw new IllegalArgumentException("Login é obrigatório");
        if (model.getSenha() == null || model.getSenha().isBlank())
            throw new IllegalArgumentException("Senha é obrigatória");
    }
}

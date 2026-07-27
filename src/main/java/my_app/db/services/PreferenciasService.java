package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PreferenciasModel;
import my_app.db.repositories.PreferenciasRepository;
import net.sf.persism.Session;

import java.sql.SQLException;

public class PreferenciasService extends BaseService<PreferenciasModel> {

    public PreferenciasService() throws SQLException {
        this(DB.getPersismSession());
    }

    public PreferenciasService(Session session) {
        super(new PreferenciasRepository(session));
    }

    @Override
    public PreferenciasModel salvar(PreferenciasModel model) throws SQLException {
        validar(model);
        return repository.salvar(model);
    }

    @Override
    public void atualizar(PreferenciasModel model) throws SQLException {
        validar(model);
        repository.atualizar(model);
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

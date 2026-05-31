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
}

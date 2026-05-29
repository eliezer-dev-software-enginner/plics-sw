package my_app.db.repositories;

import my_app.db.dto.ClienteDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.ClienteModel;
import net.sf.persism.Session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteRepository extends BaseRepository<ClienteModel> {
    public ClienteRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<ClienteModel> modelClass() {
        return ClienteModel.class;
    }
}
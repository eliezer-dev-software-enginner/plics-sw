package my_app.db.repositories;

import my_app.db.models.ClienteModel;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class ClienteRepository extends BaseRepository<ClienteModel> {
    public ClienteRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<ClienteModel> modelClass() {
        return ClienteModel.class;
    }

    public ClienteModel buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        return session().fetch(
                modelClass(),
                sql("SELECT * FROM clientes WHERE cpfCnpj = ?"),
                params(cpfCnpj)
        );
    }

    public List<ClienteModel> listarNovosPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM clientes WHERE dataCriacao BETWEEN ? AND ?"),
                params(dataInicio, dataFim)
        );
    }
}
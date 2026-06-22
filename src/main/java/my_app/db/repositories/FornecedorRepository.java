package my_app.db.repositories;

import my_app.db.models.FornecedorModel;
import net.sf.persism.Session;

import java.sql.SQLException;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class FornecedorRepository extends BaseRepository<FornecedorModel> {

    public FornecedorRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<FornecedorModel> modelClass() {
        return FornecedorModel.class;
    }

    public FornecedorModel buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        return session().fetch(
                modelClass(),
                sql("SELECT * FROM fornecedores WHERE cpfCnpj = ?"),
                params(cpfCnpj)
        );
    }
}
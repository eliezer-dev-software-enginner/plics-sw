package my_app.db.repositories;

import my_app.db.models.CompraModel;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class ComprasRepository extends BaseRepository<CompraModel> {

    public ComprasRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<CompraModel> modelClass() {
        return CompraModel.class;
    }

    public BigDecimal somarComprasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        var compras = session().query(
                modelClass(),
                sql("SELECT * FROM compras WHERE data_criacao BETWEEN ? AND ?"),
                params(dataInicio, dataFim)
        );
        return compras.stream()
                .map(CompraModel::getTotalLiquido)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

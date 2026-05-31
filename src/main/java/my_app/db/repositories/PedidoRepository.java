package my_app.db.repositories;

import my_app.db.models.PedidoModel;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

import static my_app.utils.DateUtils.localDateParaMillis;
import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class PedidoRepository extends BaseRepository<PedidoModel> {

    public PedidoRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<PedidoModel> modelClass() {
        return PedidoModel.class;
    }

    public BigDecimal somarPedidosHoje() throws SQLException {
        long inicioHoje = localDateParaMillis(LocalDate.now());
        long fimHoje = inicioHoje + (24 * 60 * 60 * 1000L) - 1;
        return somarPedidosPorPeriodo(inicioHoje, fimHoje);
    }

    public BigDecimal somarPedidosPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        var pedidos = session().query(
                modelClass(),
                sql("SELECT * FROM pedidos WHERE dataCriacao BETWEEN ? AND ?"),
                params(dataInicio, dataFim)
        );
        return pedidos.stream()
                .map(PedidoModel::getTotalLiquido)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

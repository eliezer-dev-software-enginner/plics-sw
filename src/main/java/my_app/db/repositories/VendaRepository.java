package my_app.db.repositories;

import my_app.db.models.VendaModel;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;
import static my_app.utils.DateUtils.localDateParaMillis;

public class VendaRepository extends BaseRepository<VendaModel> {

    public VendaRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<VendaModel> modelClass() {
        return VendaModel.class;
    }

    public java.util.List<VendaModel> buscarPorCliente(Integer clienteId) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM vendas WHERE cliente_id = ? ORDER BY dataCriacao DESC"),
                params(clienteId)
        );
    }

    public BigDecimal somarVendasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        var vendas = session().query(
                modelClass(),
                sql("SELECT * FROM vendas WHERE dataCriacao BETWEEN ? AND ? AND tipo_pagamento != 'A PRAZO'"),
                params(dataInicio, dataFim)
        );
        return vendas.stream()
                .map(VendaModel::getTotalLiquido)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal somarVendasHoje() throws SQLException {
        long inicioHoje = localDateParaMillis(LocalDate.now());
        long fimHoje = inicioHoje + (24 * 60 * 60 * 1000L) - 1;
        return somarVendasPorPeriodo(inicioHoje, fimHoje);
    }
}

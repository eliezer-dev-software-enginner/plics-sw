package my_app.db.repositories;

import my_app.db.models.ContasPagarModel;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class ContasPagarRepository extends BaseRepository<ContasPagarModel> {

    public ContasPagarRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<ContasPagarModel> modelClass() {
        return ContasPagarModel.class;
    }

    public List<ContasPagarModel> buscarPorStatus(String status) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_pagar WHERE status = ? ORDER BY data_vencimento ASC"),
                params(status)
        );
    }

    public List<ContasPagarModel> buscarVencidas() throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_pagar WHERE status != 'PAGO' AND data_vencimento < ? ORDER BY data_vencimento ASC"),
                params(System.currentTimeMillis())
        );
    }

    public List<ContasPagarModel> buscarPorFornecedor(Integer fornecedorId) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_pagar WHERE fornecedor_id = ? ORDER BY data_vencimento ASC"),
                params(fornecedorId)
        );
    }

    public List<ContasPagarModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_pagar WHERE data_vencimento BETWEEN ? AND ? ORDER BY data_vencimento ASC"),
                params(dataInicio, dataFim)
        );
    }

    public List<ContasPagarModel> buscarPorCompra(Integer compraId) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_pagar WHERE compra_id = ? ORDER BY data_vencimento ASC"),
                params(compraId)
        );
    }

    public void excluirPorCompraId(Integer compraId) throws SQLException {
        var contas = buscarPorCompra(compraId);
        for (var conta : contas) {
            session().delete(conta);
        }
    }

    public BigDecimal somarDespesasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        var contas = session().query(
                modelClass(),
                sql("SELECT * FROM contas_pagar WHERE data_pagamento BETWEEN ? AND ?"),
                params(dataInicio, dataFim)
        );
        return contas.stream()
                .map(ContasPagarModel::getValorPago)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

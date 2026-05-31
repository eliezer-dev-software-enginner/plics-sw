package my_app.db.repositories;

import my_app.db.models.ContaAreceberModel;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class ContasAReceberRepository extends BaseRepository<ContaAreceberModel> {

    public ContasAReceberRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<ContaAreceberModel> modelClass() {
        return ContaAreceberModel.class;
    }

    public List<ContaAreceberModel> buscarPorStatus(String status) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_a_receber WHERE status = ? ORDER BY data_vencimento ASC"),
                params(status)
        );
    }

    public List<ContaAreceberModel> buscarVencidas() throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_a_receber WHERE status != 'PAGO' AND data_vencimento < ? ORDER BY data_vencimento ASC"),
                params(System.currentTimeMillis())
        );
    }

    public List<ContaAreceberModel> buscarPorCliente(Integer clienteId) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_a_receber WHERE cliente_id = ? ORDER BY data_vencimento ASC"),
                params(clienteId)
        );
    }

    public List<ContaAreceberModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_a_receber WHERE data_vencimento BETWEEN ? AND ? ORDER BY data_vencimento ASC"),
                params(dataInicio, dataFim)
        );
    }

    public List<ContaAreceberModel> buscarPorVenda(Integer vendaId) throws SQLException {
        return session().query(
                modelClass(),
                sql("SELECT * FROM contas_a_receber WHERE venda_id = ? ORDER BY data_vencimento ASC"),
                params(vendaId)
        );
    }

    public void excluirPorVendaId(Integer vendaId) throws SQLException {
        var contas = buscarPorVenda(vendaId);
        for (var conta : contas) {
            session().delete(conta);
        }
    }

    public BigDecimal somarReceitasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        var contas = session().query(
                modelClass(),
                sql("SELECT * FROM contas_a_receber WHERE data_recebimento BETWEEN ? AND ?"),
                params(dataInicio, dataFim)
        );
        return contas.stream()
                .map(ContaAreceberModel::getValorRecebido)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

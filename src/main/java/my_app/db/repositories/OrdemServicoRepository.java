package my_app.db.repositories;

import my_app.db.models.OrdemServicoModel;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;

public class OrdemServicoRepository extends BaseRepository<OrdemServicoModel> {

    public OrdemServicoRepository(Session session) {
        super(session);
    }

    @Override
    protected Class<OrdemServicoModel> modelClass() {
        return OrdemServicoModel.class;
    }

    public long gerarProximoNumeroOS() throws SQLException {
        var todas = session().query(modelClass(),
                sql("SELECT * FROM ordens_de_servico ORDER BY numero_os DESC LIMIT 1"));
        if (todas.isEmpty()) return 1001;
        var ultima = todas.getFirst();
        return Math.max(1001, (ultima.getNumeroOs() != null ? ultima.getNumeroOs() : 0L) + 1);
    }

    public List<OrdemServicoModel> buscarPorCliente(Integer clienteId) throws SQLException {
        return session().query(modelClass(),
                sql("SELECT * FROM ordens_de_servico WHERE cliente_id = ? ORDER BY dataCriacao DESC"),
                params(clienteId));
    }

    public List<OrdemServicoModel> buscarPorTecnico(Integer tecnicoId) throws SQLException {
        return session().query(modelClass(),
                sql("SELECT * FROM ordens_de_servico WHERE tecnico_id = ? ORDER BY dataCriacao DESC"),
                params(tecnicoId));
    }

    public List<OrdemServicoModel> buscarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return session().query(modelClass(),
                sql("SELECT * FROM ordens_de_servico WHERE data_escolhida BETWEEN ? AND ? ORDER BY data_escolhida ASC"),
                params(dataInicio, dataFim));
    }

    public List<OrdemServicoModel> buscarPorStatus(String status) throws SQLException {
        return session().query(modelClass(),
                sql("SELECT * FROM ordens_de_servico WHERE status = ? ORDER BY dataCriacao DESC"),
                params(status));
    }
}

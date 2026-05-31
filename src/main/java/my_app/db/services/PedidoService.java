package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PedidoModel;
import my_app.db.repositories.PedidoRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class PedidoService extends BaseService<PedidoModel> {

    public PedidoService() throws SQLException {
        this(DB.getPersismSession());
    }

    public PedidoService(Session session) {
        super(new PedidoRepository(session));
    }

    @Override
    public PedidoModel salvar(PedidoModel model) throws SQLException {
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }
}

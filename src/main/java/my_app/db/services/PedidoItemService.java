package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PedidoItemModel;
import my_app.db.repositories.PedidoItemRepository;
import net.sf.persism.Session;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class PedidoItemService extends BaseService<PedidoItemModel> {

    private final PedidoItemRepository pedidoItemRepository;

    public PedidoItemService() throws SQLException {
        this(DB.getPersismSession());
    }

    public PedidoItemService(Session session) {
        super(new PedidoItemRepository(session));
        this.pedidoItemRepository = (PedidoItemRepository) repository;
    }

    @Override
    public PedidoItemModel salvar(PedidoItemModel model) throws SQLException {
        model.setDataCriacao(LocalDateTime.now());
        return repository.salvar(model);
    }
}

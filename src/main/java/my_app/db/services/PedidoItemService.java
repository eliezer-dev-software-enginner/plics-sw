package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PedidoItemModel;
import my_app.db.repositories.PedidoItemRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoItemService extends BaseService<PedidoItemModel> {

    private static final Logger log = LoggerFactory.getLogger(PedidoItemService.class);

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
        // DEBUG, não INFO: um pedido do PDV pode ter dezenas de itens — logar cada um em
        // INFO inundaria o log sem agregar valor (o pedido em si já é logado por PDVService).
        var salvo = repository.salvar(model);
        log.debug("Item de pedido salvo: id={} pedidoId={} produtoCod={}", salvo.getId(), salvo.getPedidoId(), salvo.getProdutoCod());
        return salvo;
    }

    public List<PedidoItemModel> listarPorPedido(Integer pedidoId) throws SQLException {
        return pedidoItemRepository.listarPorPedido(pedidoId);
    }

    public List<PedidoItemModel> listarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return pedidoItemRepository.listarPorPeriodo(dataInicio, dataFim);
    }

    public void excluirPorPedidoId(Integer pedidoId) throws SQLException {
        pedidoItemRepository.excluirPorPedidoId(pedidoId);
    }
}

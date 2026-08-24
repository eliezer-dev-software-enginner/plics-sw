package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PedidoModel;
import my_app.db.repositories.PedidoRepository;
import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PedidoService extends BaseService<PedidoModel> {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;

    public PedidoService() throws SQLException {
        this(DB.getPersismSession());
    }

    public PedidoService(Session session) {
        super(new PedidoRepository(session));
        this.pedidoRepository = (PedidoRepository) repository;
    }

    @Override
    public PedidoModel salvar(PedidoModel model) throws SQLException {
        model.setDataCriacao(LocalDateTime.now());
        var salvo = repository.salvar(model);
        log.info("Pedido (PDV) salvo: id={} clienteId={} totalLiquido={}", salvo.getId(), salvo.getClienteId(), salvo.getTotalLiquido());
        return salvo;
    }

    public BigDecimal somarPedidosHoje() throws SQLException {
        return pedidoRepository.somarPedidosHoje();
    }

    public BigDecimal somarPedidosPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return pedidoRepository.somarPedidosPorPeriodo(dataInicio, dataFim);
    }

    public java.util.List<PedidoModel> listarPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return pedidoRepository.listarPorPeriodo(dataInicio, dataFim);
    }

    public java.util.List<PedidoModel> listarDevolvidasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        return pedidoRepository.listarDevolvidasPorPeriodo(dataInicio, dataFim);
    }
}

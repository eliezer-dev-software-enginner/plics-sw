package my_app.db.services;

import my_app.db.DB;
import my_app.db.models.PedidoModel;
import my_app.db.repositories.PedidoRepository;
import net.sf.persism.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PedidoService extends BaseService<PedidoModel> {

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
        return repository.salvar(model);
    }

    public BigDecimal somarPedidosHoje() throws SQLException {
        return pedidoRepository.somarPedidosHoje();
    }

    public BigDecimal somarPedidosPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        if (dataInicio >= dataFim)
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        return pedidoRepository.somarPedidosPorPeriodo(dataInicio, dataFim);
    }
}

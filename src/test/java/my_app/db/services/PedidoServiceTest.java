package my_app.db.services;

import my_app.db.models.PedidoModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest extends BaseServiceTest {

    private PedidoService pedidoService;

    @Override
    protected void initService() {
        pedidoService = new PedidoService(session);
    }

    private PedidoModel pedidoValido() {
        var p = new PedidoModel();
        p.setTotalLiquido(BigDecimal.valueOf(150));
        p.setFormaPagamento("DINHEIRO");
        return p;
    }

    @Test
    void deveSalvarPedido() throws Exception {
        var salvo = pedidoService.salvar(pedidoValido());
        assertNotNull(salvo.getId());
        assertNotNull(salvo.getDataCriacao());
    }

    @Test
    void deveLancarExcecaoAoSomarPorPeriodoComDataInicioMaior() {
        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.somarPedidosPorPeriodo(100L, 50L));
    }
}

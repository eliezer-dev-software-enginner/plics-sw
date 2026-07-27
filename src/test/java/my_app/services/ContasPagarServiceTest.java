package my_app.services;

import my_app.db.models.CompraModel;
import my_app.db.services.BaseServiceTest;
import my_app.domain.Parcela;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContasPagarServiceTest extends BaseServiceTest {

    private ContasPagarService contasPagarService;

    @Override
    protected void initService() {
        contasPagarService = new ContasPagarService(session);
    }

    private CompraModel compraValida() {
        var compra = new CompraModel();
        compra.setId(1L);
        compra.setFornecedorId(1);
        return compra;
    }

    @Test
    void deveLancarExcecaoAoGerarContasSemParcelas() {
        var erro = assertThrows(IllegalStateException.class,
                () -> contasPagarService.gerarContasDeCompra(compraValida(), List.of()));
        assertEquals("Parcelas não informadas", erro.getMessage());
    }

    @Test
    void deveLancarExcecaoAoGerarContasComCompraNula() {
        var erro = assertThrows(IllegalStateException.class,
                () -> contasPagarService.gerarContasDeCompra(null, List.of(new Parcela(1, 0L, BigDecimal.TEN))));
        assertEquals("Compra inválida", erro.getMessage());
    }

    @Test
    void deveGerarContasAPagarParaCadaParcela() throws Exception {
        var parcelas = List.of(
                new Parcela(1, 1000L, BigDecimal.valueOf(50)),
                new Parcela(2, 2000L, BigDecimal.valueOf(50))
        );

        contasPagarService.gerarContasDeCompra(compraValida(), parcelas);

        var contas = contasPagarService.buscarPorCompra(1L);
        assertEquals(2, contas.size());
    }
}

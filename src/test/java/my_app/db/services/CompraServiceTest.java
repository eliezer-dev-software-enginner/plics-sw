package my_app.db.services;

import my_app.db.dto.CompraDto;
import my_app.db.models.CompraModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CompraServiceTest extends BaseServiceTest {

    private CompraService compraService;

    @Override
    protected void initService() {
        compraService = new CompraService(session);
    }

    private CompraDto dtoValido() {
        return new CompraDto(
                "789", BigDecimal.TEN, 1, BigDecimal.ONE,
                BigDecimal.ZERO, "DINHEIRO", "obs",
                System.currentTimeMillis(), "12345", null, "SIM", BigDecimal.TEN
        );
    }

    @Test
    void deveConverterDtoParaModel() {
        var dto = dtoValido();
        CompraModel model = compraService.toModel(dto);
        assertEquals("789", model.getProdutoCod());
        assertEquals(Integer.valueOf(1), model.getFornecedorId());
        assertEquals(BigDecimal.ONE, model.getQuantidade());
    }

    @Test
    void deveConverterDtoParaModelComId() {
        var dto = dtoValido();
        CompraModel model = compraService.toModel(dto, 42L, 1000L);
        assertEquals(42L, model.getId());
        assertEquals(1000L, model.getDataCriacaoMillis());
    }

    @Test
    void deveSalvarCompraViaDto() throws Exception {
        var salvo = compraService.salvar(dtoValido());
        assertTrue(salvo.getDataCriacaoMillis() > 0);
    }

    @Test
    void deveLancarExcecaoAoSomarPorPeriodoComDataInicioMaiorQueFim() {
        assertThrows(IllegalArgumentException.class,
                () -> compraService.somarComprasPorPeriodo(100L, 50L));
    }

    @Test
    void deveSomarComprasPorPeriodo() throws Exception {
        compraService.salvar(dtoValido());
        var soma = compraService.somarComprasPorPeriodo(0L, System.currentTimeMillis() + 1000);
        assertBigDecimalEquals(BigDecimal.TEN, soma);
    }

    @Test
    void deveAtualizarCompra() throws Exception {
        var salvo = compraService.salvar(dtoValido());
        salvo.setObservacao("Atualizada");
        compraService.atualizar(salvo);
        var buscado = compraService.buscarById(salvo.getId());
        assertEquals("Atualizada", buscado.getObservacao());
    }
}

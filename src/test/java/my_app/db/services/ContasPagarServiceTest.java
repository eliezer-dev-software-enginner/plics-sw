package my_app.db.services;

import my_app.db.models.ContasPagarModel;
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

    private ContasPagarModel contaValida() {
        var c = new ContasPagarModel();
        c.setDescricao("Compra #1");
        c.setValorOriginal(BigDecimal.valueOf(200));
        c.setValorPago(BigDecimal.ZERO);
        c.setValorRestante(BigDecimal.valueOf(200));
        c.setDataVencimento(System.currentTimeMillis() + 86400000L);
        c.setStatus("PENDENTE");
        return c;
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoVazia() {
        var c = contaValida();
        c.setDescricao("");
        assertThrows(IllegalArgumentException.class, () -> contasPagarService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoValorOriginalZero() {
        var c = contaValida();
        c.setValorOriginal(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> contasPagarService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoDataVencimentoNull() {
        var c = contaValida();
        c.setDataVencimento(null);
        assertThrows(IllegalArgumentException.class, () -> contasPagarService.salvar(c));
    }

    @Test
    void deveSalvarConta() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        assertNotNull(salvo.getId());
    }

    @Test
    void deveRegistrarPagamentoTotal() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(200));
        var atualizada = contasPagarService.buscarById(salvo.getId());
        assertEquals("PAGO", atualizada.getStatus());
        assertBigDecimalEquals(BigDecimal.ZERO, atualizada.getValorRestante());
    }

    @Test
    void deveRegistrarPagamentoParcial() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(50));
        var atualizada = contasPagarService.buscarById(salvo.getId());
        assertEquals("PARCIAL", atualizada.getStatus());
        assertBigDecimalEquals(BigDecimal.valueOf(150), atualizada.getValorRestante());
    }

    @Test
    void deveLancarExcecaoAoPagarMaisQueRestante() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(300)));
    }

    @Test
    void deveLancarExcecaoAoPagarContaJaPaga() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(200));
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(50)));
    }

    @Test
    void deveLancarExcecaoAoPagarContaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.registrarPagamento(999L, BigDecimal.TEN));
    }

    @Test
    void deveLancarExcecaoQuandoValorPagoMenorOuIgualZero() {
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.registrarPagamento(1L, BigDecimal.ZERO));
    }

    @Test
    void deveCancelarPagamento() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(200));
        contasPagarService.cancelarPagamento(salvo.getId());
        var atualizada = contasPagarService.buscarById(salvo.getId());
        assertEquals("PENDENTE", atualizada.getStatus());
        assertBigDecimalEquals(BigDecimal.ZERO, atualizada.getValorPago());
    }

    @Test
    void deveLancarExcecaoAoCancelarContaPendente() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.cancelarPagamento(salvo.getId()));
    }

    @Test
    void deveLancarExcecaoAoExcluirContaPaga() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(200));
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.excluir(salvo.getId()));
    }

    @Test
    void deveBuscarPorStatus() throws Exception {
        contasPagarService.salvar(contaValida());
        List<ContasPagarModel> pendentes = contasPagarService.buscarPorStatus("PENDENTE");
        assertEquals(1, pendentes.size());
    }

    @Test
    void deveBuscarPorPeriodo() throws Exception {
        contasPagarService.salvar(contaValida());
        var contas = contasPagarService.buscarPorPeriodo(0L, System.currentTimeMillis() + 86400000L * 2);
        assertFalse(contas.isEmpty());
    }

    @Test
    void deveLancarExcecaoAoBuscarPorPeriodoComDataInicioMaior() {
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.buscarPorPeriodo(100L, 50L));
    }

    @Test
    void deveCalcularTotalEmAberto() throws Exception {
        contasPagarService.salvar(contaValida());
        assertBigDecimalEquals(BigDecimal.valueOf(200), contasPagarService.getTotalEmAberto());
    }

    @Test
    void deveSomarDespesasPorPeriodo() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        contasPagarService.registrarPagamento(salvo.getId(), BigDecimal.valueOf(200));
        var soma = contasPagarService.somarDespesasPorPeriodo(0L, System.currentTimeMillis() + 86400000L * 2);
        assertBigDecimalEquals(BigDecimal.valueOf(200), soma);
    }

    @Test
    void deveLancarExcecaoAoSomarDespesasComDataInicioMaior() {
        assertThrows(IllegalArgumentException.class,
                () -> contasPagarService.somarDespesasPorPeriodo(100L, 50L));
    }

    @Test
    void deveBuscarPorFornecedor() throws Exception {
        var salvo = contasPagarService.salvar(contaValida());
        salvo.setFornecedorId(1);
        contasPagarService.atualizar(salvo);
        assertFalse(contasPagarService.buscarPorFornecedor(1).isEmpty());
    }
}

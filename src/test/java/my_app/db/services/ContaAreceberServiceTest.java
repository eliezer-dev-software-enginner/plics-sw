package my_app.db.services;

import my_app.db.models.ContaAreceberModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContaAreceberServiceTest extends BaseServiceTest {

    private ContaAreceberService contaService;

    @Override
    protected void initService() {
        contaService = new ContaAreceberService(session);
    }

    private ContaAreceberModel contaValida() {
        var c = new ContaAreceberModel();
        c.setDescricao("Venda #1");
        c.setValorOriginal(BigDecimal.valueOf(100));
        c.setValorRecebido(BigDecimal.ZERO);
        c.setValorRestante(BigDecimal.valueOf(100));
        c.setDataVencimento(System.currentTimeMillis() + 86400000L);
        c.setStatus("PENDENTE");
        return c;
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoVazia() {
        var c = contaValida();
        c.setDescricao("");
        assertThrows(IllegalArgumentException.class, () -> contaService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoValorOriginalZero() {
        var c = contaValida();
        c.setValorOriginal(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> contaService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoDataVencimentoNull() {
        var c = contaValida();
        c.setDataVencimento(null);
        assertThrows(IllegalArgumentException.class, () -> contaService.salvar(c));
    }

    @Test
    void deveSalvarConta() throws Exception {
        var salvo = contaService.salvar(contaValida());
        assertNotNull(salvo.getId());
    }

    @Test
    void deveRegistrarRecebimentoTotal() throws Exception {
        var salvo = contaService.salvar(contaValida());
        contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(100));
        var atualizada = contaService.buscarById(salvo.getId());
        assertEquals("PAGO", atualizada.getStatus());
        assertBigDecimalEquals(BigDecimal.ZERO, atualizada.getValorRestante());
    }

    @Test
    void deveRegistrarRecebimentoParcial() throws Exception {
        var salvo = contaService.salvar(contaValida());
        contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(30));
        var atualizada = contaService.buscarById(salvo.getId());
        assertEquals("PARCIAL", atualizada.getStatus());
        assertBigDecimalEquals(BigDecimal.valueOf(70), atualizada.getValorRestante());
    }

    @Test
    void deveLancarExcecaoAoReceberMaisQueRestante() throws Exception {
        var salvo = contaService.salvar(contaValida());
        assertThrows(IllegalArgumentException.class,
                () -> contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(200)));
    }

    @Test
    void deveLancarExcecaoAoReceberContaJaPaga() throws Exception {
        var salvo = contaService.salvar(contaValida());
        contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(100));
        assertThrows(IllegalArgumentException.class,
                () -> contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(50)));
    }

    @Test
    void deveLancarExcecaoAoReceberContaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> contaService.registrarRecebimento(999L, BigDecimal.TEN));
    }

    @Test
    void deveLancarExcecaoQuandoValorRecebidoMenorOuIgualZero() {
        assertThrows(IllegalArgumentException.class,
                () -> contaService.registrarRecebimento(1L, BigDecimal.ZERO));
    }

    @Test
    void deveCancelarRecebimento() throws Exception {
        var salvo = contaService.salvar(contaValida());
        contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(100));
        contaService.cancelarRecebimento(salvo.getId());
        var atualizada = contaService.buscarById(salvo.getId());
        assertEquals("PENDENTE", atualizada.getStatus());
        assertBigDecimalEquals(BigDecimal.ZERO, atualizada.getValorRecebido());
    }

    @Test
    void deveLancarExcecaoAoCancelarContaPendente() throws Exception {
        var salvo = contaService.salvar(contaValida());
        assertThrows(IllegalArgumentException.class,
                () -> contaService.cancelarRecebimento(salvo.getId()));
    }

    @Test
    void deveLancarExcecaoAoExcluirContaPaga() throws Exception {
        var salvo = contaService.salvar(contaValida());
        contaService.registrarRecebimento(salvo.getId(), BigDecimal.valueOf(100));
        assertThrows(IllegalArgumentException.class,
                () -> contaService.excluir(salvo.getId()));
    }

    @Test
    void deveBuscarPorStatus() throws Exception {
        contaService.salvar(contaValida());
        List<ContaAreceberModel> pendentes = contaService.buscarPorStatus("PENDENTE");
        assertEquals(1, pendentes.size());
    }

    @Test
    void deveLancarExcecaoAoGerarContasComVendaNull() {
        assertThrows(IllegalArgumentException.class,
                () -> contaService.gerarContasDeVenda(null, 1, List.of()));
    }

    @Test
    void deveLancarExcecaoAoGerarContasComParcelasVazias() {
        assertThrows(IllegalArgumentException.class,
                () -> contaService.gerarContasDeVenda(1, 1, List.of()));
    }

    @Test
    void deveCalcularTotalEmAberto() throws Exception {
        contaService.salvar(contaValida());
        assertBigDecimalEquals(BigDecimal.valueOf(100), contaService.getTotalEmAberto());
    }

    @Test
    void deveExcluirPorVendaId() throws Exception {
        var salvo = contaService.salvar(contaValida());
        salvo.setVendaId(1);
        contaService.atualizar(salvo);
        contaService.excluirPorVendaId(1);
        assertEquals(0, contaService.listar().size());
    }

    @Test
    void deveBuscarPorPeriodo() throws Exception {
        contaService.salvar(contaValida());
        var contas = contaService.buscarPorPeriodo(0L, System.currentTimeMillis() + 86400000L * 2);
        assertFalse(contas.isEmpty());
    }

    @Test
    void deveLancarExcecaoAoBuscarPorPeriodoComDataInicioMaior() {
        assertThrows(IllegalArgumentException.class,
                () -> contaService.buscarPorPeriodo(100L, 50L));
    }
}

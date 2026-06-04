package my_app.db.services;

import my_app.db.models.OrdemServicoModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoServiceTest extends BaseServiceTest {

    private OrdemServicoService ordemServicoService;

    @Override
    protected void initService() {
        ordemServicoService = new OrdemServicoService(session);
    }

    private OrdemServicoModel osValida() {
        var o = new OrdemServicoModel();
        o.setClienteId(1);
        o.setTecnicoId(1);
        o.setEquipamento("Notebook");
        o.setTotalLiquido(BigDecimal.valueOf(500));
        o.setDataEscolhida(System.currentTimeMillis());
        return o;
    }

    @Test
    void deveLancarExcecaoQuandoClienteNull() {
        var o = osValida();
        o.setClienteId(null);
        assertThrows(IllegalArgumentException.class, () -> ordemServicoService.salvar(o));
    }

    @Test
    void deveLancarExcecaoQuandoTecnicoNull() {
        var o = osValida();
        o.setTecnicoId(null);
        assertThrows(IllegalArgumentException.class, () -> ordemServicoService.salvar(o));
    }

    @Test
    void deveLancarExcecaoQuandoEquipamentoVazio() {
        var o = osValida();
        o.setEquipamento("");
        assertThrows(IllegalArgumentException.class, () -> ordemServicoService.salvar(o));
    }

    @Test
    void deveLancarExcecaoQuandoTotalLiquidoNegativo() {
        var o = osValida();
        o.setTotalLiquido(BigDecimal.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> ordemServicoService.salvar(o));
    }

    @Test
    void deveSalvarOS() throws Exception {
        var salvo = ordemServicoService.salvar(osValida());
        assertNotNull(salvo.getId());
        assertNotNull(salvo.getNumeroOs());
    }

    @Test
    void deveGerarNumeroOSAutomaticamente() throws Exception {
        var o1 = ordemServicoService.salvar(osValida());
        var o2 = ordemServicoService.salvar(osValida());
        assertNotNull(o1.getNumeroOs());
        assertTrue(o2.getNumeroOs() > o1.getNumeroOs());
    }

    @Test
    void deveAtualizarOS() throws Exception {
        var salvo = ordemServicoService.salvar(osValida());
        salvo.setEquipamento("Desktop");
        ordemServicoService.atualizar(salvo);
        var buscado = ordemServicoService.buscarById(salvo.getId());
        assertEquals("Desktop", buscado.getEquipamento());
    }

    @Test
    void deveExcluirOS() throws Exception {
        var salvo = ordemServicoService.salvar(osValida());
        ordemServicoService.excluir(salvo.getId());
        assertNull(ordemServicoService.buscarById(salvo.getId()));
    }

    @Test
    void deveLancarExcecaoAoExcluirOsInexistente() {
        assertThrows(IllegalArgumentException.class, () -> ordemServicoService.excluir(999L));
    }

    @Test
    void deveBuscarPorCliente() throws Exception {
        ordemServicoService.salvar(osValida());
        assertFalse(ordemServicoService.buscarPorCliente(1).isEmpty());
    }

    @Test
    void deveBuscarPorTecnico() throws Exception {
        ordemServicoService.salvar(osValida());
        assertFalse(ordemServicoService.buscarPorTecnico(1).isEmpty());
    }

    @Test
    void deveBuscarPorPeriodo() throws Exception {
        ordemServicoService.salvar(osValida());
        var resultados = ordemServicoService.buscarPorPeriodo(0L, System.currentTimeMillis() + 1000);
        assertFalse(resultados.isEmpty());
    }

    @Test
    void deveBuscarPorStatus() throws Exception {
        var salvo = ordemServicoService.salvar(osValida());
        var resultados = ordemServicoService.buscarPorStatus(salvo.getStatus());
        assertFalse(resultados.isEmpty());
    }

    @Test
    void deveDefinirStatusPadrao() throws Exception {
        var salvo = ordemServicoService.salvar(osValida());
        assertEquals("Orçamento", salvo.getStatus());
    }

    @Test
    void deveDefinirValoresPadraoParaMaoDeObraEPecas() throws Exception {
        var salvo = ordemServicoService.salvar(osValida());
        assertEquals(BigDecimal.ZERO, salvo.getMaoDeObraValor());
        assertEquals(BigDecimal.ZERO, salvo.getPecasValor());
    }
}

package my_app.screens.ordemServicoScreen;

import my_app.db.models.ClienteModel;
import my_app.db.models.TecnicoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.OrdemServicoService;
import my_app.db.services.TecnicoService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoScreenViewModelTest extends BaseViewModelTest {

    private OrdemServicoScreenViewModel vm;
    private OrdemServicoService ordemServicoService;
    private ClienteService clienteService;
    private TecnicoService tecnicoService;

    @Override
    protected void initService() {
        ordemServicoService = new OrdemServicoService(session);
        clienteService = new ClienteService(session);
        tecnicoService = new TecnicoService(session);
        vm = new OrdemServicoScreenViewModel(null, ordemServicoService, clienteService, tecnicoService);
    }

    private void prepararDependencias() throws Exception {
        var t = new TecnicoModel();
        t.setNome("Técnico Teste");
        var tecnico = tecnicoService.salvar(t);

        var c = new ClienteModel();
        c.setNome("Cliente Teste");
        c.setEmail("");
        c.setCelular("");
        var cliente = clienteService.salvar(c);

        vm.tecnicoSelected.set(tecnico);
        vm.clienteSelected.set(cliente);
    }

    @Test
    void deveSalvarOrdemServico() throws Exception {
        prepararDependencias();
        vm.equipamento.set("Notebook Dell");
        vm.maoDeObra.set("30000");
        vm.pecasValor.set("20000");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = ordemServicoService.listar();
        assertEquals(1, list.size());
        assertEquals("Notebook Dell", list.get(0).getEquipamento());
    }

    @Test
    void deveLancarExcecaoQuandoEquipamentoVazio() throws Exception {
        prepararDependencias();
        vm.equipamento.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        assertEquals(0, ordemServicoService.listar().size());
    }
}

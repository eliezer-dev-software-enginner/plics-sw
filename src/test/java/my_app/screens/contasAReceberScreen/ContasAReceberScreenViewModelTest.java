package my_app.screens.contasAReceberScreen;

import my_app.db.models.ClienteModel;
import my_app.db.models.ContaAreceberModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ContaAreceberService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContasAReceberScreenViewModelTest extends BaseViewModelTest {

    private ContasAReceberScreenViewModel vm;
    private ContaAreceberService contaService;
    private ClienteService clienteService;

    private ClienteModel cliente;

    @Override
    protected void initService() {
        contaService = new ContaAreceberService(session);
        clienteService = new ClienteService(session);
        vm = new ContasAReceberScreenViewModel(null, contaService, clienteService);
    }

    @Test
    void deveSalvarConta() throws Exception {
        cliente = new ClienteModel();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setCelular("11999999999");
        clienteService.salvar(cliente);

        vm.loadInicial();
        waitForAsync();

        vm.clienteSelected.set(cliente);
        vm.descricao.set("Serviço prestado");
        vm.valorOriginal.set("50000"); // R$ 500,00
        vm.dataVencimento.set(LocalDate.now().plusDays(30));

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = contaService.listar();
        assertEquals(1, list.size());
        assertEquals("Serviço prestado", list.getFirst().getDescricao());
    }

    @Test
    void deveValidarFormularioInvalido() throws Exception {
        vm.descricao.set("");
        vm.valorOriginal.set("0");

        try {
            vm.handleAddOrUpdate();
        } catch (IllegalStateException ignored) {
        }
        waitForAsync();

        assertEquals(0, contaService.listar().size());
    }

    @Test
    void deveCarregarContas() throws Exception {
        var conta = new ContaAreceberModel();
        conta.setDescricao("Recebimento teste");
        conta.setValorOriginal(new BigDecimal("300.00"));
        conta.setValorRestante(new BigDecimal("300.00"));
        conta.setValorRecebido(BigDecimal.ZERO);
        conta.setDataVencimento(System.currentTimeMillis());
        conta.setStatus("PENDENTE");
        contaService.salvar(conta);

        vm.loadInicial();
        waitForAsync();

        var list = contaService.listar();
        assertFalse(list.isEmpty());
    }
}

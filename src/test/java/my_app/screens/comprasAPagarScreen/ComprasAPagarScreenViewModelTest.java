package my_app.screens.comprasAPagarScreen;

import my_app.db.models.ContasPagarModel;
import my_app.db.models.FornecedorModel;
import my_app.db.services.ContasPagarService;
import my_app.db.services.FornecedorService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ComprasAPagarScreenViewModelTest extends BaseViewModelTest {

    private ComprasAPagarScreenViewModel vm;
    private ContasPagarService contaService;
    private FornecedorService fornecedorService;

    private FornecedorModel fornecedor;

    @Override
    protected void initService() {
        contaService = new ContasPagarService(session);
        fornecedorService = new FornecedorService(session);
        vm = new ComprasAPagarScreenViewModel(null, contaService, fornecedorService);
    }

    @Test
    void deveSalvarConta() throws Exception {
        fornecedor = new FornecedorModel();
        fornecedor.setNome("Fornecedor Teste");
        fornecedorService.salvar(fornecedor);

        vm.loadInicial();
        waitForAsync();

        vm.fornecedorSelected.set(fornecedor);
        vm.descricao.set("Conta de energia");
        vm.valorOriginal.set("15000"); // R$ 150,00 em centavos
        vm.dataVencimento.set(LocalDate.now().plusDays(30));

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = contaService.listar();
        assertEquals(1, list.size());
        assertEquals("Conta de energia", list.getFirst().getDescricao());
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
        fornecedor = new FornecedorModel();
        fornecedor.setNome("Fornecedor Teste");
        fornecedorService.salvar(fornecedor);

        var conta = new ContasPagarModel();
        conta.setDescricao("Conta teste");
        conta.setValorOriginal(new BigDecimal("100.00"));
        conta.setValorRestante(new BigDecimal("100.00"));
        conta.setValorPago(BigDecimal.ZERO);
        conta.setDataVencimento(System.currentTimeMillis());
        conta.setStatus("PENDENTE");
        conta.setFornecedorId(fornecedor.getId());
        contaService.salvar(conta);

        vm.loadInicial();
        waitForAsync();

        var list = contaService.listar();
        assertFalse(list.isEmpty());
    }
}

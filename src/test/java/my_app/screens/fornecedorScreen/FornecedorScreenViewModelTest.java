package my_app.screens.fornecedorScreen;

import my_app.db.services.FornecedorService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FornecedorScreenViewModelTest extends BaseViewModelTest {

    private FornecedorScreenViewModel vm;
    private FornecedorService fornecedorService;

    @Override
    protected void initService() {
        fornecedorService = new FornecedorService(session);
        vm = new FornecedorScreenViewModel(null, fornecedorService);
    }

    @Test
    void deveSalvarFornecedor() throws Exception {
        vm.nome.set("Distribuidora XYZ");
        vm.cnpj.set("12345678901234");
        vm.celular.set("");
        vm.email.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = fornecedorService.listar();
        assertEquals(1, list.size());
        assertEquals("Distribuidora XYZ", list.get(0).getNome());
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        vm.nome.set("");
        assertThrows(RuntimeException.class, () -> vm.handleAddOrUpdate());
    }

    @Test
    void deveLancarExcecaoQuandoCnpjInvalido() throws Exception {
        vm.nome.set("Fornecedor B");
        vm.cnpj.set("123");

        vm.handleAddOrUpdate();
        waitForAsync();

        assertEquals(0, fornecedorService.listar().size());
    }
}

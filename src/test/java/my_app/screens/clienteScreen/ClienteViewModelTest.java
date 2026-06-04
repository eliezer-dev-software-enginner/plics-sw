package my_app.screens.clienteScreen;

import my_app.db.services.ClienteService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteViewModelTest extends BaseViewModelTest {

    private ClienteViewModel vm;
    private ClienteService clienteService;

    @Override
    protected void initService() {
        clienteService = new ClienteService(session);
        vm = new ClienteViewModel(null, clienteService);
    }

    @Test
    void deveSalvarCliente() throws Exception {
        vm.nome.set("Maria Silva");
        vm.email.set("maria@email.com");
        vm.celular.set("11988887777");
        vm.cnpjCpf.set("");
        vm.tipoPessoaEhFisica.get(); // true by default

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = clienteService.listar();
        assertEquals(1, list.size());
        assertEquals("Maria Silva", list.get(0).getNome());
        assertEquals("maria@email.com", list.get(0).getEmail());
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() throws Exception {
        vm.nome.set("");
        vm.email.set("");
        vm.celular.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        assertEquals(0, clienteService.listar().size());
    }

    @Test
    void deveLancarExcecaoQuandoEmailInvalido() throws Exception {
        vm.nome.set("João");
        vm.email.set("email-invalido");
        vm.celular.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        assertEquals(0, clienteService.listar().size());
    }
}

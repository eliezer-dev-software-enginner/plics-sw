package my_app.db.services;

import my_app.db.models.ClienteModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteServiceTest extends BaseServiceTest {

    private ClienteService clienteService;

    @Override
    protected void initService() {
        clienteService = new ClienteService(session);
    }

    private ClienteModel clienteValido() {
        var c = new ClienteModel();
        c.setNome("João");
        c.setEmail("");
        c.setCelular("");
        c.setPessoaFisica(true);
        return c;
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        var c = clienteValido();
        c.setNome("");
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoEmailInvalido() {
        var c = clienteValido();
        c.setEmail("invalido");
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoCelularInvalido() {
        var c = clienteValido();
        c.setCelular("123");
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c));
    }

    @Test
    void deveSalvarCliente() throws Exception {
        var salvo = clienteService.salvar(clienteValido());
        assertNotNull(salvo.getId());
        assertNotNull(salvo.getDataCriacao());
    }

    @Test
    void deveAceitarEmailValido() throws Exception {
        var c = clienteValido();
        c.setEmail("joao@email.com");
        var salvo = clienteService.salvar(c);
        assertEquals("joao@email.com", salvo.getEmail());
    }

    @Test
    void deveAceitarCelularValido() throws Exception {
        var c = clienteValido();
        c.setCelular("11999999999");
        var salvo = clienteService.salvar(c);
        assertEquals("11999999999", salvo.getCelular());
    }

    @Test
    void deveAtualizarCliente() throws Exception {
        var c = clienteService.salvar(clienteValido());
        c.setNome("João Atualizado");
        clienteService.atualizar(c);
        var buscado = clienteService.buscarById(c.getId());
        assertEquals("João Atualizado", buscado.getNome());
    }

    @Test
    void devePermitirEmailVazio() throws Exception {
        var c = clienteValido();
        c.setEmail("");
        assertDoesNotThrow(() -> clienteService.salvar(c));
    }

    @Test
    void devePermitirCelularVazio() throws Exception {
        var c = clienteValido();
        c.setCelular("");
        assertDoesNotThrow(() -> clienteService.salvar(c));
    }

    @Test
    void deveLancarExcecaoQuandoCpfCnpjDuplicado() throws Exception {
        var c1 = clienteValido();
        c1.setCpfCnpj("123.456.789-00");
        clienteService.salvar(c1);

        var c2 = clienteValido();
        c2.setCpfCnpj("123.456.789-00");
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c2));
    }

    @Test
    void devePermitirSalvarClienteComCpfCnpjUnico() throws Exception {
        var c1 = clienteValido();
        c1.setCpfCnpj("123.456.789-00");
        clienteService.salvar(c1);

        var c2 = clienteValido();
        c2.setCpfCnpj("987.654.321-00");
        assertDoesNotThrow(() -> clienteService.salvar(c2));
    }

    @Test
    void devePermitirAtualizarClienteMantendoCpfCnpj() throws Exception {
        var c = clienteValido();
        c.setCpfCnpj("123.456.789-00");
        var salvo = clienteService.salvar(c);

        salvo.setNome("João Editado");
        assertDoesNotThrow(() -> clienteService.atualizar(salvo));
    }

    @Test
    void deveLancarExcecaoQuandoCpfCnpjJaExisteEmOutroCliente() throws Exception {
        var c1 = clienteValido();
        c1.setCpfCnpj("123.456.789-00");
        clienteService.salvar(c1);

        var c2 = clienteValido();
        c2.setCpfCnpj("987.654.321-00");
        var salvo2 = clienteService.salvar(c2);

        salvo2.setCpfCnpj("123.456.789-00");
        assertThrows(IllegalArgumentException.class, () -> clienteService.atualizar(salvo2));
    }
}

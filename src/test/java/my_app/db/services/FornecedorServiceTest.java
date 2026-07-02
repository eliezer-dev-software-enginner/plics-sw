package my_app.db.services;

import my_app.db.models.FornecedorModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FornecedorServiceTest extends BaseServiceTest {

    private FornecedorService fornecedorService;

    @Override
    protected void initService() {
        fornecedorService = new FornecedorService(session);
    }

    private FornecedorModel fornecedorValido() {
        var f = new FornecedorModel();
        f.setNome("Fornecedor A");
        f.setCpfCnpj("11222333000181");
        return f;
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        var f = fornecedorValido();
        f.setNome("");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(f));
    }

    @Test
    void deveLancarExcecaoQuandoCnpjInvalido() {
        var f = fornecedorValido();
        f.setCpfCnpj("123");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(f));
    }

    @Test
    void deveLancarExcecaoQuandoEmailInvalido() {
        var f = fornecedorValido();
        f.setEmail("invalido");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(f));
    }

    @Test
    void deveLancarExcecaoQuandoCelularInvalido() {
        var f = fornecedorValido();
        f.setCelular("123");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(f));
    }

    @Test
    void deveSalvarFornecedor() throws Exception {
        var salvo = fornecedorService.salvar(fornecedorValido());
        assertNotNull(salvo.getId());
        assertNotNull(salvo.getDataCriacao());
    }

    @Test
    void devePermitirCnpjVazio() throws Exception {
        var f = fornecedorValido();
        f.setCpfCnpj("");
        assertDoesNotThrow(() -> fornecedorService.salvar(f));
    }

    @Test
    void deveLancarExcecaoQuandoCnpjDuplicado() throws Exception {
        fornecedorService.salvar(fornecedorValido());
        var f2 = fornecedorValido();
        f2.setNome("Fornecedor B");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(f2));
    }

    @Test
    void devePermitirCnpjDuplicadoNaMesmaEntidadeAoAtualizar() throws Exception {
        var salvo = fornecedorService.salvar(fornecedorValido());
        assertDoesNotThrow(() -> fornecedorService.atualizar(salvo));
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaCnpjJaExistente() throws Exception {
        fornecedorService.salvar(fornecedorValido());
        var f2 = new FornecedorModel();
        f2.setNome("Fornecedor B");
        f2.setCpfCnpj("99887766554429");
        var salvo2 = fornecedorService.salvar(f2);
        salvo2.setCpfCnpj("12345678901234");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.atualizar(salvo2));
    }

    @Test
    void deveAceitarEmailValido() throws Exception {
        var f = fornecedorValido();
        f.setEmail("contato@fornecedor.com");
        var salvo = fornecedorService.salvar(f);
        assertEquals("contato@fornecedor.com", salvo.getEmail());
    }

    @Test
    void deveAceitarCelularValido() throws Exception {
        var f = fornecedorValido();
        f.setCelular("11988887777");
        var salvo = fornecedorService.salvar(f);
        assertEquals("11988887777", salvo.getCelular());
    }

    @Test
    void deveAceitarCpfValido() throws Exception {
        var f = new FornecedorModel();
        f.setNome("Fornecedor PF");
        f.setCpfCnpj("12345678901");
        var salvo = fornecedorService.salvar(f);
        assertNotNull(salvo.getId());
        assertEquals("12345678901", salvo.getCpfCnpj());
    }

    @Test
    void deveRejeitarCpfInvalido() {
        var f = new FornecedorModel();
        f.setNome("Fornecedor PF");
        f.setCpfCnpj("1234567890");
        assertThrows(IllegalArgumentException.class, () -> fornecedorService.salvar(f));
    }

    @Test
    void deveAceitarCpfNoUpdate() throws Exception {
        var f = new FornecedorModel();
        f.setNome("Fornecedor PF");
        f.setCpfCnpj("12345678901");
        var salvo = fornecedorService.salvar(f);
        assertDoesNotThrow(() -> fornecedorService.atualizar(salvo));
    }
}

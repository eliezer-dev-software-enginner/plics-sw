package my_app.screens.fornecedorScreen;

import my_app.db.services.FornecedorService;
import my_app.domain.Data;
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
    void tipoPessoaSelected_iniciaComoFisica() {
        assertEquals("Física", vm.tipoPessoaSelected.get());
    }

    @Test
    void tipoPessoaEhFisica_retornaTrueQuandoFisica() {
        vm.tipoPessoaSelected.set("Física");
        assertTrue(vm.tipoPessoaEhFisica.get());
    }

    @Test
    void tipoPessoaEhFisica_retornaFalseQuandoJuridica() {
        vm.tipoPessoaSelected.set("Jurídica");
        assertFalse(vm.tipoPessoaEhFisica.get());
    }

    @Test
    void deveSalvarFornecedorPessoaFisica() throws Exception {
        vm.nome.set("João Fornecedor");
        vm.cnpjCpf.set("12345678901");
        vm.celular.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = fornecedorService.listar();
        assertEquals(1, list.size());
        assertEquals("João Fornecedor", list.get(0).getNome());
        assertEquals("12345678901", list.get(0).getCpfCnpj());
    }

    @Test
    void deveSalvarFornecedorPessoaJuridica() throws Exception {
        vm.tipoPessoaSelected.set("Jurídica");
        vm.nome.set("Empresa XYZ Ltda");
        vm.cnpjCpf.set("11222333000181");
        vm.celular.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = fornecedorService.listar();
        assertEquals(1, list.size());
        assertEquals("Empresa XYZ Ltda", list.get(0).getNome());
        assertEquals("11222333000181", list.get(0).getCpfCnpj());
    }

    @Test
    void deveSalvarFornecedorSemCpfCnpj() throws Exception {
        vm.nome.set("Fornecedor Sem Doc");
        vm.cnpjCpf.set("");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = fornecedorService.listar();
        assertEquals(1, list.size());
        assertEquals("Fornecedor Sem Doc", list.get(0).getNome());
    }

    @Test
    void deveRejeitarNomeVazio() {
        vm.nome.set("");
        assertThrows(RuntimeException.class, () -> vm.handleAddOrUpdate());
    }

    @Test
    void clearForm_resetaCampos() {
        vm.nome.set("Teste");
        vm.cnpjCpf.set("12345678901");
        vm.celular.set("11988887777");
        vm.email.set("teste@teste.com");
        vm.inscricaoEstadual.set("123");
        vm.cidade.set("São Paulo");
        vm.bairro.set("Centro");
        vm.rua.set("Rua A");
        vm.numero.set("100");
        vm.observacao.set("obs");

        vm.clearForm();

        assertEquals("", vm.nome.get());
        assertEquals("", vm.cnpjCpf.get());
        assertEquals("", vm.celular.get());
        assertEquals("", vm.inscricaoEstadual.get());
        assertEquals("", vm.email.get());
        assertEquals(Data.ufList.getFirst(), vm.ufSelected.get());
        assertEquals("", vm.cidade.get());
        assertEquals("", vm.bairro.get());
        assertEquals("", vm.rua.get());
        assertEquals("", vm.numero.get());
        assertEquals("", vm.observacao.get());
    }
}

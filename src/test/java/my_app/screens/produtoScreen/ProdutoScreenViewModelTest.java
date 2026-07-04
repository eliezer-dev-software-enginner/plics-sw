package my_app.screens.produtoScreen;

import my_app.db.models.CategoriaModel;
import my_app.db.models.FornecedorModel;
import my_app.db.services.CategoriaService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoScreenViewModelTest extends BaseViewModelTest {

    private ProdutoScreenViewModel vm;
    private ProdutoService produtoService;
    private FornecedorService fornecedorService;
    private CategoriaService categoriaService;

    @Override
    protected void initService() {
        produtoService = new ProdutoService(session);
        fornecedorService = new FornecedorService(session);
        categoriaService = new CategoriaService(session);
        vm = new ProdutoScreenViewModel(null, produtoService, fornecedorService, categoriaService);
    }

    private void criarDependencias() throws Exception {
        var f = new FornecedorModel();
        f.setNome("Fornecedor Teste");
        f.setCpfCnpj("12345678901234");
        var fornecedor = fornecedorService.salvar(f);

        var c = new CategoriaModel();
        c.setNome("Categoria Teste");
        var categoria = categoriaService.salvar(c);

        vm.fornecedores.set(java.util.List.of(fornecedor));
        vm.fornecedorSelected.set(fornecedor);
        vm.categorias.set(java.util.List.of(categoria));
        vm.categoriaSelected.set(categoria);
    }

    @Test
    void deveSalvarProduto() throws Exception {
        criarDependencias();
        vm.codigoBarras.set("7891234567890");
        vm.descricao.set("Produto Teste ViewModel");
        vm.precoVenda.set("99.90");
        vm.unidadeSelected.set("UN");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = produtoService.listar();
        assertEquals(1, list.size());
        assertEquals("Produto Teste ViewModel", list.get(0).getDescricao());
    }

    @Test
    void deveLancarExcecaoQuandoCodigoBarrasVazio() throws Exception {
        criarDependencias();
        vm.codigoBarras.set("");
        vm.descricao.set("Sem código");

        vm.handleAddOrUpdate();
        waitForAsync();

        assertEquals(0, produtoService.listar().size());
    }

    @Test
    void validarDeveRetornarErroQuandoValidadeMenorQueDataAtual() {
        vm.perecivelSelected.set("Sim");
        vm.validade.set(java.time.LocalDate.now().minusDays(1));

        assertNotNull(vm.validar());
    }

    @Test
    void validarDeveRetornarNullQuandoValidadeFutura() {
        vm.perecivelSelected.set("Sim");
        vm.validade.set(java.time.LocalDate.now().plusDays(30));

        assertNull(vm.validar());
    }

    @Test
    void validarDeveRetornarNullQuandoNaoPerecivel() {
        vm.perecivelSelected.set("Não");
        vm.validade.set(null);

        assertNull(vm.validar());
    }

    @Test
    void deveSalvarProdutoComPropriedades() throws Exception {
        criarDependencias();
        vm.codigoBarras.set("7891234567893");
        vm.descricao.set("Produto com Cor, Tamanho, Modelo");
        vm.precoVenda.set("79.90");
        vm.unidadeSelected.set("UN");
        vm.corSelected.set("Azul");
        vm.tamanhoSelected.set("G");
        vm.modelo.set("Esportivo");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = produtoService.listar();
        assertEquals(1, list.size());
        var saved = list.get(0);
        assertEquals("Azul", saved.getCor());
        assertEquals("G", saved.getTamanho());
        assertEquals("Esportivo", saved.getModelo());
    }

    @Test
    void deveSalvarProdutoComValidadeFutura() throws Exception {
        criarDependencias();
        vm.codigoBarras.set("7891234567892");
        vm.descricao.set("Produto Validade Futura");
        vm.precoVenda.set("49.90");
        vm.unidadeSelected.set("UN");
        vm.perecivelSelected.set("Sim");
        vm.validade.set(java.time.LocalDate.now().plusDays(30));

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = produtoService.listar();
        assertEquals(1, list.size());
        assertEquals("Produto Validade Futura", list.get(0).getDescricao());
    }
}

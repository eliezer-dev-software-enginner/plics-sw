package my_app.screens.comprasScreen;

import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.CompraService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.screens.BaseViewModelTest;
import my_app.services.ContasPagarService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ComprasScreenViewModelTest extends BaseViewModelTest {

    private ComprasScreenViewModel vm;
    private CompraService compraService;
    private FornecedorService fornecedorService;
    private ProdutoService produtoService;

    @Override
    protected void initService() {
        compraService = new CompraService(session);
        fornecedorService = new FornecedorService(session);
        produtoService = new ProdutoService(session);
        vm = new ComprasScreenViewModel(null, compraService, fornecedorService, produtoService, null);
    }

    @Test
    void deveSalvarCompra() throws Exception {
        var fornecedor = new FornecedorModel();
        fornecedor.setNome("Fornecedor Teste");
        fornecedorService.salvar(fornecedor);

        var produto = new ProdutoModel();
        produto.setCodigoBarras("789");
        produto.setDescricao("Produto Teste");
        produto.setUnidade("UN");
        produto.setPrecoCompra(new BigDecimal("10.00"));
        produto.setPrecoVenda(new BigDecimal("20.00"));
        produto.setEstoque(BigDecimal.ZERO);
        produto.setFornecedorId(fornecedor.getId());
        produto.setTotalLiquido(new BigDecimal("10.00"));
        produtoService.salvar(produto);

        vm.fornecedorSelected.set(fornecedor);
        vm.codigo.set("789");
        vm.qtd.set("10");
        vm.pcCompra.set("1500"); // R$ 15,00 em centavos
        vm.numeroNota.set("NF-001");
        vm.dataCompra.set(LocalDate.now());
        vm.descontoEmDinheiro.set("0");
        vm.opcaoEstoqueSelected.set("Não");

        vm.handleAddOrUpdate();
        waitForAsync();

        var list = compraService.listar();
        assertEquals(1, list.size());
    }

    @Test
    void deveCarregarDados() throws Exception {
        vm.fetchData();
        waitForAsync();

        assertNotNull(vm.fornecedores.get());
        assertNotNull(vm.compras.get());
    }
}

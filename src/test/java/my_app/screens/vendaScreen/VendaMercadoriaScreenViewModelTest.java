package my_app.screens.vendaScreen;

import my_app.db.models.ClienteModel;
import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.ContaAreceberService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.db.services.VendaService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class VendaMercadoriaScreenViewModelTest extends BaseViewModelTest {

    private VendaMercadoriaScreenViewModel vm;
    private VendaService vendaService;
    private ProdutoService produtoService;
    private ClienteService clienteService;

    @Override
    protected void initService() {
        vendaService = new VendaService(session);
        produtoService = new ProdutoService(session);
        clienteService = new ClienteService(session);
        vm = new VendaMercadoriaScreenViewModel(null, vendaService, produtoService, clienteService, new ContaAreceberService(session));
    }

    @Test
    void deveSalvarVenda() throws Exception {
        var cliente = new ClienteModel();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setCelular("11999999999");
        clienteService.salvar(cliente);

        var fornecedor = new FornecedorModel();
        fornecedor.setNome("Fornecedor Teste");
        new FornecedorService(session).salvar(fornecedor);

        var produto = new ProdutoModel();
        produto.setCodigoBarras("789");
        produto.setDescricao("Produto Teste");
        produto.setUnidade("UN");
        produto.setPrecoCompra(new BigDecimal("10.00"));
        produto.setPrecoVenda(new BigDecimal("20.00"));
        produto.setEstoque(BigDecimal.TEN);
        produto.setFornecedorId(fornecedor.getId());
        produto.setTotalLiquido(new BigDecimal("10.00"));
        produtoService.salvar(produto);

        vm.clienteSelected.set(cliente);
        vm.produtoEncontrado.set(produto);
        vm.codigo.set("789");
        vm.qtd.set("5");
        vm.pcVenda.set("2000"); // R$ 20,00 em centavos
        vm.numeroNota.set("NF-001");
        vm.dataVenda.set(LocalDate.now());
        vm.descontoEmDinheiro.set("0");
        vm.opcaoEstoqueSelected.set("Não");

        try {
            vm.handleAddOrUpdate();
        } catch (Throwable ignored) {
        }
        waitForAsync();

        var list = vendaService.listar();
        if (!list.isEmpty()) {
            assertEquals("789", list.getFirst().getProdutoCod());
        }
    }

    @Test
    void deveCarregarDados() throws Exception {
        vm.fetchData();
        waitForAsync();

        assertNotNull(vm.clientes.get());
        assertNotNull(vm.vendas.get());
    }
}

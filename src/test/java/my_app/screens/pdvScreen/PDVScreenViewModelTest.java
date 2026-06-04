package my_app.screens.pdvScreen;

import my_app.db.models.FornecedorModel;
import my_app.db.models.ProdutoModel;
import my_app.db.services.ClienteService;
import my_app.db.services.FornecedorService;
import my_app.db.services.ProdutoService;
import my_app.screens.BaseViewModelTest;
import my_app.services.PDVService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PDVScreenViewModelTest extends BaseViewModelTest {

    private PDVScreenViewModel vm;
    private ProdutoService produtoService;

    @Override
    protected void initService() {
        produtoService = new ProdutoService(session);
        var clienteService = new ClienteService(session);
        var pdvService = new PDVService();
        vm = new PDVScreenViewModel(null, produtoService, clienteService, pdvService);
    }

    @Test
    void deveAtualizarQuantidade() {
        var produto = new ProdutoModel();
        produto.setCodigoBarras("789");
        produto.setDescricao("Teste");
        produto.setPrecoVenda(new BigDecimal("10.00"));

        vm.itensCarrinho.add(new ItemVenda(produto));
        var item = vm.itensCarrinho.get().getFirst();

        vm.atualizarQuantidade(item, new BigDecimal("3"));

        assertEquals(0, new BigDecimal("3").compareTo(item.quantidade));
    }

    @Test
    void deveRemoverItem() {
        var produto = new ProdutoModel();
        produto.setCodigoBarras("789");
        produto.setDescricao("Teste");
        produto.setPrecoVenda(new BigDecimal("10.00"));

        vm.itensCarrinho.add(new ItemVenda(produto));
        var item = vm.itensCarrinho.get().getFirst();

        vm.removerItem(item);

        assertTrue(vm.itensCarrinho.get().isEmpty());
    }

    @Test
    void adicionarPorCodigoNaoEncontraProduto() {
        try {
            vm.adicionarPorCodigo("000000");
        } catch (Throwable ignored) {
        }

        assertTrue(vm.itensCarrinho.get().isEmpty());
    }
}

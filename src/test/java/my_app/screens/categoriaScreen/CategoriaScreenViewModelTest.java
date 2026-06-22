package my_app.screens.categoriaScreen;

import my_app.db.models.CategoriaModel;
import my_app.db.services.CategoriaService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaScreenViewModelTest extends BaseViewModelTest {

    private CategoriaScreenViewModel vm;
    private CategoriaService categoriaService;

    @Override
    protected void initService() {
        categoriaService = new CategoriaService(session);
        vm = new CategoriaScreenViewModel(null, categoriaService);
    }

    @Test
    void deveSalvarCategoria() throws Exception {
        vm.nome.set("Eletrônicos");
        vm.handleAddOrUpdate();
        waitForAsync();

        var list = categoriaService.listar();
        assertEquals(1, list.size());
        assertEquals("Eletrônicos", list.get(0).getNome());
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() throws Exception {
        vm.nome.set("");
        vm.handleAddOrUpdate();
        waitForAsync();

        assertEquals(0, categoriaService.listar().size());
    }

    @Test
    void deveAtualizarCategoria() throws Exception {
        var salvo = categoriaService.salvar(categoria("Masculino"));

        vm.categoriaSelecionada.set(salvo);
        vm.modoEdicaoState().set(true);
        vm.nome.set("Moda Masculina");
        vm.handleAddOrUpdate();
        waitForAsync();

        var list = categoriaService.listar();
        assertEquals(1, list.size(), "Deveria ter apenas 1 categoria (atualizada, não criada)");
        assertEquals("Moda Masculina", list.get(0).getNome());
    }

    private CategoriaModel categoria(String nome) {
        var c = new CategoriaModel();
        c.setNome(nome);
        return c;
    }
}

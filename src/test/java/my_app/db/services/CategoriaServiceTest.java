package my_app.db.services;

import my_app.db.models.CategoriaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaServiceTest extends BaseServiceTest {

    private CategoriaService categoriaService;

    @Override
    protected void initService() {
        categoriaService = new CategoriaService(session);
    }

    private CategoriaModel categoria(String nome) {
        var c = new CategoriaModel();
        c.setNome(nome);
        return c;
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.salvar(categoria("")));
    }

    @Test
    void deveLancarExcecaoQuandoNomeNull() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.salvar(categoria(null)));
    }

    @Test
    void deveSalvarCategoria() throws Exception {
        var salvo = categoriaService.salvar(categoria("Eletrônicos"));
        assertNotNull(salvo.getId());
        assertEquals("Eletrônicos", salvo.getNome());
    }

    @Test
    void deveLancarExcecaoAoSalvarNomeDuplicado() throws Exception {
        categoriaService.salvar(categoria("Eletrônicos"));
        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.salvar(categoria("eletrônicos")));
    }

    @Test
    void devePermitirAtualizarComMesmoNome() throws Exception {
        var salvo = categoriaService.salvar(categoria("Eletrônicos"));
        assertDoesNotThrow(() -> categoriaService.atualizar(salvo));
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaNomeJaExistente() throws Exception {
        categoriaService.salvar(categoria("Eletrônicos"));
        var outra = categoriaService.salvar(categoria("Informática"));
        outra.setNome("eletrônicos");
        assertThrows(IllegalArgumentException.class, () -> categoriaService.atualizar(outra));
    }

    @Test
    void deveListarCategorias() throws Exception {
        categoriaService.salvar(categoria("A"));
        categoriaService.salvar(categoria("B"));
        assertEquals(2, categoriaService.listar().size());
    }
}

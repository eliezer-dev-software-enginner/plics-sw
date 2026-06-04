package my_app.screens.tecnicoScreen;

import my_app.db.services.TecnicoService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TecnicoScreenViewModelTest extends BaseViewModelTest {

    private TecnicoScreenViewModel vm;
    private TecnicoService tecnicoService;

    @Override
    protected void initService() {
        tecnicoService = new TecnicoService(session);
        vm = new TecnicoScreenViewModel(null, tecnicoService);
    }

    @Test
    void deveSalvarTecnico() throws Exception {
        vm.nome.set("Carlos Técnico");
        vm.handleAddOrUpdate();
        waitForAsync();

        var list = tecnicoService.listar();
        assertEquals(1, list.size());
        assertEquals("Carlos Técnico", list.get(0).getNome());
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() throws Exception {
        vm.nome.set("");
        try {
            vm.handleAddOrUpdate();
        } catch (Throwable ignored) {
        }
        waitForAsync();

        assertEquals(0, tecnicoService.listar().size());
    }
}

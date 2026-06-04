package my_app.screens.empresaScreen;

import my_app.db.services.EmpresaService;
import my_app.screens.BaseViewModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaViewModelTest extends BaseViewModelTest {

    private EmpresaViewModel vm;
    private EmpresaService empresaService;

    @Override
    protected void initService() {
        empresaService = new EmpresaService(session);
        vm = new EmpresaViewModel(null, empresaService);
    }

    @Test
    void deveSalvarEmpresa() throws Exception {
        vm.nome.set("Minha Empresa Ltda");
        vm.celular.set("11999999999");

        vm.handleSave();
        waitForAsync();

        var empresa = empresaService.buscarUnico();
        assertNotNull(empresa);
        assertEquals("Minha Empresa Ltda", empresa.getNome());
    }

    @Test
    void deveLancarExcecaoQuandoNomeVazio() throws Exception {
        vm.nome.set("");

        vm.handleSave();
        waitForAsync();

        assertNull(empresaService.buscarUnico());
    }
}

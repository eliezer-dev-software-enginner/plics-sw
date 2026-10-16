package my_app.core;

import megalodonte.base.route.RouteTable;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.base.components.Component;
import megalodonte.components.SimpleTable;
import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenContractRoutesTest {
    @Test
    void listActionsResolveAllRegisteredFormRoutes() {
        var routes = new RouteTable(new AppRoutes().routes(), AppRoutes.Screens.HOME.name());
        for (var screen : new String[]{
                "ADD_OR_EDIT_PRODUTO", "ADD_OR_EDIT_CLIENTE", "ADD_OR_EDIT_CATEGORIAS",
                "ADD_OR_EDIT_COMPRAS_A_RECEBER", "ADD_OR_EDIT_VENDA_MERCADORIA",
                "ADD_OR_EDIT_FORNECEDOR"}) {
            for (var action : new String[]{"add", "edit", "clone"}) {
                long id = action.equals("add") ? -1 : 6;
                var path = ScreenContract.formRoute(screen, id, action);
                var resolved = routes.resolve(path);
                assertEquals(screen + "/${id}/${type}", resolved.route().identification());
                assertEquals(String.valueOf(id), resolved.params().get("id"));
                assertEquals(action, resolved.params().get("type"));
            }
        }
    }

    @Test
    void homeAndOrderWindowButtonsResolveTheirRoutes() {
        var routes = new RouteTable(new AppRoutes().routes(), AppRoutes.Screens.HOME.name());
        for (var screen : new AppRoutes.Screens[]{
                AppRoutes.Screens.VENDAS, AppRoutes.Screens.PRODUTOS, AppRoutes.Screens.CLIENTES,
                AppRoutes.Screens.COMPRAS_A_RECEBER, AppRoutes.Screens.PDV,
                AppRoutes.Screens.CATEGORIAS, AppRoutes.Screens.FORNECEDORES,
                AppRoutes.Screens.PEDIDOS, AppRoutes.Screens.EMPRESA,
                AppRoutes.Screens.RELATORIOS, AppRoutes.Screens.PREFERENCIAS,
                AppRoutes.Screens.SUGERIR_MELHORIA, AppRoutes.Screens.INFO_UPDATE,
                AppRoutes.Screens.LOGS, AppRoutes.Screens.TERMOS_DE_USO}) {
            assertEquals(screen.name(), routes.resolve(screen.name()).route().identification());
        }
        assertEquals("42", routes.resolve("PEDIDO_DETAILS/42").params().get("id"));
    }

    @Test
    void commonListButtonsDispatchResolvableRoutesAndGuardMissingSelection() {
        var spawned = new AtomicReference<String>();
        var context = (ScreenContextInterface) Proxy.newProxyInstance(
                ScreenContextInterface.class.getClassLoader(),
                new Class<?>[]{ScreenContextInterface.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("spawnWindow")) spawned.set((String) args[0]);
                    return null;
                });
        var routes = new RouteTable(new AppRoutes().routes(), AppRoutes.Screens.HOME.name());
        for (var screen : new String[]{
                "ADD_OR_EDIT_PRODUTO", "ADD_OR_EDIT_CLIENTE", "ADD_OR_EDIT_CATEGORIAS",
                "ADD_OR_EDIT_COMPRAS_A_RECEBER", "ADD_OR_EDIT_VENDA_MERCADORIA",
                "ADD_OR_EDIT_FORNECEDOR"}) {
            var list = new StubList(context);
            list.vm.screenNameSpawn = screen;

            list.handleClickNew();
            assertEquals("add", routes.resolve(spawned.get()).params().get("type"));
            assertThrows(IllegalArgumentException.class, list::handleClickMenuEdit);
            assertThrows(IllegalArgumentException.class, list::handleClickMenuClone);
            assertThrows(IllegalArgumentException.class, list::handleClickMenuDelete);

            var item = new StubEntity();
            item.setId(6L);
            list.vm.selected.set(item);
            list.handleClickMenuEdit();
            assertEquals("edit", routes.resolve(spawned.get()).params().get("type"));
            assertEquals("6", routes.resolve(spawned.get()).params().get("id"));
            list.handleClickMenuClone();
            assertEquals("clone", routes.resolve(spawned.get()).params().get("type"));
            list.handleClickMenuDelete();
            assertTrue(list.vm.deleteInvoked);
        }
    }

    @Test
    void editAndDeleteAvailabilityFollowsSelection() {
        var context = (ScreenContextInterface) Proxy.newProxyInstance(
                ScreenContextInterface.class.getClassLoader(),
                new Class<?>[]{ScreenContextInterface.class},
                (proxy, method, args) -> null);
        var list = new StubList(context);
        var actions = new Actions();
        for (var name : new String[]{"select", "details", "create", "edit", "delete", "clone", "exportCsv", "exportPdf"}) {
            actions.register(name, ignored -> {});
        }
        try (var session = UiDefinition.fromResource(getClass(), "/views/produto-list.view.jsonc").open(actions)) {
            list.updateSelectionActions(session);
            assertTrue(session.states().state("noSelection", Boolean.class).get());
            assertTrue(session.states().state("notSingleSelection", Boolean.class).get());

            var item = new StubEntity();
            item.setId(6L);
            list.vm.selected.set(item);
            list.updateSelectionActions(session);
            assertEquals(false, session.states().state("noSelection", Boolean.class).get());
            assertEquals(false, session.states().state("notSingleSelection", Boolean.class).get());
        }
    }

    private static final class StubEntity extends Identifier {}

    private static final class StubViewModel extends ViewModelScreenContract<StubEntity> {
        boolean deleteInvoked;
        StubViewModel(ScreenContextInterface context) { super(context); }
        @Override protected boolean matchesSearch(StubEntity model, String query) { return false; }
        @Override public StubEntity findById(Long id) { return null; }
        @Override public void populateFieldsFromModel(StubEntity model) {}
        @Override public StubEntity populateModelFromFields() { return null; }
        @Override public void clearForm() {}
        @Override public void handleAddOrUpdate() {}
        @Override public void handleClickMenuDelete() { deleteInvoked = true; }
        @Override public void fetchListData() {}
    }

    private static final class StubList implements ScreenContract<StubEntity> {
        final StubViewModel vm;
        StubList(ScreenContextInterface context) { vm = new StubViewModel(context); }
        @Override public StubViewModel viewModel() { return vm; }
        @Override public SimpleTable<StubEntity> table() { return null; }
        @Override public Component itemDetails(StubEntity model) { return null; }
    }
}

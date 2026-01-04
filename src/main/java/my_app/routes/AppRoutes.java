package my_app.routes;

import javafx.stage.Stage;
import megalodonte.router.Router;
import my_app.screens.DetailScreen.AutenticacaoScreen;
import my_app.screens.DetailScreen.DetailScreen;
import my_app.screens.HomeScreen.HomeScreen;
import my_app.screens.produtoScreen.ProdutoScreen;

import java.util.Set;

public class AppRoutes {
    public Router defineRoutes(Stage stage) throws ReflectiveOperationException {
        var routes = Set.of(
                new Router.Route("auth", router -> new AutenticacaoScreen(router), new Router.RouteProps(900, 550,null)),
                new Router.Route("home", router -> new HomeScreen(router), new Router.RouteProps(1300, 700,null)),
                new Router.Route("cad-produtos/${id}",router-> new ProdutoScreen(router), new Router.RouteProps(1500, 900,"Cadastro de produtos")),
                new Router.Route("detail",router-> new DetailScreen(router), new Router.RouteProps(900, 700, null))
        );
        return new Router(routes, "auth", stage);
    }
}

/**
 * Exemplo ed navegacoes:
 * "cad-produtos/teste}"
 *
 * --- Fechando
 * router.closeSpawn()))
 * .r_child(MenuItem("Sair", Entypo.REPLY, "red", () -> router.closeSpawn("cad-produtos/"+id)))
 */
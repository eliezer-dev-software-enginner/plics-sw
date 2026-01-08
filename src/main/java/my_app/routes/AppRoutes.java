package my_app.routes;

import javafx.stage.Stage;
import megalodonte.router.Router;
import my_app.screens.authScreen.AccessScreen;
import my_app.screens.authScreen.AutenticacaoScreen;
import my_app.screens.HomeScreen.HomeScreen;
import my_app.screens.categoriasScreen.CategoriaScreen;
import my_app.screens.produtoScreen.ProdutoScreen;

import java.util.Set;

public class AppRoutes {
    public Router defineRoutes(Stage stage) throws ReflectiveOperationException {
        var routes = Set.of(
                new Router.Route("auth", router -> new AutenticacaoScreen(router), new Router.RouteProps(900, 550,null, false)),
                new Router.Route("access", router -> new AccessScreen(router), new Router.RouteProps(900, 550,null, false)),
                new Router.Route("home", router -> new HomeScreen(router), new Router.RouteProps(1300, 700,null, false)),
                //new Router.Route("cad-produtos/${id}",router-> new ProdutoScreen(router), new Router.RouteProps(1500, 900,"Cadastro de produtos", false)),
                new Router.Route("cad-produtos",router-> new ProdutoScreen(router), new Router.RouteProps(1500, 900,"Cadastro de produtos", false)),
                new Router.Route("categoria",router-> new CategoriaScreen(router), new Router.RouteProps(1000, 700, "Gerenciamento de categorias", false))
        );
        return new Router(routes, "categoria", stage);
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
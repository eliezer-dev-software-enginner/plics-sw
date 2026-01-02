package my_app.routes;

import javafx.stage.Stage;
import megalodonte.Router;
import my_app.screens.DetailScreen.DetailScreen;
import my_app.screens.HomeScreen.HomeScreen;
import my_app.screens.produtoScreen.ProdutoScreen;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class AppRoutes {
    public Router defineRoutes(Stage stage) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var routes = Set.of(
                new Router.Route("home", router -> new HomeScreen(router), new Router.RouteProps(1300, 700)),
                new Router.Route("cad-produto",router-> new ProdutoScreen(router), new Router.RouteProps(1500, 900)),
                new Router.Route("detail",router-> new DetailScreen(router), new Router.RouteProps(900, 700))
        );
        return new Router(routes, "cad-produto", stage);
    }
}

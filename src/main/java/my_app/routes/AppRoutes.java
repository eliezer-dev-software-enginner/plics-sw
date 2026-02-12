package my_app.routes;

import javafx.stage.Stage;
import megalodonte.router.Router;
import my_app.screens.*;
import my_app.screens.authScreen.AccessScreen;
import my_app.screens.authScreen.AutenticacaoScreen;
import my_app.screens.CategoriaScreen;
import my_app.screens.comprasAPagarScreen.ComprasAPagarScreen;
import my_app.screens.contasAReceberScreen.ContasAReceberScreen;
import my_app.screens.produtoScreen.ProdutoScreen;

import java.util.Set;

public class AppRoutes {
    public Router defineRoutes(Stage stage) throws ReflectiveOperationException {
        var routes = Set.of(
                new Router.Route("auth", router -> new AutenticacaoScreen(router), new Router.RouteProps(900, 550,null, false)),
                new Router.Route("access", router -> new AccessScreen(router), new Router.RouteProps(900, 550,null, false)),
                new Router.Route("home", router -> new HomeScreen(router), new Router.RouteProps(1050, 550,null, true)),
                //new Router.Route("cad-produtos/${id}",router-> new ProdutoScreen(router), new Router.RouteProps(1500, 900,"Cadastro de produtos", false)),
                new Router.Route("produtos",router-> new ProdutoScreen(router), new Router.RouteProps(970, 650,"Cadastro de produtos", true)),
                //ok
                new Router.Route("categorias",router-> new CategoriaScreen(router), new Router.RouteProps(1000, 650, "Gerenciamento de categorias", false)),
                //ok
                new Router.Route("fornecedores",router-> new FornecedorScreen(router), new Router.RouteProps(900, 650, "Gerenciamento de Fornecedores", false)),
                //ok
                new Router.Route("empresa",router-> new CadastroEmpresaScreen(router), new Router.RouteProps(900, 650, "Informações da empresa", false)),
               //ok
                new Router.Route("compras",router-> new ComprasScreen(router), new Router.RouteProps(1000, 650, "Compras de mercadorias", true)),
                //ok
                new Router.Route("clientes",router-> new ClienteScreen(router), new Router.RouteProps(1000, 650, "Gerenciamento de clientes", true)),
                //ok
                new Router.Route("contas-a-pagar",router-> new ComprasAPagarScreen(router), new Router.RouteProps(1000, 650, "Gerenciamento de contas a pagar", true)),
                //ok
                new Router.Route("tecnicos",router-> new TecnicoScreen(router), new Router.RouteProps(1000, 650, "Gerenciamento de Técnicos para ordem de serviço", true)),

                new Router.Route("ordem-de-servico",router-> new OrdemServicoScreen(router), new Router.RouteProps(1000, 650, "Gerenciamento de ordens de serviço", true)),

                new Router.Route("relatar-erro",router-> new RelatarErroScreen(router), new Router.RouteProps(1000, 650, "Relatar erros", true)),

                new Router.Route("sugerir-melhoria",router-> new SugerirMelhoriaScreen(router), new Router.RouteProps(1000, 650, "Detalhes de melhoria ou funcionalidades a serem sugeridas", true)),

                new Router.Route("contas-a-receber",router-> new ContasAReceberScreen(router), new Router.RouteProps(1000, 650, "Gerenciamento de contas a receber", true)),
                //ok
                new Router.Route("vendas",router-> new VendaMercadoriaScreen(router), new Router.RouteProps(1000, 650, "Gerencie sua venda de mercadorias", true))
        );
        return new Router(routes, "sugerir-melhoria", stage);
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
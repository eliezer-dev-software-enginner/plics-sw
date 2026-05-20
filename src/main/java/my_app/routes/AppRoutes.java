package my_app.routes;

import megalodonte.base.route.RouteProps;
import megalodonte.router.v4.Router;
import my_app.Main;
import my_app.screens.*;
import my_app.screens.WelcomeScreen;
import my_app.screens.CategoriaScreen;
import my_app.screens.clienteScreen.ClienteScreen;
import my_app.screens.homeScreen.HomeScreen;
import my_app.screens.pdvScreen.PDVScreen;
import my_app.screens.comprasAPagarScreen.ComprasAPagarScreen;
import my_app.screens.contasAReceberScreen.ContasAReceberScreen;
import my_app.screens.pedidosScreen.PedidosScreen;
import my_app.screens.produtoScreen.ProdutoScreen;
import my_app.screens.vendaScreen.VendaMercadoriaScreen;

import java.util.Set;

public class AppRoutes {
    public Router defineRoutes(boolean askCredentials, boolean forceAccessRoute) throws ReflectiveOperationException {

        var routes = Set.of(
                new Router.Route("welcome", ctx -> new WelcomeScreen(ctx), new RouteProps(900, 550, Main.BASE_TITLE, true)),
                new Router.Route("home", ctx -> new HomeScreen(ctx), new RouteProps(1150, 690,Main.BASE_TITLE, true)),
                //new Router.Route("cad-produtos/${id}",ctx-> new ProdutoScreen(ctx), new Router.RouteProps(1500, 900,"Cadastro de produtos", false)),
                new Router.Route("produtos",ctx-> new ProdutoScreen(ctx), new RouteProps(1210, 650,"Cadastro de produtos", true)),
                //ok
                new Router.Route("categorias",ctx-> new CategoriaScreen(ctx), new RouteProps(1000, 650, "Gerenciamento de categorias", false)),
                //ok
                new Router.Route("fornecedores",ctx-> new FornecedorScreen(ctx), new RouteProps(900, 650, "Gerenciamento de Fornecedores", true)),
                //ok
                new Router.Route("empresa",ctx-> new CadastroEmpresaScreen(ctx), new RouteProps(900, 650, "Informações da empresa", false)),
               //ok
                new Router.Route("compras",ctx-> new ComprasScreen(ctx), new RouteProps(1000, 650, "Compras de mercadorias", true)),
                //ok
                new Router.Route("clientes",ctx-> new ClienteScreen(ctx), new RouteProps(1000, 650, "Gerenciamento de clientes", true)),
                //ok
                new Router.Route("contas-a-pagar",ctx-> new ComprasAPagarScreen(ctx), new RouteProps(1000, 650, "Gerenciamento de contas a pagar", true)),
                //ok
                new Router.Route("tecnicos",ctx-> new TecnicoScreen(ctx), new RouteProps(1000, 650, "Gerenciamento de Técnicos para ordem de serviço", true)),

                new Router.Route("ordem-de-servico",ctx-> new OrdemServicoScreen(ctx), new RouteProps(1000, 650, "Gerenciamento de ordens de serviço", true)),

                new Router.Route("relatar-erro",ctx-> new RelatarErroScreen(ctx), new RouteProps(1000, 650, "Relatar erros", true)),

                new Router.Route("sugerir-melhoria",ctx-> new SugerirMelhoriaScreen(ctx), new RouteProps(1000, 650, "Detalhes de melhoria ou funcionalidades a serem sugeridas", true)),
                new Router.Route("pdv-screen",ctx-> new PDVScreen(ctx), new RouteProps(1025, 650, "Seu caixa está aberto", true)),

                new Router.Route("contas-a-receber",ctx-> new ContasAReceberScreen(ctx), new RouteProps(1000, 650, "Gerenciamento de contas a receber", true)),
                //ok
                new Router.Route("vendas",ctx-> new VendaMercadoriaScreen(ctx), new RouteProps(1200, 650, "Gerencie sua venda de mercadorias", true)),
                new Router.Route("pedidos",ctx-> new PedidosScreen(ctx), new RouteProps(1000, 650, "Analise suas vendas feitas no PDV", true)),
                new Router.Route("preferencias",ctx-> new PreferenciasScreen(ctx),
                        new RouteProps(650, 500, "Alteração de preferências do comportamento do aplicativo", true)),
                new Router.Route("entrar-com-credenciais",ctx-> new AuthScreen(ctx),
                        new RouteProps(550, 400, "Seja muito bem vindo", false))
        );

        String rotaInicial = "produtos";
        //String rotaInicial = "pdv-screen";
        //String rotaInicial = "vendas";
        //String rotaInicial = "pedidos";
       // String rotaInicial = "home";
        //String rotaInicial = "entrar-com-credenciais";

//        String rotaInicial;
//        if (forceAccessRoute) {
//            rotaInicial = "welcome";
//        } else if (askCredentials) {
//            rotaInicial = "entrar-com-credenciais";
//        } else {
//            rotaInicial = "home";
//        }
        return new Router(routes, rotaInicial);
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
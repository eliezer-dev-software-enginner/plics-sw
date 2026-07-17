package my_app.core;

import megalodonte.base.route.RouteProps;
import megalodonte.router.v4.Router;
import my_app.Main;
import my_app.SplashScreen;
import my_app.screens.ler_planilha_ia.LerPlanilhaScreen;
import my_app.screens.welcomeScreen.WelcomeScreen;
import my_app.screens.infoUpdateScreen.InfoUpdateScreen;
import my_app.screens.feedbackScreen.RelatarErroScreen;
import my_app.screens.feedbackScreen.SugerirMelhoriaScreen;
import my_app.screens.authScreen.AuthScreen;
import my_app.screens.ordemServicoScreen.OrdemServicoScreen;
import my_app.screens.preferenciasScreen.PreferenciasScreen;
import my_app.screens.categoriaScreen.CategoriaScreen;
import my_app.screens.clienteScreen.ClienteScreen;
import my_app.screens.comprasScreen.ComprasScreen;
import my_app.screens.empresaScreen.CadastroEmpresaScreen;
import my_app.screens.fornecedorScreen.FornecedorScreen;
import my_app.screens.homeScreen.HomeScreen;
import my_app.screens.pdvScreen.PDVScreen;
import my_app.screens.comprasAPagarScreen.ComprasAPagarScreen;
import my_app.screens.contasAReceberScreen.ContasAReceberScreen;
import my_app.screens.pedidosScreen.PedidosScreen;
import my_app.screens.produtoScreen.ProdutoScreen;
import my_app.screens.tecnicoScreen.TecnicoScreen;
import my_app.screens.vendaScreen.VendaMercadoriaScreen;

import java.sql.SQLException;
import java.util.Set;

public class AppRoutes {
    public enum Screens {
        SPLASH,
        WELCOME,
        AUTH,
        HOME,
        PRODUTOS,
        CATEGORIAS,
        FORNECEDORES,
        EMPRESA,
        COMPRAS,
        CLIENTES,
        COMPRAS_A_PAGAR,
        COMPRAS_A_RECEBER,
        TECNICOS,
        ORDEM_SERVICO,
        RELATAR_ERRO,
        SUGERIR_MELHORIA,
        PDV,
        VENDAS,
        PEDIDOS,
        PREFERENCIAS,
        INFO_UPDATE
    }

    public Set<Router.Route> routes() {
        return Set.of(
                new Router.Route(Screens.SPLASH.name(), ctx -> new SplashScreen(),
                        new RouteProps(600, 500, Main.BASE_TITLE, false)),
                new Router.Route(Screens.WELCOME.name(), WelcomeScreen::new, new RouteProps(950, 550, Main.BASE_TITLE, true)),
                new Router.Route(Screens.AUTH.name(), AuthScreen::new, new RouteProps(950, 550, "Seja muito bem vindo", true)),
                new Router.Route(Screens.HOME.name(), HomeScreen::new, new RouteProps(1180, 670, Main.BASE_TITLE, true)),
                //new Router.Route("cad-produtos/${id}",ctx-> new ProdutoScreen(ctx), new Router.RouteProps(1500, 900,"Cadastro de produtos", false)),
                new Router.Route(Screens.PRODUTOS.name(), ProdutoScreen::new, new RouteProps(1210, 650, "Cadastro de produtos", true)),
                //ok
                new Router.Route(Screens.CATEGORIAS.name(), CategoriaScreen::new, new RouteProps(1000, 650, "Gerenciamento de categorias", false)),
                //ok
                new Router.Route(Screens.FORNECEDORES.name(), FornecedorScreen::new, new RouteProps(1210, 650, "Gerenciamento de Fornecedores", true)),

                new Router.Route(Screens.EMPRESA.name(), CadastroEmpresaScreen::new, new RouteProps(900, 650, "Informações da empresa", false)),
                //ok
                new Router.Route(Screens.COMPRAS.name(), ComprasScreen::new, new RouteProps(1000, 650, "Compras de mercadorias", true)),
                //ok
                new Router.Route(Screens.CLIENTES.name(), ClienteScreen::new, new RouteProps(1150, 650, "Gerenciamento de clientes", true)),
                //ok
                new Router.Route(Screens.COMPRAS_A_PAGAR.name(), ComprasAPagarScreen::new, new RouteProps(1000, 650, "Gerenciamento de contas a pagar", true)),
                //ok
                new Router.Route(Screens.TECNICOS.name(), TecnicoScreen::new, new RouteProps(1000, 650, "Gerenciamento de Técnicos para ordem de serviço", true)),
                new Router.Route(Screens.ORDEM_SERVICO.name(), OrdemServicoScreen::new, new RouteProps(1000, 650, "Gerenciamento de ordens de serviço", true)),
                new Router.Route(Screens.RELATAR_ERRO.name(), RelatarErroScreen::new, new RouteProps(1000, 650, "Relatar erros", true)),
                new Router.Route(Screens.SUGERIR_MELHORIA.name(), SugerirMelhoriaScreen::new, new RouteProps(1000, 650, "Detalhes de melhoria ou funcionalidades a serem sugeridas", true)),
                new Router.Route(Screens.PDV.name(), PDVScreen::new, new RouteProps(1025, 650, "Seu caixa está aberto", true)),
                new Router.Route(Screens.COMPRAS_A_RECEBER.name(), ContasAReceberScreen::new, new RouteProps(1000, 650, "Gerenciamento de contas a receber", true)),
                new Router.Route(Screens.VENDAS.name(), VendaMercadoriaScreen::new, new RouteProps(1200, 650, "Gerencie sua venda de mercadorias", true)),
                new Router.Route(Screens.PEDIDOS.name(), PedidosScreen::new, new RouteProps(1000, 650, "Analise suas vendas feitas no PDV", true)),
                new Router.Route(Screens.PREFERENCIAS.name(), PreferenciasScreen::new, new RouteProps(650, 500, "Alteração de preferências do comportamento do aplicativo", true)),
                new Router.Route(Screens.INFO_UPDATE.name(), InfoUpdateScreen::new, new RouteProps(900, 650, "Atualizações do aplicativo", false))
        );
    }
}

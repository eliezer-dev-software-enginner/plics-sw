package my_app.core;

import megalodonte.base.route.RouteProps;
import megalodonte.router.v4.Router;
import my_app.Main;
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
    public Set<Router.Route> routes() {
        return Set.of(
                new Router.Route("ler-planilha-ia", LerPlanilhaScreen::new, new RouteProps(650, 500, "Extrair dados com IA", true)),
                new Router.Route("welcome", WelcomeScreen::new, new RouteProps(950, 550, Main.BASE_TITLE, true)),
                new Router.Route("home", HomeScreen::new, new RouteProps(1180, 650,Main.BASE_TITLE, true)),
                //new Router.Route("cad-produtos/${id}",ctx-> new ProdutoScreen(ctx), new Router.RouteProps(1500, 900,"Cadastro de produtos", false)),
                new Router.Route("produtos", ProdutoScreen::new, new RouteProps(1210, 650,"Cadastro de produtos", true)),
                //ok
                new Router.Route("categorias", CategoriaScreen::new, new RouteProps(1000, 650, "Gerenciamento de categorias", false)),
                //ok
                new Router.Route("fornecedores", FornecedorScreen::new, new RouteProps(1210, 650, "Gerenciamento de Fornecedores", true)),

                new Router.Route("empresa", CadastroEmpresaScreen::new, new RouteProps(900, 650, "Informações da empresa", false)),
                //ok
                new Router.Route("compras", ComprasScreen::new, new RouteProps(1000, 650, "Compras de mercadorias", true)),
                //ok
                new Router.Route("clientes", ClienteScreen::new, new RouteProps(1150, 650, "Gerenciamento de clientes", true)),
                //ok
                new Router.Route("contas-a-pagar", ComprasAPagarScreen::new, new RouteProps(1000, 650, "Gerenciamento de contas a pagar", true)),
                //ok
                new Router.Route("tecnicos", TecnicoScreen::new, new RouteProps(1000, 650, "Gerenciamento de Técnicos para ordem de serviço", true)),

                new Router.Route("ordem-de-servico", OrdemServicoScreen::new, new RouteProps(1000, 650, "Gerenciamento de ordens de serviço", true)),

                new Router.Route("relatar-erro", RelatarErroScreen::new, new RouteProps(1000, 650, "Relatar erros", true)),

                new Router.Route("sugerir-melhoria", SugerirMelhoriaScreen::new, new RouteProps(1000, 650, "Detalhes de melhoria ou funcionalidades a serem sugeridas", true)),
                new Router.Route("pdv-screen", PDVScreen::new, new RouteProps(1025, 650, "Seu caixa está aberto", true)),

                new Router.Route("contas-a-receber", ContasAReceberScreen::new, new RouteProps(1000, 650, "Gerenciamento de contas a receber", true)),
                //ok
                new Router.Route("vendas", VendaMercadoriaScreen::new, new RouteProps(1200, 650, "Gerencie sua venda de mercadorias", true)),
                new Router.Route("pedidos", PedidosScreen::new, new RouteProps(1000, 650, "Analise suas vendas feitas no PDV", true)),
                new Router.Route("preferencias", PreferenciasScreen::new,
                        new RouteProps(650, 500, "Alteração de preferências do comportamento do aplicativo", true)),
                new Router.Route("entrar-com-credenciais", AuthScreen::new,
                        new RouteProps(550, 490, "Seja muito bem vindo", false)),
                new Router.Route("info-update", InfoUpdateScreen::new,
                        new RouteProps(900, 650, "Atualizações do aplicativo", false))
        );
    }
}

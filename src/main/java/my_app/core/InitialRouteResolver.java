package my_app.core;

public class InitialRouteResolver {

    public static String resolve(boolean isFirstAccess, boolean enterWithCredentials) {
        if (isFirstAccess) {
            return "welcome";
        }
        if (enterWithCredentials) {
            return "entrar-com-credenciais";
        }
        return "home";
    }
}
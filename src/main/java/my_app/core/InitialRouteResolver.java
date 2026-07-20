package my_app.core;

import my_app.Main;

public class InitialRouteResolver {

    public static String resolve(boolean isFirstAccess, boolean enterWithCredentials) {
        if(Main.devMode)return AppRoutes.Screens.RELATAR_ERRO.name();
        if (isFirstAccess) {
            return AppRoutes.Screens.WELCOME.name();
        }
        if (enterWithCredentials) {
            return AppRoutes.Screens.AUTH.name();
        }
        return AppRoutes.Screens.HOME.name();
    }
}
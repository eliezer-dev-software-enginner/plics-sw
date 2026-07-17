package my_app.core;

public class InitialRouteResolver {

    public static String resolve(boolean isFirstAccess, boolean enterWithCredentials) {
        if (isFirstAccess) {
            return AppRoutes.Screens.WELCOME.name();
        }
        if (enterWithCredentials) {
            return AppRoutes.Screens.AUTH.name();
        }
        return AppRoutes.Screens.HOME.name();
    }
}
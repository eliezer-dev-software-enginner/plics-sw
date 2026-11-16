package plics_sw_new;


import megalodonte.application.MegalodonteApp;
import megalodonte.application.MegalodonteApplication;
import megalodonte.base.theme.ThemeManager;
import megalodonte.theme.DefaultTheme;


public final class Main {

public static final class AppHost extends MegalodonteApplication {}



static void main(String[] args) {

    ThemeManager.setTheme(new DefaultTheme());

    MegalodonteApp.appName("plics-sw-new");

    MegalodonteApp.appIcon("/icon.png");

    MegalodonteApp.run(AppHost.class, args, context -> context.useView(new HomeScreen()));

}
}

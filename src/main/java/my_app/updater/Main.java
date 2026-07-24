package my_app.updater;

import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.MegalodonteApp;
import megalodonte.application.MegalodonteApplication;
import megalodonte.base.theme.ThemeManager;
import my_app.core.Themes;

public class Main {

    // Classe própria de launch — mesmo motivo do Main.AppHost principal: no Linux o
    // WM_CLASS é o nome desta classe, então o updater (processo separado) não fica
    // com o mesmo ícone/agrupamento de dock do app principal. Ver MegalodonteApplication.
    public static class UpdaterAppHost extends MegalodonteApplication {}

    static void main(String[] args) {
        MegalodonteApp.run(UpdaterAppHost.class, args, Main::initialize, ev -> {
            if (ev == MegalodonteApp.Event.CloseRequest) {
                ListenerManager.disposeAll();
            }
        });
    }

    public static void initialize(Context context) {
        ThemeManager.setTheme(Themes.LIGHT);
        context.javafxStage().setTitle("Atualizando Plics SW");
        context.useView(new HomeScreen(context));
    }
}

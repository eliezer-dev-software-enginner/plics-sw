package my_app.updater;

import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.MegalodonteApp;
import megalodonte.base.theme.ThemeManager;
import my_app.core.Themes;

public class Main {

    static void main(String[] args) {
        MegalodonteApp.run(args, Main::initialize, ev -> {
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

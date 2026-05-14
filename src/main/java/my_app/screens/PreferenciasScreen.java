package my_app.screens;

import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import megalodonte.v2.Show;
import my_app.db.models.*;
import my_app.db.repositories.*;
import my_app.screens.components.Components;
//import javafx.scene.control.*;
import javafx.scene.control.*;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.props.*;

import java.sql.SQLException;
import java.util.List;

public class PreferenciasScreen implements ScreenComponent {
    private final ScreenContext ctx;
    private final PreferenciasRepository preferenciasRepository;

//    State<String> temaSelected = State.of("Claro");
    State<String> habilitarCredenciaisSelected = State.of("Não");
    State<String> loginState = State.of("");
    State<String> passwordState = State.of("");

    PreferenciasModel prefLoaded;

    public PreferenciasScreen(ScreenContext ctx) {
        this.ctx = ctx;

//        temaSelected.subscribe(theme->{
//            ThemeManager.setTheme(theme.equals("Claro")? Themes.LIGHT: Themes.DARK);
//        });

        preferenciasRepository = new PreferenciasRepository();
    }

    @Override
    public void onMount() {
        Async.Run(()->{
            try{
                var prefs = preferenciasRepository.listar();
                if(!prefs.isEmpty()){
                    var pref = prefs.getFirst();
                    UI.runOnUi(()->{
                        prefLoaded = pref;
                        //temaSelected.set(pref.tema);
                        habilitarCredenciaisSelected.set(pref.credenciaisHabilitadas==1?"Sim":"Não");
                        loginState.set(pref.login);
                        passwordState.set(pref.senha);
                    });
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Component render() {
        var crentialsScreenIsVisible = ComputedState.of(()-> habilitarCredenciaisSelected.get().equals("Sim"), habilitarCredenciaisSelected);

        return new Column(new ColumnProps().paddingAll(20)).children(
                new Text("Minhas preferências"),
                //Components.SelectColumn("Alterar Tema", List.of("Claro", "Escuro"), temaSelected, it->it),
                Components.SelectColumn("Habilitar credenciais", List.of("Sim", "Não"), habilitarCredenciaisSelected, it->it),
                Show.when(crentialsScreenIsVisible, ()-> new Column().children(
                        new Text("Escolha seu login e senha de acesso"),
                        Components.InputColumn("Login", loginState, ""),
                        Components.InputColumn("Senha", passwordState, "")
                )),
                new SpacerVertical(10),
                Components.ButtonCadastro("Salvar Preferências", this::salvarPrefs)
//                Show.when(habilitarCredenciaisSelected)
        );
    }

    void salvarPrefs(){
        Async.Run(()->{
            try{
                prefLoaded.credenciaisHabilitadas = habilitarCredenciaisSelected.get().equals("Sim")? 1: 0;
                prefLoaded.login = loginState.get();
                prefLoaded.senha = passwordState.get();
                //model.tema = temaSelected.get();

                preferenciasRepository.atualizar(prefLoaded);
                UI.runOnUi(()-> Components.ShowPopup(ctx, "Preferências foram salvas com sucesso!"));
            } catch (Exception e) {
               UI.runOnUi(()-> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

}
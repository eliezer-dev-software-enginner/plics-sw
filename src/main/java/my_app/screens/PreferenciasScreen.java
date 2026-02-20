package my_app.screens;

import megalodonte.ComputedState;
import megalodonte.Show;
import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.router.Router;
import my_app.db.models.PreferenciasModel;
import my_app.db.repositories.PreferenciasRepository;
import my_app.screens.components.Components;

import java.sql.SQLException;
import java.util.List;

public class PreferenciasScreen implements ScreenComponent {
    private final Router router;
    private final PreferenciasRepository preferenciasRepository;

//    State<String> temaSelected = State.of("Claro");
    State<String> habilitarCredenciaisSelected = State.of("Não");
    State<String> loginState = State.of("");
    State<String> passwordState = State.of("");

    public PreferenciasScreen(Router router) {
        this.router = router;

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

        return new Column(new ColumnProps().paddingAll(20)).c_childs(
                new Text("Minhas preferências"),
                //Components.SelectColumn("Alterar Tema", List.of("Claro", "Escuro"), temaSelected, it->it),
                Components.SelectColumn("Habilitar credenciais", List.of("Sim", "Não"), habilitarCredenciaisSelected, it->it),
                Show.when(crentialsScreenIsVisible, ()-> new Column().c_childs(
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
                var model = new PreferenciasModel();
                model.credenciaisHabilitadas = habilitarCredenciaisSelected.get().equals("Sim")? 1: 0;
                model.login = loginState.get();
                model.senha = passwordState.get();
                //model.tema = temaSelected.get();

                preferenciasRepository.atualizar(model);
                UI.runOnUi(()-> Components.ShowPopup(router, "Preferências foram salvas com sucesso!"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
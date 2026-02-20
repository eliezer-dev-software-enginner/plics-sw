package my_app.screens;

import megalodonte.State;
import megalodonte.base.async.Async;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.router.Router;
import my_app.db.models.PreferenciasModel;
import my_app.db.repositories.PreferenciasRepository;
import my_app.screens.components.Components;

import java.sql.SQLException;

public class EnterWithLoginPasswordScreen implements ScreenComponent {
    private final Router router;
    private final PreferenciasRepository preferenciasRepository;

    State<String> loginState = State.of("");
    State<String> passwordState = State.of("");

    PreferenciasModel prefRecuperada;

    public EnterWithLoginPasswordScreen(Router router) {
        this.router = router;
        preferenciasRepository = new PreferenciasRepository();
    }

    @Override
    public void onMount() {
        Async.Run(()->{
            try{
                var prefs = preferenciasRepository.listar();
                if(!prefs.isEmpty()){
                    prefRecuperada = prefs.getFirst();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Component render() {
        return new Column(new ColumnProps().paddingAll(20)).c_childs(
                new Text("Realiza já seu login na Plics SW"),
                        Components.InputColumn("Login", loginState, ""),
                        Components.InputColumn("Senha", passwordState, ""),
                new SpacerVertical(10),
                Components.ButtonCadastro("Entrar", this::entrar)
        );
    }

    void entrar(){
        if(prefRecuperada.login.trim().equals(loginState.get()) && prefRecuperada.senha.trim().equals(passwordState.get())){
            Components.ShowPopup(router, "Login efetuado com sucesso!");
            router.navigateTo("home");
        }
    }

}
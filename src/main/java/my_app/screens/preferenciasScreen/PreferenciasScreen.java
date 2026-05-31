package my_app.screens.preferenciasScreen;

import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.v2.Show;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ColumnProps;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.Data;
import my_app.domain.components.Components;

public class PreferenciasScreen implements ScreenComponent {

    private final PreferenciasViewModel vm;

    public PreferenciasScreen(ScreenContext ctx) {
        this.vm = new PreferenciasViewModel(ctx);
    }

    @Override
    public void onMount() {
        vm.load();
    }

    public Component render() {
        var credentialsScreenIsVisible = ComputedState.of(
                () -> vm.habilitarCredenciaisSelected.get().equals("Sim"),
                vm.habilitarCredenciaisSelected
        );

        return new Column(new ColumnProps().paddingAll(20)).children(
                new Text("Minhas preferências"),
                Components.SelectColumn("Habilitar credenciais", Data.simNaoList, vm.habilitarCredenciaisSelected, it -> it),
                Show.when(credentialsScreenIsVisible, () -> new Column().children(
                        new Text("Escolha seu login e senha de acesso"),
                        Components.InputColumn("Login", vm.loginState, ""),
                        Components.InputColumn("Senha", vm.passwordState, "")
                )),
                new SpacerVertical(10),
                Components.ButtonCadastro("Salvar Preferências", vm::salvar)
        );
    }
}

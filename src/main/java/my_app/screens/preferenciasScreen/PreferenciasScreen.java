package my_app.screens.preferenciasScreen;

import megalodonte.ComputedState;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.layout_components.Row;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;
import megalodonte.v2.Show;
import megalodonte.components.Button;
import megalodonte.components.LineHorizontal;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.props.ButtonProps;
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

        return new Column(new ColumnProps().paddingAll(20).spacingOf(ThemeManager.theme().spacing().sm())).children(
                new Text("Minhas preferências", new TextProps().bold().fontSize(ThemeManager.theme().typography().subtitle())),
                new Row(new RowProps().spacingOf(20)).children(
                        Components.SelectColumn("Habilitar credenciais", Data.simNaoList, vm.habilitarCredenciaisSelected, it -> it),
                        Components.SelectColumn("Exibir popup Instagram na abertura", Data.simNaoList, vm.habilitarPopupInstagramSelected, it -> it),
                        Components.SelectColumn("Selecionar impressora", vm.comportsState, vm.comportsStateSelected, it -> it,false)
                        ),
                     Show.when(credentialsScreenIsVisible, () -> new Column().children(
                        new Text("Escolha seu login e senha de acesso", new TextProps().bold()),
                        new SpacerVertical(ThemeManager.theme().spacing().sm()),
                        Components.InputColumn("Login", vm.loginState, "Ex: admin"),
                        Components.InputColumn("Senha", vm.passwordState, "Digite uma senha")
                )),
                Components.ButtonCadastro("Salvar Preferências", vm::salvar),
                new LineHorizontal(),
                new Button("Encerrar sessão",
                        new ButtonProps().fillWidth().height(31)
                                .fontSize(14).textColor("white").bgColor("#dc2626"))
                        .onClick(() -> Components.ShowAlertAdvice(
                                "Tem certeza que deseja sair? Não se preocupe seus dados serão mantidos ;)",
                                vm::signOut
                        ))
        );
    }
}

package my_app.screens.empresaScreen;

import disgust.io.ButtonsPack;
import disgust.io.br.Pack;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.base.theme.ThemeManager;
import megalodonte.components.Card;
import megalodonte.components.Scroll;
import megalodonte.components.SpacerHorizontal;
import megalodonte.components.SpacerVertical;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ButtonVariant;
import megalodonte.props.ContainerProps;
import megalodonte.props.ImageProps;
import megalodonte.props.RowProps;
import megalodonte.router.v5.ScreenContext;
import my_app.core.components.Components;

public class CadastroEmpresaScreen implements ScreenComponent {
    private final EmpresaViewModel vm;

    public CadastroEmpresaScreen(ScreenContext ctx) {
       vm =  new EmpresaViewModel(ctx);
    }

    public void onMount(){
        vm.fetchData();
    }

    @Override
    public void onDestroy() {
        try {
            vm.onDestroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Component render() {
        return new Scroll(
                new Container(new ContainerProps().paddingAll(5))
                        .c_child(new SpacerVertical(10))
                        .c_child(form())
        );
    }

    Component form(){
        return new Card(new Column()
                .c_child(Components.FormSubtitle("Informações da empresa"))
                .c_child(new SpacerVertical(ThemeManager.theme().spacing().md()))
                .c_child(TopWithImage())
                .c_child(new SpacerVertical(ThemeManager.theme().spacing().sm()))
                .c_child(Components.FormSubtitle("Endereço"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(ThemeManager.theme().spacing().sm()))
                        .children(
                                Pack.InputColumnCep("Cep", vm.cep),
                                disgust.io.Pack.InputColumn("Cidade", vm.cidade,"Ex: Paraiso"),
                                disgust.io.Pack.InputColumn("Bairro", vm.bairro,"Ex: Bairro abc"),
                                disgust.io.Pack.InputColumn("Rua", vm.rua,"Ex: rua das graças")
                        )
                )
                .c_child(new SpacerVertical(ThemeManager.theme().spacing().sm()))
                .c_child(Components.FormSubtitle("Dados de carnê"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(ThemeManager.theme().spacing().sm()))
                        .r_child(
                                disgust.io.Pack.InputColumn("Local de pagamento",  vm.localPagamento,"Ex: Pagável em qualquer banco ou lotérica"))
                        .r_child(
                                Components.TextAreaColumnWidthNoRestricted("Texto de responsabilidade do cedente",  vm.textoResponsabilidade,
                                        "Ex: Após o vencimento cobrar multa...",200))
                )
                .c_child(new SpacerVertical(20))
                .c_child(ButtonsPack.ContainedButton("Salvar", ButtonVariant.PRIMARY,true,vm::handleSave)));
    }

    Row TopWithImage() {
        var left = new Row(new RowProps().bottomVertically().spacingOf(ThemeManager.theme().spacing().sm()))
                .children(
                        disgust.io.Pack.InputColumn("Nome *", vm.nome, "Ex: Empresa ABC"),
                        Pack.InputColumnPhone("Telefone/Celular",  vm.celular));

        return new Row()
                .r_child(left)
                .r_child(new SpacerHorizontal(ThemeManager.theme().spacing().sm()))
                .r_child(Components.ImageSelector("Mudar logomarca",  vm.logoMarca,
                        new ImageProps().size(100),  vm::handleUpdateLogoMarca));
    }
}
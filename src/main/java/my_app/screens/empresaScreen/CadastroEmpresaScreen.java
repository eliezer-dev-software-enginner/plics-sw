package my_app.screens.empresaScreen;

import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.router.v4.ScreenContext;
import my_app.domain.components.Components;
import megalodonte.components.*;
import megalodonte.components.layout_components.Row;
import megalodonte.props.*;

import java.sql.SQLException;

public class CadastroEmpresaScreen implements ScreenComponent {
    private final EmpresaViewModel vm;

    public CadastroEmpresaScreen(ScreenContext ctx) throws SQLException {
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
        return new Container(new ContainerProps().paddingAll(5))
                .c_child(new SpacerVertical(10))
                .c_child(form());
    }

    Component form(){
        return new Card(new Column()
                .c_child(Components.FormTitle("Informações da empresa"))
                .c_child(new SpacerVertical(20))
                .c_child(TopWithImage())
                .c_child(new SpacerVertical(10))
                .c_child(Components.FormTitle("Endereço"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Cep", vm.cep,"xxxxxxxx"))
                        .r_child(
                                Components.InputColumn("Cidade", vm.cidade,"Ex: Paraiso"))
                        .r_child(
                                Components.InputColumn("Bairro", vm.bairro,"Ex: Bairro abc"))
                        .r_child(
                                Components.InputColumn("Rua", vm.rua,"Ex: rua das graças"))
                )
                .c_child(new SpacerVertical(10))
                .c_child(Components.FormTitle("Dados de carnê"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Local de pagamento",  vm.localPagamento,"Ex: Pagável em qualquer banco ou lotérica"))
                        .r_child(
                                Components.TextAreaColumn("Texto de responsabilidade do cedente",  vm.textoResponsabilidade,"Ex: Após o vencimento cobrar multa..."))
                )
                .c_child(new SpacerVertical(20))
                .c_child(Components.ButtonCadastro("Salvar",vm::handleSave)))
            //TODO: adicionar imagem
                ;
    }

    Row TopWithImage() {
        var left = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(
                        Components.InputColumn("Nome", vm.nome, "Ex: Empresa ABC"))
                .r_child(
                        Components.InputColumn("Telefone/Celular",  vm.celular, "(xx)xxxxx-yyyy"));

        return new Row()
                .r_child(left)
                .r_child(new SpacerHorizontal(10))
                .r_child(Components.ImageSelector("Mudar logomarca",  vm.logoMarca,
                        new ImageProps().size(100),  vm::handleUpdateLogoMarca));
    }
}
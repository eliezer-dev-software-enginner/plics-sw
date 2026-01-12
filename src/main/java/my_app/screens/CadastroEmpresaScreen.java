package my_app.screens;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import megalodonte.State;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.dto.CategoriaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.repositories.CategoriaRepository;
import my_app.screens.components.Components;

import java.sql.SQLException;

public class CadastroEmpresaScreen {
    private final Router router;

    State<String> nome = State.of("");
    State<String> celular = State.of("");
    State<String> cep = State.of("");
    State<String> cidade = State.of("");
    State<String> bairro = State.of("");
    State<String> rua = State.of("");

    State<String> localPagamento = State.of("");
    State<String> textoResponsabilidade = State.of("");

//    private CategoriaRepository categoriaRepository = new CategoriaRepository();
    public CadastroEmpresaScreen(Router router) {
        this.router = router;
        fetchData();
    }

    private void fetchData() {
//        try {
//            categoriasObservable.clear();
//            categoriasObservable.addAll(categoriaRepository.listar());
//        } catch (Exception e) {
//            throw new RuntimeException("Erro ao carregar categorias", e);
//        }
    }

    private final Theme theme = ThemeManager.theme();


    public Component render() {
        return new Column(new ColumnProps().paddingAll(5), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(new SpacerVertical(10))
                .c_child(form());
    }

    Component form(){
        return new Card(new Column()
                .c_child(Components.FormTitle("Informações da empresa"))
                .c_child(new SpacerVertical(20))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Nome", nome,"Ex: Eletrômicos"))
                        .r_child(
                                Components.InputColumn("Telefone/Celular", celular,"(xx)xxxxx-yyyy"))
                )
                .c_child(Components.FormTitle("Endereço"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Cep", cep,"Ex: Eletrômicos"))
                        .r_child(
                                Components.InputColumn("Cidade", cidade,"(xx)xxxxx-yyyy"))
                        .r_child(
                                Components.InputColumn("Bairro", bairro,"Ex: Eletrômicos"))
                        .r_child(
                                Components.InputColumn("Rua", rua,"Ex: Eletrômicos"))
                )
                .c_child(Components.FormTitle("Dados de carnê"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Local de pagamento", localPagamento,"Ex: Pagável em qualquer banco ou lotérica"))
                        .r_child(
                                Components.InputColumn("Texto de responsabilidade do cedente", textoResponsabilidade,"Ex: Após o vencimento cobrar multa..."))
                )
                .c_child(new SpacerVertical(20))
                .c_child(Components.ButtonCadastro("Salvar",this::handleSave)))
            //TODO: adicionar imagem
                ;
    }

    private void handleSave(){
        String value = nome.get();
        IO.println("Nome: " + value);

//        var dto = new CategoriaDto(value.trim(), System.currentTimeMillis());
//
//        try{
//            var model = categoriaRepository.salvar(dto);
//            categoriasObservable.add(model);
//            IO.println("Categoria '" + model.nome + "' cadastrada com ID: " + model.id);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    }
}
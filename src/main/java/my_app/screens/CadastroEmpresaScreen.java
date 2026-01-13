package my_app.screens;

import javafx.stage.FileChooser;
import megalodonte.State;
import megalodonte.async.Async;
import megalodonte.base.UI;
import megalodonte.components.*;
import megalodonte.props.ColumnProps;
import megalodonte.props.ImageProps;
import megalodonte.props.RowProps;
import megalodonte.router.Router;
import megalodonte.styles.ColumnStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.models.EmpresaModel;
import my_app.db.repositories.EmpresaRepository;
import my_app.screens.components.Components;

import java.io.File;
import java.nio.file.Path;

public class CadastroEmpresaScreen {
    private final Router router;

    State<String> nome = State.of("");
    State<String> celular = State.of("");
    State<String> logoMarca = State.of("/logo_256x256.png");

    State<String> cep = State.of("");
    State<String> cidade = State.of("");
    State<String> bairro = State.of("");
    State<String> rua = State.of("");

    State<String> localPagamento = State.of("");
    State<String> textoResponsabilidade = State.of("");

    private EmpresaRepository empresaRepository = new EmpresaRepository();
    public CadastroEmpresaScreen(Router router) {
        this.router = router;
        onMount();
    }

    void onMount(){
        fetchData();
    }

    private void fetchData() {
        Async.Run(()->{
            try {
                var list = empresaRepository.listar();
                if(!list.isEmpty()){
                    var model = list.getFirst();

                    UI.runOnUi(()->{
                        nome.set(model.nome);
                        celular.set(model.telefone);
                        logoMarca.set(model.logoMarca != null ? model.logoMarca : "/logo_256x256.png");
                        cep.set(model.cep);
                        cidade.set(model.cidade);
                        bairro.set(model.bairro);
                        rua.set(model.rua);
                        localPagamento.set(model.localPagamento);
                        textoResponsabilidade.set(model.textoResponsabilidade);
                    });
                }

            } catch (Exception e) {
                throw new RuntimeException("Erro ao carregar categorias", e);
            }
        });

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
                .c_child(TopWithImage())
                .c_child(new SpacerVertical(10))
                .c_child(Components.FormTitle("Endereço"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Cep", cep,"xxxxxxxx"))
                        .r_child(
                                Components.InputColumn("Cidade", cidade,"Ex: Paraiso"))
                        .r_child(
                                Components.InputColumn("Bairro", bairro,"Ex: Bairro abc"))
                        .r_child(
                                Components.InputColumn("Rua", rua,"Ex: rua das graças"))
                )
                .c_child(new SpacerVertical(10))
                .c_child(Components.FormTitle("Dados de carnê"))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(
                                Components.InputColumn("Local de pagamento", localPagamento,"Ex: Pagável em qualquer banco ou lotérica"))
                        .r_child(
                                Components.TextAreaColumn("Texto de responsabilidade do cedente", textoResponsabilidade,"Ex: Após o vencimento cobrar multa..."))
                )
                .c_child(new SpacerVertical(20))
                .c_child(Components.ButtonCadastro("Salvar",this::handleSave)))
            //TODO: adicionar imagem
                ;
    }

    Row TopWithImage() {
        var left = new Row(new RowProps().bottomVertically().spacingOf(10))
                .r_child(
                        Components.InputColumn("Nome", nome, "Ex: Eletrômicos"))
                .r_child(
                        Components.InputColumn("Telefone/Celular", celular, "(xx)xxxxx-yyyy"));

        return new Row()
                .r_child(left)
                .r_child(new SpacerHorizontal(10))
                .r_child(Components.ImageSelector("Mudar logomarca", logoMarca,
                        new ImageProps().size(100), this::handleUpdateLogoMarca));
    }

    private void handleUpdateLogoMarca(){
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar imagem");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files", "*.png","*.jpg","*.jpeg"));

        File arquivo = fileChooser.showOpenDialog(this.router.getCurrentActiveStage());
        if(arquivo != null){
            IO.println("abs: " + arquivo.getAbsolutePath());
            String imagePath = arquivo.toURI().toString();
            IO.println("uri: " + imagePath);

            logoMarca.set(imagePath);
        }
    }

    private void handleSave(){
        var model = new EmpresaModel();
        model.id = 1L;
        model.nome = nome.get();
        model.logoMarca = logoMarca.get();
        model.cep = cep.get();
        model.bairro = bairro.get();
        model.rua = rua.get();
        model.cidade = cidade.get();
        model.localPagamento = localPagamento.get();
        model.termoServico = textoResponsabilidade.get();
        model.telefone = celular.get();
        model.textoResponsabilidade = textoResponsabilidade.get();

        Async.Run(()->{
            try{
                empresaRepository.atualizar(model);
                UI.runOnUi(()-> {
                    IO.println("Empresa atualizada com sucesso!");
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
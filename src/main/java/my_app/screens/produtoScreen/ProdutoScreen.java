package my_app.screens.produtoScreen;

import javafx.scene.paint.Color;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.Component;
import megalodonte.components.Image;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.CardProps;
import megalodonte.props.TextProps;
import megalodonte.router.RouteParamsAware;
import megalodonte.router.Router;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static my_app.screens.produtoScreen.ProdutoComponents.*;

public class ProdutoScreen implements RouteParamsAware {
    private Router router;
    private ProdutoScreenViewModel vm;

    public ProdutoScreen(Router router) {
        this.router = router;
        this.vm = new ProdutoScreenViewModel();
    }

    private String id;
    @Override
    public void onRouteParams(Map<String, String> params) {
        this.id = params.get("id");
    }

    public Component render (){
        IO.println("[id]: " + id);
        return new Column(new ColumnProps().paddingAll(15), new ColumnStyler().bgColor("#fff"))
                .c_child(Menu())
                .c_child(new SpacerVertical(30))
                .c_child(
                        new Row(new RowProps().paddingAll(20)
                                .spacingOf(20), new RowStyler().borderWidth(1).borderColor("black").borderRadius(1))
                                .r_child(ContainerLeft(vm))
                                .r_child(new SpacerHorizontal().fill())
                                .r_child(ContainerRight())
                )
                .c_child(new SpacerVertical(30))
                .c_child(new Text("Informaçoẽs", new TextProps().fontSize(30)))
                .c_child(new Text("Dica 1: Pressione CTRL + G para gerar o código de barras",new TextProps().fontSize(24)))
                .c_child(new Text("Dica 2: O sistema não permite gravar produtos diferentes com o mesmo código de barras!",new TextProps().fontSize(24)));
    }

    //TODO: opções de Editar e Excluir só aparecem ao buscar produto
    Component Menu(){
        return new Row(new RowProps().spacingOf(20))
                .r_child(MenuItem("Novo", Entypo.ADD_TO_LIST, "green", () -> limpar()))
                .r_child(MenuItem("Salvar (CTRL + S)", Entypo.SAVE, "green", () -> executar(vm::salvar)))
                .r_child(MenuItem("Editar", Entypo.EDIT, "blue", () -> executar(vm::atualizar)))
                .r_child(MenuItem("Excluir", Entypo.TRASH, "red", () -> executar(vm::excluir)))
                .r_child(new SpacerHorizontal().fill())
                .r_child(MenuItem("Sair", Entypo.REPLY, "red", () -> router.closeSpawn("cad-produtos/"+id)));
    }

    Component MenuItem(String title,Ikon ikon,  String color, Runnable onClick){

       var icon = Component.FromJavaFxNode(FontIcon.of(ikon, 50, Color.web(color)));

        return new Card(
                new Column(new ColumnProps().centerHorizontally())
                    .c_child(new Clickable(icon).onClick(onClick::run))
                    .c_child(new Text(title, new TextProps().fontSize(19))));
    }



    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }

    private void executar(Action action) {
        try {
            action.run();
            IO.println("Operação realizada com sucesso");
        } catch (Exception e) {
            IO.println(e.getMessage());
        }
    }

    private void limpar() {
        vm.descricao.set("");
        vm.precoCompra.set("0");
        vm.precoVenda.set("0");
        vm.estoque.set("0");
        vm.observacoes.set("");
    }
}

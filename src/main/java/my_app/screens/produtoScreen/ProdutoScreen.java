package my_app.screens.produtoScreen;

import javafx.scene.paint.Color;
import javafx.scene.layout.Priority;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.Button;
import megalodonte.components.Component;
import megalodonte.components.Image;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.CardProps;
import megalodonte.props.TextProps;
import megalodonte.router.Router;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.screens.components.Components;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.entypo.Entypo;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static my_app.screens.produtoScreen.ProdutoComponents.*;

public class ProdutoScreen {
    private final Router router;
    private final ProdutoScreenViewModel vm;
    private final Theme theme = ThemeManager.theme();

    public ProdutoScreen(Router router) {
        this.router = router;
        this.vm = new ProdutoScreenViewModel();
    }

    public Component render (){
        return new Column(new ColumnProps().paddingAll(25), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(Components.commonCustomMenus(this::handleClickNew, this::handleClickEdit, this::handleClickDelete))
                .c_child(new SpacerVertical(20))
                .c_child(createHeaderSection())
                .c_child(new SpacerVertical(30))
                .c_child(createMainContent());
    }

    void handleClickNew(){
        limparFormulario();
        IO.println("Formulário limpo para novo produto");
    }

    void handleClickEdit(){
        String codigo = vm.codigoBarras.get();
        if (codigo.isEmpty()) {
            IO.println("Digite um código de barras para buscar");
            return;
        }
        
        try {
            vm.buscar();
            IO.println("Produto carregado para edição");
        } catch (Exception e) {
            IO.println("Erro ao buscar produto: " + e.getMessage());
        }
    }

    void handleClickDelete(){
        String codigo = vm.codigoBarras.get();
        if (codigo.isEmpty()) {
            IO.println("Digite um código de barras para excluir");
            return;
        }
        
        try {

                vm.excluir();
                vm.refreshProdutos();
                limparFormulario();
                IO.println("Produto excluído com sucesso");

        } catch (Exception e) {
            IO.println("Erro ao excluir produto: " + e.getMessage());
        }
    }


    private Component createHeaderSection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(25))
                        .c_child(new Text("Cadastro de Produtos", 
                                new TextProps().fontSize(28).bold()))
                        .c_child(new SpacerVertical(10))
                        .c_child(new Text("Gerencie o catálogo de produtos do seu estabelecimento", 
                                new TextProps().fontSize(16), new TextStyler().color("#6b7280"))),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    private Component createMainContent() {
        return new Row(new RowProps().spacingOf(30))
                .r_child(createFormSection())
                .r_child(createTableSection());
    }

    private Component createFormSection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(25))
                        .c_child(new Text("Dados do Produto", 
                                new TextProps().fontSize(20).bold()))
                        .c_child(new SpacerVertical(20))
                        .c_child(ContainerLeft(vm))
                        .c_child(new SpacerVertical(25))
                        .c_child(createActionButtons()),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    private Component createTableSection() {
        return new Card(
                new Column(new ColumnProps().paddingAll(25))
                        .c_child(new Row(new RowProps().spacingOf(10))
                                .r_child(new Text("Produtos Cadastrados", 
                                        new TextProps().fontSize(20).bold()))
                                .r_child(new Button("Atualizar", 
                                        new ButtonProps()
                                                .height(35)
                                                .bgColor("#2563eb")
                                                .textColor("white")
                                                .onClick(() -> vm.refreshProdutos()))))
                        .c_child(new SpacerVertical(15))
                        .c_child(ProdutosTable(vm.produtos)),
                new CardProps()
                        .padding(0)
                        .radius(12)
        );
    }

    private Component createActionButtons() {
        return new Row(new RowProps().spacingOf(10))
                .r_child(new Button("Salvar", 
                        new ButtonProps()
                                .fillWidth()
                                .height(45)
                                .bgColor("#10b981")
                                .textColor("white")
                                .fontSize(16)
                                .onClick(() -> handleSalvar())))
                .r_child(new Button("Limpar", 
                        new ButtonProps()
                                .fillWidth()
                                .height(45)
                                .bgColor("#6b7280")
                                .textColor("white")
                                .fontSize(16)
                                .onClick(this::limparFormulario))
                );
    }

    private void handleSalvar() {
        try{
            vm.salvar();
            vm.refreshProdutos();
            limparFormulario();
            IO.println("Produto salvo com sucesso");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }


    private void limparFormulario() {
        vm.codigoBarras.set("");
        vm.descricao.set("");
        vm.precoCompra.set("0");
        vm.precoVenda.set("0");
        vm.margem.set("0");
        vm.lucro.set("0");
        vm.comissao.set("");
        vm.garantia.set("");
        vm.marca.set("");
        vm.unidadeSelected.set("UN");
        vm.estoque.set("0");
        vm.validade.set("");
        vm.observacoes.set("");
        vm.imagem.set("/assets/produto-generico.png");
    }
}

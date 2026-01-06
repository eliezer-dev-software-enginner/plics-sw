package my_app.screens.categoriasScreen;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.CardProps;
import megalodonte.props.ListProps;
import megalodonte.props.TextProps;
import megalodonte.props.TextTone;
import megalodonte.props.TextVariant;
import megalodonte.router.Router;
import megalodonte.styles.CardStyler;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;
import my_app.db.repositories.LicensaRepository;
import my_app.screens.components.Components;
import my_app.screens.components.CategoriaListExample;
import megalodonte.components.MegalodonteList;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class CategoriaScreen {
    private final Router router;

    State<String> nome = new State<>("");
    State<String> descricao = new State<>("");
    State<String> search = new State<>("");
    private final List<CategoriaListExample.Categoria> categorias = new ArrayList<>();
    
    public CategoriaScreen(Router router) {
        this.router = router;
        
        // Inicializar dados de exemplo
        categorias.add(new CategoriaListExample.Categoria(1, "Eletrônicos", "Produtos eletrônicos e gadgets", 124));
        categorias.add(new CategoriaListExample.Categoria(2, "Vestuário", "Roupas masculinas e femininas", 85));
        categorias.add(new CategoriaListExample.Categoria(3, "Alimentos", "Produtos perecíveis e mercearia", 210));
    }

    private Theme theme = ThemeManager.theme();

    public Component render (){
        Consumer<CategoriaListExample.Categoria> onEdit = categoria -> {
            nome.set(categoria.nome);
            descricao.set(categoria.descricao);
        };
        
        Consumer<CategoriaListExample.Categoria> onDelete = categoria -> {
            System.out.println("Excluindo categoria: " + categoria.nome);
            // TODO: Implementar exclusão no banco
        };
        
        return new Column(new ColumnProps().paddingAll(20), new ColumnStyler().bgColor(theme.colors().background()))
                .c_child(new Text("BR Nation", new TextProps().tone(TextTone.PRIMARY).variant(TextVariant.TITLE)))
                .c_child(new Text("Gerenciamento de categoria de Estoque",
                        new TextProps().variant(TextVariant.SUBTITLE), new TextStyler().color("#94a3b8")))
                .c_child(new SpacerVertical(10))
                .c_child(new Card(form(),new CardProps().padding(20),
                        new CardStyler().bgColor(theme.colors().surface())
                                .borderColor(theme.colors().secondary())
                        .borderWidth(1)))
                .c_child(new SpacerVertical(10))
                .c_child(CategoriaListExample.createCategoriaList(categorias, search, onEdit, onDelete));
    }

    Component form(){
        return new Column()
                .c_child(
                        new Row()
                                .r_child(new Text("Cadastrar Nova Categoria", new TextProps().bold().variant(TextVariant.SUBTITLE))))
                .c_child(new SpacerVertical(20))
                .c_child(new Row(new RowProps().bottomVertically().spacingOf(10))
                        .r_child(new Input(nome, new InputProps().height(45).fontSize(18).placeHolder("Ex: Eletrônicos")))
                        .r_child(new Button("+ Adicionar", new ButtonProps().fillWidth().height(45).bgColor("#2563eb").fontSize(20).textColor("white").onClick(()-> {}))))
                ;
    }


}
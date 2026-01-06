package my_app.screens.components;

import megalodonte.*;
import megalodonte.components.*;
import megalodonte.props.ListProps;
import megalodonte.props.TextProps;
import megalodonte.props.TextTone;
import megalodonte.props.TextVariant;
import megalodonte.styles.ListStyler;

import java.util.List;
import java.util.function.Consumer;

/**
 * Exemplo de uso do componente List para categorias
 */
public class CategoriaListExample {
    
    // Modelo de dados
    public static class Categoria {
        public final int id;
        public final String nome;
        public final String descricao;
        public final int quantidade;
        
        public Categoria(int id, String nome, String descricao, int quantidade) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
            this.quantidade = quantidade;
        }
        
        @Override
        public String toString() {
            return nome; // Para busca funcionar
        }
    }
    
    // Renderer personalizado para categorias
    private static class CategoriaRenderer implements ListItemRenderer<Categoria> {
        @Override
        public Component render(Categoria categoria, int index, ListProps<Categoria> props) {
            return new Row(new RowProps().spacingOf(16).paddingAll(16))
                    .r_child(
                        new Column(new ColumnProps().spacingOf(4))
                            .c_child(new Text(categoria.nome, 
                                new TextProps().variant(TextVariant.SUBTITLE)))
                            .c_child(new Text(categoria.descricao, 
                                new TextProps()
                                    .variant(TextVariant.SMALL)
                                    .tone(TextTone.SECONDARY)))
                    )
                    .r_child(new SpacerHorizontal())
                    .r_child(
                        new Column(new ColumnProps().spacingOf(4))
                            .c_child(new Text(String.valueOf(categoria.quantidade), 
                                new TextProps()
                                    .variant(TextVariant.SUBTITLE)
                                    .tone(TextTone.PRIMARY)))
                            .c_child(new Text("itens", 
                                new TextProps()
                                    .variant(TextVariant.SMALL)
                                    .tone(TextTone.SECONDARY)))
                    );
        }
        
        @Override
        public Component renderHeader() {
            return new Row(new RowProps()
                    .spacingOf(16)
                    .paddingAll(16))
                    .r_child(new Text("Categoria", new TextProps()
                            .variant(TextVariant.SUBTITLE)
                            .tone(TextTone.PRIMARY)))
                    .r_child(new SpacerHorizontal())
                    .r_child(new Text("Quantidade", new TextProps()
                            .variant(TextVariant.SUBTITLE)
                            .tone(TextTone.PRIMARY)));
        }
    }
    
    // Método que cria o componente de lista
    public static Component createCategoriaList(
            List<Categoria> categorias, 
            State<String> searchTerm,
            Consumer<Categoria> onEdit,
            Consumer<Categoria> onDelete
    ) {
        return new MegalodonteList<>(
                new ListProps<Categoria>()
                        .items(categorias)
                        .searchTerm(searchTerm)
                        .onEdit(onEdit)
                        .onDelete(onDelete)
                        .showSearch(true)
                        .showHeader(true)
                        .showActions(true)
                        .emptyMessage("Nenhuma categoria encontrada"),
                new CategoriaRenderer(),
                new ListStyler<Categoria>()
                        .bgColor("#1e293b")
                        .borderColor("#334155")
                        .borderWidth(1)
                        .borderRadius(8)
                        .headerBgColor("#0f172a")
                        .headerTextColor("#ffffff")
                        .rowHoverBgColor("#334155")
                        .searchBgColor("#0f172a")
                        .emptyTextColor("#64748b")
        );
    }
}
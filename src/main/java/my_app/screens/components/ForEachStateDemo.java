package my_app.screens.components;

import megalodonte.ListState;
import megalodonte.State;
import megalodonte.ForEachState;
import megalodonte.components.*;
import megalodonte.props.ButtonProps;
import megalodonte.props.TextProps;

import java.util.Arrays;
import java.util.List;

/**
 * Exemplo simples do ForEachState funcionando
 */
public class ForEachStateDemo {
    
    public record Produto(String nome, double preco) {}
    
    public static Component create() {
        // Estado com lista de produtos
        ListState<Produto> produtosState = ListState.of(Arrays.asList(
            new Produto("Café", 15.00),
            new Produto("Pão", 8.00)
        ));
        
        // Cria o ForEachState que mapeia produtos para botões
        ForEachState<Produto, Button> forEachState = ForEachState.of(
            produtosState,
            produto -> new Button(produto.nome + " - R$ " + produto.preco)
        );

        Column mainColumn = new Column()
                .c_child(new Text("Produtos Disponíveis", new TextProps().fontSize(20).bold()))

                .c_child( new Button("Adicionar Novo Produto", new ButtonProps().onClick(()->{
                    // Usando o método add()
                    produtosState.add(new Produto("Teste", 10));
                })))
                // Usando o método items() implementado!
                .items(forEachState)
                // Voltando a usar c_child() normal - VBox separado garante isolamento
                .c_child(new Button("Remover Último", new ButtonProps().onClick(()->{
                    // Usando o método removeLast()
                    produtosState.removeLast();
                })))
                .c_child(new Button("Remover Caros (>R$ 20)", new ButtonProps().onClick(()->{
                    // Usando o método removeIf() com cast
                    produtosState.removeIf(produto -> {
                        ForEachStateDemo.Produto p = (ForEachStateDemo.Produto) produto;
                        return p.preco > 20;
                    });
                })))
                .c_child(new Button("Editar 'Pão'", new ButtonProps().onClick(()->{
                    // Usando o método replace() para editar
                    Produto produtoEditado = new Produto("Pão Francês", 12.00);
                    produtosState.replace(new Produto("Pão", 8.00), produtoEditado);
                })))
                .c_child(new Button("Editar por Índice (1)", new ButtonProps().onClick(()->{
                    // Usando o método set(index, item) para editar por posição
                    Produto produtoEditado = new Produto("Pão Integral", 15.00);
                    produtosState.set(1, produtoEditado);
                })))
                .c_child(new Button("Remover 'Teste'", new ButtonProps().onClick(()->{
                    // Usando o método remove() específico
                    produtosState.remove(new Produto("Teste", 10));
                })))
                .c_child(new Text("Operações disponíveis:"));
        
        return mainColumn;
    }
}
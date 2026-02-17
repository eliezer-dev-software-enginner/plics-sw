package my_app.screens.components;

import megalodonte.State;
import megalodonte.ForEachState;
import megalodonte.components.*;
import megalodonte.props.TextProps;

import java.util.Arrays;
import java.util.List;

/**
 * Exemplo simples e funcional do ForEachState
 */
public class SimpleForEachDemo {
    
    public record Item(String nome) {}
    
    public static Component create() {
        // Estado com lista de itens
        State<List<Item>> itensState = State.of(Arrays.asList(
            new Item("Primeiro Item"),
            new Item("Segundo Item"),
            new Item("Terceiro Item")
        ));
        
        // Cria o ForEachState
        ForEachState<Item, Button> forEachState = ForEachState.of(
            itensState,
            item -> new Button(item.nome)
        );
        
        // Coluna principal simples
        Column coluna = new Column();
        
        // Título
        coluna.c_child(new Text("Lista de Itens", (TextProps) new TextProps().fontSize(20).bold()));
        
        // Adiciona os botões do ForEachState
        List<Button> botoes = forEachState.getComponents();
        for (Button btn : botoes) {
            coluna.c_child(btn);
        }
        
        return coluna;
    }
}
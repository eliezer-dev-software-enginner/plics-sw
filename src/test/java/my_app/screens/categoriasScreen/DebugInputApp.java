package my_app.screens.categoriasScreen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.TextProps;
import megalodonte.props.TextVariant;
import megalodonte.theme.Theme;
import megalodonte.theme.ThemeManager;

public class DebugInputApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        State<String> nome = State.of("");
        
        // Input igual ao do CategoriaScreen
        Input input = new Input(nome, 
            new megalodonte.InputProps().height(45).fontSize(18).placeHolder("Ex: Eletrônicos"));
        
        Button button = new Button("+ Adicionar");
        button.setOnAction(e -> {
            String value = nome.get();
            System.out.println("Valor no State: '" + value + "'");
            System.out.println("Valor no Input: '" + input.field.getText() + "'");
        });
        
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10, input.getNode(), button);
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
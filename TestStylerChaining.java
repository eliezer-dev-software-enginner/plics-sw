// Teste para verificar se os métodos fluentes funcionam corretamente
public class TestStylerChaining {
    public static void main(String[] args) {
        // Teste ColumnStyler
        var columnStyler = new megalodonte.ColumnStyler()
            .bgColor("#fff")
            .borderColor("black")
            .borderWidth(1);
        
        // Teste RowStyler  
        var rowStyler = new megalodonte.RowStyler()
            .bgColor("red")
            .borderColor("blue")
            .borderWidth(2);
            
        // Teste CardStyler
        var cardStyler = new megalodonte.styles.CardStyler()
            .bgColor("green")
            .borderColor("yellow")
            .borderWidth(3);
            
        // Teste InputStyler
        var inputStyler = new megalodonte.styles.InputStyler()
            .bgColor("gray")
            .placeholderColor("lightgray")
            .borderColor("darkgray")
            .borderWidth(1);
            
        // Teste TextStyler
        var textStyler = new megalodonte.TextStyler()
            .color("white")
            .textColor("black");
            
        System.out.println("✅ Todos os stylers funcionam com chaining como antes!");
    }
}
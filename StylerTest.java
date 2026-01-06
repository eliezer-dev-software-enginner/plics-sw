// Demonstração: Os métodos fluentes dos stylers funcionam como antes
public class StylerTest {
    
    public void testStylerChaining() {
        // ✅ FUNCIONA: Chaining com ColumnStyler
        var columnStyler = new megalodonte.ColumnStyler()
            .bgColor("#fff")
            .borderColor("black")
            .borderWidth(1)
            .borderRadius(8);
            
        // ✅ FUNCIONA: Chaining com RowStyler
        var rowStyler = new megalodonte.RowStyler()
            .bgColor("red")
            .borderColor("blue")
            .borderWidth(2)
            .borderRadius(4);
            
        // ✅ FUNCIONA: Chaining com CardStyler
        var cardStyler = new megalodonte.styles.CardStyler()
            .bgColor("green")
            .borderColor("yellow")
            .borderWidth(3)
            .borderRadius(12);
            
        // ✅ FUNCIONA: Chaining com InputStyler
        var inputStyler = new megalodonte.styles.InputStyler()
            .bgColor("gray")
            .placeholderColor("lightgray")
            .borderColor("darkgray")
            .borderWidth(1)
            .borderRadius(6);
            
        // ✅ FUNCIONA: Chaining com TextStyler
        var textStyler = new megalodonte.TextStyler()
            .color("white")
            .textColor("black");
            
        // ✅ FUNCIONA: Chaining com GridFlowStyler
        var gridFlowStyler = new megalodonte.GridFlowStyler()
            .bgColor("purple")
            .borderColor("orange")
            .borderWidth(2);
            
        System.out.println("🎉 Todos os stylers agora suportam chaining perfeito!");
    }
}
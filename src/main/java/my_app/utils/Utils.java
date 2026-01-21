package my_app.utils;

import javafx.scene.control.TableView;
import my_app.db.models.ModelBase;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class Utils {
    public static String toBRLCurrency(BigDecimal value){
        final NumberFormat BRL =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return BRL.format(value);

    }

    public static <T> void onItemTableSelectedChange(TableView<T> table, Consumer<T> eventHandler){
        table.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                eventHandler.accept(newSelection);
            }
        });

    }

    /**
     * Esse método é usado para transformar os centavos visuais para valor em Real que será persistido no banco de dados.
     * 1000 centavos equivalem a 10 reais.
     * A conversão entre centavos e reais é baseada na relação de que 1 real = 100 centavos.  Para converter centavos em reais, basta dividir o número de centavos por 100:
     *
     * 1000 centavos ÷ 100 = 10 reais
     * @param centavos
     * @return
     */
    public static BigDecimal deCentavosParaReal(String centavos){
        return new BigDecimal(centavos).movePointLeft(2);
    }


    /**
     * Transforma o valor em Real persitido no banco para centavos para utilizar nos inputs
     * @param real valor em Real recuperado do banco
     * @return valor em centavos para exibição nos inputs
     */
    public static String deRealParaCentavos(BigDecimal real){
        return real.multiply(new BigDecimal("100")).toPlainString();
    }
}

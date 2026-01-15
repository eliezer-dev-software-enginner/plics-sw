package my_app.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Utils {
    public static String toBRLCurrency(BigDecimal value){
        final NumberFormat BRL =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return BRL.format(value);

    }
}

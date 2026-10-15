package my_app.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static pack.utilities.DatePack.localDateParaMillis;

/** One installment with an exact currency amount. */
public record Parcela(int numero, Long dataVencimento, BigDecimal valor) {
    public static List<Parcela> gerarParcelas(LocalDate primeiraData, int quantidade, double total) {
        return gerarParcelas(primeiraData, quantidade, BigDecimal.valueOf(total));
    }

    public static List<Parcela> gerarParcelas(LocalDate primeiraData, int quantidade, BigDecimal total) {
        if (primeiraData == null) throw new IllegalArgumentException("A data do primeiro vencimento é obrigatória.");
        if (quantidade < 1) throw new IllegalArgumentException("A quantidade de parcelas deve ser positiva.");
        if (total == null || total.signum() < 0) throw new IllegalArgumentException("O total deve ser não negativo.");
        long centavos = total.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        long base = centavos / quantidade;
        long resto = centavos % quantidade;
        var parcelas = new ArrayList<Parcela>(quantidade);
        for (int index = 0; index < quantidade; index++) {
            long valorCentavos = base + (index < resto ? 1 : 0);
            parcelas.add(new Parcela(index + 1, localDateParaMillis(primeiraData.plusMonths(index)),
                    BigDecimal.valueOf(valorCentavos, 2)));
        }
        return List.copyOf(parcelas);
    }
}

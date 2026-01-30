package my_app.domain;

import my_app.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public record Parcela(int numero, Long dataVencimento, BigDecimal valor) {

    public static  List<Parcela> gerarParcelas(LocalDate dataPrimeiraParcela, int quantidadeParcelas, double valorTotalLiquido) {
        List<Parcela> novasParcelas = new ArrayList<>();
        double valorParcela = valorTotalLiquido / quantidadeParcelas;
        IO.println("=== GERANDO PARCELAS ===");
        IO.println("Valor total para parcelar: R$ " + valorTotalLiquido);

        for (int i = 0; i < quantidadeParcelas; i++) {
            LocalDate dataVencimento = dataPrimeiraParcela.plusMonths(i);
            Parcela parcela = new Parcela(i + 1, DateUtils.localDateParaMillis(dataVencimento),
                    new BigDecimal(valorParcela));
            novasParcelas.add(parcela);
        }

        // Atualizar o state com as parcelas geradas
       // parcelas.set(novasParcelas);

        IO.println("=== PARCELAS GERADAS ===");
        for (Parcela parcela : novasParcelas) {
            IO.println("Parcela " + parcela.numero() + ": " +
                    DateUtils.millisParaLocalDate(parcela.dataVencimento()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - Valor: R$ " + String.format("%.2f", parcela.valor()));
        }
        IO.println("========================");
        return novasParcelas;
    }

}

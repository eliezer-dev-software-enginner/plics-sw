package my_app.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

public class Utils {
    public static String toBRLCurrency(BigDecimal value){
        final NumberFormat BRL =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return BRL.format(value);

    }

    public static String toBRLCurrency(String value){
        final NumberFormat BRL =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return BRL.format(new BigDecimal(value));
    }



    @Deprecated(forRemoval = true)
    public static <T> void onItemTableSelectedChange(TableView<T> table, Consumer<T> eventHandler){
        table.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                eventHandler.accept(newSelection);
            }
        });

    }

    /**
     * Gera um código de barras no padrão EAN-13 matematicamente válido.
     *
     * <p><b>Como funciona:</b></p>
     * <ul>
     *   <li>Gera 12 dígitos numéricos aleatórios</li>
     *   <li>Calcula o dígito verificador usando o algoritmo oficial do EAN-13</li>
     *   <li>Retorna uma String com 13 dígitos no total</li>
     * </ul>
     *
     * <p><b>Validade:</b></p>
     * <ul>
     *   <li>✔ Código EAN-13 matematicamente correto</li>
     *   <li>✔ Aceito por leitores de código de barras</li>
     *   <li>✔ Ideal para sistemas internos, testes e controle de estoque próprio</li>
     * </ul>
     *
     * <p><b>Limitações importantes:</b></p>
     * <ul>
     *   <li>❌ Não representa um código GS1 oficial</li>
     *   <li>❌ Pode colidir com códigos reais existentes no mercado</li>
     *   <li>❌ Não deve ser usado para produtos destinados a varejo, emissão fiscal ou comercialização oficial</li>
     * </ul>
     *
     * <p><b>Use este método apenas quando:</b></p>
     * <ul>
     *   <li>O sistema for interno</li>
     *   <li>O código servir apenas como identificador técnico</li>
     *   <li>Não houver exigência fiscal ou comercial</li>
     * </ul>
     *
     * @return String contendo um código EAN-13 válido
     */
    public static String gerarCodigoBarrasEAN13() {
        Random random = new Random();
        StringBuilder codigo = new StringBuilder();

        // Gera os 12 dígitos base
        for (int i = 0; i < 12; i++) {
            codigo.append(random.nextInt(10));
        }

        // Calcula o dígito verificador
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            soma += (i % 2 == 0) ? digito : digito * 3;
        }

        int digitoVerificador = (10 - (soma % 10)) % 10;

        return codigo.append(digitoVerificador).toString();
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
        if (centavos == null || centavos.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(centavos).movePointLeft(2);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }


/**
     * Transforma o valor em Real persistido no banco para centavos para utilizar nos inputs
     * @param real valor em Real recuperado do banco (ex: 10.00)
     * @return valor em centavos para exibição nos inputs (ex: "1000")
     */
    public static String deRealParaCentavos(BigDecimal real){
        if (real == null) return "0";
        return real.multiply(new BigDecimal("100")).intValue() + "";
    }

    // Validação simples de E-mail
    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
    }

    // Validação de CNPJ (Remove formatação e verifica se tem 14 dígitos)
// Para uma validação rigorosa (cálculo de dígitos), recomenda-se uma lib ou algoritmo completo.
    public static boolean isValidCnpj(String cnpj) {
        String cleanCnpj = cnpj.replaceAll("[^0-9]", "");
        return cleanCnpj.length() == 14;
    }

    // Valida se o telefone tem 10 ou 11 dígitos numéricos
    public static boolean isValidPhone(String phone) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 11;
    }
    /**
     * Aplica a máscara (XX) XXXXX-XXXX ou (XX) XXXX-XXXX dinamicamente
     */
    public static String formatPhone(String numeric) {
        if (numeric == null || numeric.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        int len = numeric.length();

        if (len > 0) sb.append("(");
        if (len <= 2) {
            sb.append(numeric);
        } else {
            sb.append(numeric.substring(0, 2)).append(") ");
            String rest = numeric.substring(2);

            if (rest.length() <= 4) {
                sb.append(rest);
            } else if (rest.length() == 5) {
                // Formato celular (5 dígitos no primeiro bloco)
                sb.append(rest);
            } else if (rest.length() <= 8) {
                // Formato Fixo: (XX) XXXX-XXXX
                sb.append(rest.substring(0, 4)).append("-").append(rest.substring(4));
            } else {
                // Formato Celular: (XX) XXXXX-XXXX
                sb.append(rest.substring(0, 5)).append("-").append(rest.substring(5));
            }
        }
        return sb.toString();
    }


    @Deprecated(forRemoval = true)
    public static <T> void updateItemOnObservableList(
            ObservableList<T> observableList, T modelSelected, T modelAtualizada
    ){
        int index = observableList.indexOf(modelSelected);
        if (index != -1) {
            observableList.set(index, modelAtualizada);
        }
    }

    public static String quantidadeTratada(BigDecimal quantidade){
        return quantidade.stripTrailingZeros().toPlainString();
    }
}

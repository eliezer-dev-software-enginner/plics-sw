package my_app.utils;

import java.math.BigDecimal;
import java.util.Random;


public class Utils {
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
     * Transforma o valor em Real persistido no banco para centavos para utilizar nos inputs
     * @param real valor em Real recuperado do banco (ex: 10.00)
     * @return valor em centavos para exibição nos inputs (ex: "1000")
     */
    public static String deRealParaCentavos(BigDecimal real){
        if (real == null) return "0";
        return real.multiply(new BigDecimal("100")).setScale(0, java.math.RoundingMode.HALF_UP).toBigInteger().toString();
    }

    // Validação simples de E-mail
    public static boolean isNotValidEmail(String email) {
        return !email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
    }

    public static String quantidadeTratada(BigDecimal quantidade){
        return quantidade.stripTrailingZeros().toPlainString();
    }
}

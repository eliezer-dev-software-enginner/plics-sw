package my_app.services;

import my_app.security.CryptoManager;

/**
 * Checagem provisória de acesso: compara um "dígito verificador" publicado
 * no site oficial (via {@link HtmlParser}) com o valor esperado embutido no
 * aplicativo. URL e valor esperado ficam criptografados aqui (mesmo esquema
 * já usado em TelegramNotifierFactory, via CryptoManager) — não em texto
 * puro no repositório.
 */
public class VerificacaoAcessoService {

    private static final String ENCRYPTED_URL =
            "928sspEfInz6JyYMPCLJl1FQ2e+96BTI/sqhApDD/PE1MivvBQuCvt5sbwCPxbIU";
    private static final String ENCRYPTED_VERIFICADOR_ESPERADO =
            "yPzrRzqmUscjzwrNQe52ka9FK0Pjmh1YtZT/fNQlYJ+CCa21lMyHgLzBheK/JZa3";

    /**
     * @return true se o dígito verificador do site bater com o esperado.
     * Qualquer falha (site fora do ar, sem internet, span ausente) também
     * retorna false — sem internet/site indisponível bloqueia o acesso.
     */
    public static boolean acessoLiberado() {
        try {
            var crypto = new CryptoManager();
            String url = crypto.decrypt(ENCRYPTED_URL);
            String esperado = crypto.decrypt(ENCRYPTED_VERIFICADOR_ESPERADO);
            String obtido = HtmlParser.getVerificador(url);
            return esperado.equals(obtido);
        } catch (Exception e) {
            return false;
        }
    }
}

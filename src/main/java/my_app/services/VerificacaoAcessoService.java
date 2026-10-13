package my_app.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Optional runtime access check configured outside the application package. */
public final class VerificacaoAcessoService {
    private static final Logger log = LoggerFactory.getLogger(VerificacaoAcessoService.class);

    private VerificacaoAcessoService() {}

    public static boolean acessoLiberado() {
        String url = System.getenv("PLICS_ACCESS_VERIFY_URL");
        String esperado = System.getenv("PLICS_ACCESS_VERIFY_EXPECTED");
        if (url == null || url.isBlank() || esperado == null || esperado.isBlank()) {
            return true;
        }
        try {
            boolean liberado = esperado.equals(HtmlParser.getVerificador(url));
            if (!liberado) log.warn("Verificador de acesso não bateu com o esperado");
            return liberado;
        } catch (Exception error) {
            log.warn("Não foi possível consultar o verificador de acesso: {}", error.toString());
            return true;
        }
    }
}

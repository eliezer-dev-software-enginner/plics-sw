package my_app.services;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HtmlParserTest {

    @Test
    void deveExtrairTextoDoSpanVerificador() {
        var dom = Jsoup.parse("<html><body><span class=\"verificador\">abc123</span></body></html>");
        assertEquals("abc123", HtmlParser.extrairVerificador(dom));
    }

    @Test
    void deveRetornarNullQuandoSpanNaoExiste() {
        var dom = Jsoup.parse("<html><body><p>sem verificador aqui</p></body></html>");
        assertNull(HtmlParser.extrairVerificador(dom));
    }
}

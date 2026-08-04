package my_app.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class HtmlParser {
    public static String getVerificador(String url) throws IOException {
        Document dom = Jsoup.connect(url).get();
        return extrairVerificador(dom);
    }

    static String extrairVerificador(Document dom) {
        Element span = dom.select("span.verificador").first();
        return span == null ? null : span.text();
    }
}

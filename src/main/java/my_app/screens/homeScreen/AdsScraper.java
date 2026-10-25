package my_app.screens.homeScreen;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;

/** Reads the public ad markup without executing scripts from the website. */
final class AdsScraper {
    static final URI PAGE = URI.create("https://eliezer-dev.vercel.app/anuncios");
    private final HttpClient client;

    record Ad(String title, URI image, boolean urgent, URI redirect) { }

    AdsScraper() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build());
    }

    AdsScraper(HttpClient client) { this.client = client; }

    List<Ad> fetch() throws IOException, InterruptedException {
        var request=HttpRequest.newBuilder(PAGE).timeout(Duration.ofSeconds(8)).GET().build();
        var response=client.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200) throw new IOException("Página de anúncios: HTTP " + response.statusCode());
        if(response.body().length()>1_000_000) throw new IOException("Página de anúncios maior que o esperado");
        return parse(response.body(),PAGE);
    }

    static List<Ad> parse(String html, URI page) {
        var ads=new ArrayList<Ad>();
        var document=Jsoup.parse(html,page.toString());
        for(var element:document.select("span[data-plics-ad=true]")) {
            add(ads,page,element.attr("data-title"),element.attr("data-image"),
                    "true".equalsIgnoreCase(element.attr("data-urgent")),element.attr("data-redirect"));
        }
        if(!ads.isEmpty()) return List.copyOf(ads);
        // Older published page emits semicolon fields and React comment separators.
        for(var element:document.select("span")) {
            String[] fields=element.wholeText().split(";",-1);
            if(fields.length>=4) add(ads,page,fields[0],fields[1],"true".equalsIgnoreCase(fields[2]),fields[3]);
        }
        return List.copyOf(ads);
    }

    private static void add(List<Ad> ads,URI page,String title,String image,boolean urgent,String redirect) {
        if(image==null || image.isBlank()) return;
        try {
            URI assetBase=page.resolve(page.getPath().endsWith("/") ? page.getPath() : page.getPath()+"/");
            URI imageUrl=assetBase.resolve(image.strip());
            if(!safe(imageUrl)) return;
            URI destination=redirect==null || redirect.isBlank() || redirect.equals("undefined") ? null : page.resolve(redirect.strip());
            if(destination!=null && !safe(destination)) destination=null;
            ads.add(new Ad(title==null || title.isBlank() ? "Conheça esta novidade" : title.strip(),imageUrl,urgent,destination));
        } catch(IllegalArgumentException ignored) { }
    }

    private static boolean safe(URI uri) {
        return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                && uri.getHost()!=null;
    }

}

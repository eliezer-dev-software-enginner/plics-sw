package my_app.screens.homeScreen;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the public ad markup without executing scripts from the website. */
final class AdsScraper {
    static final URI PAGE = URI.create("https://eliezer-dev.vercel.app/anuncios");
    private static final Pattern TAG = Pattern.compile("<span\\b([^>]*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTRIBUTE = Pattern.compile("([\\w-]+)=\"([^\"]*)\"");
    private static final Pattern LEGACY = Pattern.compile("<span>(.*?)</span>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
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
        Matcher tags=TAG.matcher(html);
        while(tags.find()) {
            String attributes=tags.group(1);
            if(!attributes.contains("data-plics-ad=")) continue;
            var values=new java.util.HashMap<String,String>();
            Matcher matches=ATTRIBUTE.matcher(attributes);
            while(matches.find()) values.put(matches.group(1),decode(matches.group(2)));
            add(ads,page,values.get("data-title"),values.get("data-image"),
                    "true".equalsIgnoreCase(values.get("data-urgent")),values.get("data-redirect"));
        }
        if(!ads.isEmpty()) return List.copyOf(ads);
        // Older published page emits semicolon fields and React comment separators.
        Matcher legacy=LEGACY.matcher(html);
        while(legacy.find()) {
            String[] fields=decode(legacy.group(1).replace("<!-- -->","")).split(";",-1);
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

    private static String decode(String value) {
        return value.replace("&amp;","&").replace("&quot;","\"").replace("&#x27;", "'")
                .replace("&lt;","<").replace("&gt;",">");
    }
}

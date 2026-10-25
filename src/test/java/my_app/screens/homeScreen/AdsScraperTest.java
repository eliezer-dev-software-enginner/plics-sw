package my_app.screens.homeScreen;

import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

class AdsScraperTest {
    private static final URI PAGE=URI.create("https://eliezer-dev.vercel.app/anuncios");

    @Test void parsesUrgentAndRegularAdsFromRenderedMarkup() {
        String html="<main>"+
                "<span data-plics-ad=\"true\" data-title=\"Bots &amp; automação\" data-image=\"/_next/static/media/bots.png\" data-urgent=\"false\" data-redirect=\"https://example.com/bots\"></span>"+
                "<span data-plics-ad=\"true\" data-title=\"Oferta\" data-image=\"./assets/oferta.png\" data-urgent=\"true\" data-redirect=\"https://example.com/oferta\"></span></main>";
        var ads=AdsScraper.parse(html,PAGE);
        assertEquals(2,ads.size());
        assertEquals("Bots & automação",ads.getFirst().title());
        assertFalse(ads.getFirst().urgent());
        assertEquals("https://eliezer-dev.vercel.app/_next/static/media/bots.png",ads.getFirst().image().toString());
        assertTrue(ads.get(1).urgent());
        assertEquals("https://eliezer-dev.vercel.app/anuncios/assets/oferta.png",ads.get(1).image().toString());
    }

    @Test void acceptsOldMarkupWithoutTreatingMissingBooleanAsUrgent() {
        var ads=AdsScraper.parse("<span>;<!-- -->./assets/banner.png<!-- -->;<!-- -->;<!-- -->undefined</span>",PAGE);
        assertEquals(1,ads.size());
        assertFalse(ads.getFirst().urgent());
        assertEquals("https://eliezer-dev.vercel.app/",ads.getFirst().redirect().toString());
    }
}

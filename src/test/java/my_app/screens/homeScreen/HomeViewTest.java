package my_app.screens.homeScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeViewTest {
    @Test void homeViewKeepsDynamicAdsWithoutFixedPromotion() throws Exception {
        var actions = new Actions();
        for (var name : new String[]{"sales", "products", "customers", "receivables", "pos", "categories",
                "suppliers", "orders", "company", "reports", "preferences", "feedback", "updates", "logs",
                "terms", "checkUpdates", "videos"}) {
            actions.register(name, ignored -> {});
        }
        try (var session = UiDefinition.fromResource(getClass(), "/views/home.view.jsonc").open(actions)) {
            assertEquals("home", session.id());
            String view=new String(getClass().getResourceAsStream("/views/home.view.jsonc").readAllBytes(),StandardCharsets.UTF_8);
            assertTrue(view.contains("\"name\": \"urgentAds\""));
            assertTrue(view.indexOf("\"name\": \"regularAds\"") > view.indexOf("Canal de vídeos"));
            assertFalse(view.contains("Impulsione seu Instagram"));
            assertFalse(view.contains("showPromo"));
        }
    }
}

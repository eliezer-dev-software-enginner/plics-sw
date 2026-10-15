package my_app.screens.homeScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeViewTest {
    @Test void homeViewLoadsWithNavigationAndPromotionActions() {
        var actions = new Actions();
        for (var name : new String[]{"sales", "products", "customers", "receivables", "pos", "categories",
                "suppliers", "orders", "company", "reports", "preferences", "feedback", "updates", "logs",
                "terms", "checkUpdates", "videos", "instagram", "buyInstagram"}) {
            actions.register(name, ignored -> {});
        }
        try (var session = UiDefinition.fromResource(getClass(), "/views/home.view.jsonc").open(actions)) {
            assertEquals("home", session.id());
            assertEquals(false, session.states().get("showPromo"));
        }
    }
}

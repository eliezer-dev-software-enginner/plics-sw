package my_app.screens.empresaScreen;

import megalodonte.jsonc.Actions;
import megalodonte.jsonc.UiDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmpresaViewTest {
    @Test void companyFormDeclaresEditableFieldsAndActions() {
        var definition = UiDefinition.fromResource(getClass(), "/views/empresa.view.jsonc");
        try (var session = definition.open(new Actions()
                .register("chooseLogo", ignored -> {})
                .register("save", ignored -> {}))) {
            assertEquals("companySettings", session.id());
            assertEquals("", session.states().string("nome").get());
            assertEquals("", session.states().string("logo").get());
        }
    }
}

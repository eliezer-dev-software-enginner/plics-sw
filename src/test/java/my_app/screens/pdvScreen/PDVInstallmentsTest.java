package my_app.screens.pdvScreen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PDVInstallmentsTest {
    @Test void acceptsPositiveInstallmentCount() {
        assertEquals(3, PDVScreenViewModel.validarNumeroParcelas(" 3 "));
    }

    @Test void rejectsInvalidInstallmentCount() {
        for (String value : new String[]{"", "abc", "0", "-2", "999999999999"}) {
            assertThrows(NumberFormatException.class, () -> PDVScreenViewModel.validarNumeroParcelas(value));
        }
        assertThrows(NumberFormatException.class, () -> PDVScreenViewModel.validarNumeroParcelas(null));
    }
}

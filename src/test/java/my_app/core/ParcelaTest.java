package my_app.core;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParcelaTest {
    @Test void dividesCentsWithoutLosingOrCreatingMoney() {
        var parcelas = Parcela.gerarParcelas(LocalDate.of(2026, 10, 15), 3, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("33.34"), parcelas.get(0).valor());
        assertEquals(new BigDecimal("33.33"), parcelas.get(1).valor());
        assertEquals(new BigDecimal("100.00"), parcelas.stream().map(Parcela::valor).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test void rejectsInvalidCount() {
        assertThrows(IllegalArgumentException.class, () -> Parcela.gerarParcelas(LocalDate.now(), 0, BigDecimal.ONE));
    }
}

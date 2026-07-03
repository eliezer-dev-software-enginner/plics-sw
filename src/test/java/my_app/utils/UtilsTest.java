package my_app.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void isValidCnpj_aceitaNumericoValido() {
        assertTrue(Utils.isValidCnpj("11222333000181"));
    }

    @Test
    void isValidCnpj_aceitaNumericoComMascara() {
        assertTrue(Utils.isValidCnpj("11.222.333/0001-81"));
    }

    @Test
    void isValidCnpj_aceitaAlfanumericoValido() {
        assertTrue(Utils.isValidCnpj("12ABC34501DE35"));
    }

    @Test
    void isValidCnpj_aceitaAlfanumericoComMascara() {
        assertTrue(Utils.isValidCnpj("12.ABC.345/01DE-35"));
    }

    @Test
    void isValidCnpj_rejeitaNull() {
        assertFalse(Utils.isValidCnpj(null));
    }

    @Test
    void isValidCnpj_rejeitaVazio() {
        assertFalse(Utils.isValidCnpj(""));
    }

    @Test
    void isValidCnpj_rejeitaTamanhoIncorreto() {
        assertFalse(Utils.isValidCnpj("123"));
    }

    @Test
    void isValidCnpj_rejeitaUltimosDigitosComLetra() {
        assertFalse(Utils.isValidCnpj("112223330001AB"));
    }

    @Test
    void isValidCnpj_rejeitaLetrasInvalidas() {
        assertFalse(Utils.isValidCnpj("OI__ABC__DE__FG"));
    }

    @Test
    void isValidCnpj_aceitaMistoNumericoAlfanumerico() {
        assertTrue(Utils.isValidCnpj("12AB3456789C11"));
    }

    @Test
    void isValidCnpj_rejeitaUltimosDigitosLetra() {
        assertFalse(Utils.isValidCnpj("12ABC34501DEAB"));
    }

    @Test
    void isValidCnpj_aceitaMistoLetrasDigitos() {
        assertTrue(Utils.isValidCnpj("AB123456789082"));
}
}

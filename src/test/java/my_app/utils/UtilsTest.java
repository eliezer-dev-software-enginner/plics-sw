package my_app.utils;

import org.junit.jupiter.api.Test;
import pack.utilities.ValidatorPack;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void isValidCnpj_aceitaNumericoValido() {
        assertTrue(ValidatorPack.isValidCnpj("11222333000181"));
    }

    @Test
    void isValidCnpj_aceitaNumericoComMascara() {
        assertTrue(ValidatorPack.isValidCnpj("11.222.333/0001-81"));
    }

    @Test
    void isValidCnpj_aceitaAlfanumericoValido() {
        assertTrue(ValidatorPack.isValidCnpj("12ABC34501DE35"));
    }

    @Test
    void isValidCnpj_aceitaAlfanumericoComMascara() {
        assertTrue(ValidatorPack.isValidCnpj("12.ABC.345/01DE-35"));
    }

    @Test
    void isValidCnpj_rejeitaNull() {
        assertFalse(ValidatorPack.isValidCnpj(null));
    }

    @Test
    void isValidCnpj_rejeitaVazio() {
        assertFalse(ValidatorPack.isValidCnpj(""));
    }

    @Test
    void isValidCnpj_rejeitaTamanhoIncorreto() {
        assertFalse(ValidatorPack.isValidCnpj("123"));
    }

    @Test
    void isValidCnpj_rejeitaUltimosDigitosComLetra() {
        assertFalse(ValidatorPack.isValidCnpj("112223330001AB"));
    }

    @Test
    void isValidCnpj_rejeitaLetrasInvalidas() {
        assertFalse(ValidatorPack.isValidCnpj("OI__ABC__DE__FG"));
    }

    @Test
    void isValidCnpj_aceitaMistoNumericoAlfanumerico() {
        assertTrue(ValidatorPack.isValidCnpj("12AB3456789C11"));
    }

    @Test
    void isValidCnpj_rejeitaUltimosDigitosLetra() {
        assertFalse(ValidatorPack.isValidCnpj("12ABC34501DEAB"));
    }

    @Test
    void isValidCnpj_aceitaMistoLetrasDigitos() {
        assertTrue(ValidatorPack.isValidCnpj("AB123456789082"));
}
}

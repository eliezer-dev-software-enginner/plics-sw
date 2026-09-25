package my_app.core.telegram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelegramNotifierTest {

    @Test
    void formataMacComHexadecimalMaiusculoEDoisPontos() {
        byte[] address = {0x00, 0x1A, 0x2B, 0x0C, (byte) 0xFE, (byte) 0xFF};

        assertEquals("00:1A:2B:0C:FE:FF", TelegramNotifier.formatMacAddress(address));
    }

    @Test
    void macAusenteTemFallbackLegivel() {
        assertEquals("indisponível", TelegramNotifier.formatMacAddress(null));
        assertEquals("indisponível", TelegramNotifier.formatMacAddress(new byte[0]));
    }
}

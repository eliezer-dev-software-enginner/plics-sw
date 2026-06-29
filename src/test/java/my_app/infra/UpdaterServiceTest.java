package my_app.infra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdaterServiceTest {

    @Test
    void versaoRemotaEhMaisNovaQueAtual() throws Exception {
        var service = new UpdaterService();
        assertTrue(service.hasUpdate("0.0.1"),
            "Deveria ter update pois 0.0.1 < latest");
    }

    @Test
    void versaoAtualIgualRemotaNaoTemUpdate() throws Exception {
        var latest = new UpdaterService().getLatestVersion();
        assertFalse(new UpdaterService().hasUpdate(latest),
            "Nao deveria ter update se versao atual ja e a latest (" + latest + ")");
    }

    @Test
    void retornaLatestVersionValida() throws Exception {
        var version = new UpdaterService().getLatestVersion();
        assertNotNull(version);
        assertFalse(version.isBlank());
        System.out.println("Ultima versao no GitHub: " + version);
    }

    @Test
    void comparaVersoesCorretamente() {
        assertTrue(UpdaterService.compareVersions("2.0.0", "1.0.0") > 0);
        assertTrue(UpdaterService.compareVersions("1.0.0", "2.0.0") < 0);
        assertEquals(0, UpdaterService.compareVersions("1.5.3", "1.5.3"));
        assertTrue(UpdaterService.compareVersions("1.10.0", "1.9.9") > 0);
    }
}

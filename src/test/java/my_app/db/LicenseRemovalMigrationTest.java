package my_app.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LicenseRemovalMigrationTest {
    @TempDir Path tempDir;

    @Test
    void removesActivationDataButPreservesLocalCredentials() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("legacy.db");
        Flyway.configure().dataSource(url, "", "")
                .locations("classpath:flyway_migrations").target("37").load().migrate();
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE preferencias SET login = 'saved-user', senha = 'saved-password', licensa = 'old-key'");
        }

        Flyway.configure().dataSource(url, "", "")
                .locations("classpath:flyway_migrations").load().migrate();
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT login, senha FROM preferencias")) {
                assertEquals(true, result.next());
                assertEquals("saved-user", result.getString(1));
                assertEquals("saved-password", result.getString(2));
            }
            try (var result = statement.executeQuery("PRAGMA table_info(preferencias)")) {
                while (result.next()) assertFalse(result.getString("name").equals("licensa"));
            }
            try (var result = statement.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='licensas'")) {
                assertEquals(true, result.next());
                assertEquals(0, result.getInt(1));
            }
        }
    }
}

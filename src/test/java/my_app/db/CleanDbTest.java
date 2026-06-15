package my_app.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanDbTest {

    @Test
    void limparBancoNaoCorrompeHistoricoFlyway() throws Exception {
        var testUrl = "jdbc:sqlite:file:test_clean_db?mode=memory&cache=shared";

        try (var conn = DriverManager.getConnection(testUrl)) {
            Flyway.configure()
                    .dataSource(testUrl, "", "")
                    .locations("classpath:flyway_migrations")
                    .load()
                    .migrate();
        }

        try (var conn = DriverManager.getConnection(testUrl);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")) {
            rs.next();
            assertEquals(17, rs.getInt(1), "Devem ter 17 migrations aplicadas");
        }

        var sql = readResource("/clean_db.sql");
        try (var conn = DriverManager.getConnection(testUrl);
             var stmt = conn.createStatement()) {
            for (var line : sql.split(";")) {
                var trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }

        try (var conn = DriverManager.getConnection(testUrl);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM preferencias")) {
            rs.next();
            assertEquals(0, rs.getInt(1), "Dados das preferencias devem ter sido deletados");
        }

        try (var conn = DriverManager.getConnection(testUrl);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")) {
            rs.next();
            assertEquals(17, rs.getInt(1), "Historico do Flyway deve permanecer intacto");
        }

        Flyway.configure()
                .dataSource(testUrl, "", "")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();

        try (var conn = DriverManager.getConnection(testUrl);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")) {
            rs.next();
            assertEquals(17, rs.getInt(1), "Historico deve continuar intacto apos restart");
        }
    }

    private static String readResource(String path) {
        try (var reader = new BufferedReader(
                new InputStreamReader(CleanDbTest.class.getResourceAsStream(path), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler recurso: " + path, e);
        }
    }
}

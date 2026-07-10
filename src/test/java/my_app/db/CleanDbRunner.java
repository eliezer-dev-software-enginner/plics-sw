package my_app.db;

import org.flywaydb.core.Flyway;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.stream.Collectors;

public class CleanDbRunner {

    private static final String TEST_DB = System.getProperty("java.io.tmpdir")
            .replace("\\", "/") + "/clean_db_test_" + System.currentTimeMillis() + ".db";
    private static final String TEST_URL = "jdbc:sqlite:" + TEST_DB;

    static void main(String[] args) throws Exception {
        System.out.println("=== Teste: limparBanco nao corrompe historico Flyway ===");

        DriverManager.getConnection(TEST_URL);
        Flyway.configure()
                .dataSource(TEST_URL, "", "")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();
        System.out.println("1. Flyway.migrate() executado com sucesso");

        try (var conn = DriverManager.getConnection(TEST_URL);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")) {
            rs.next();
            int count = rs.getInt(1);
            System.out.println("   Migrations aplicadas: " + count);
            if (count != 17) {
                System.err.println("   FALHA: Esperado 17, obtido " + count);
                System.exit(1);
            }
        }

        var sql = readResource();
        System.out.println("2. Executando clean_db.sql:");
        try (var conn = DriverManager.getConnection(TEST_URL);
             var stmt = conn.createStatement()) {
            for (var line : sql.split(";")) {
                var trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    System.out.println("   SQL: " + trimmed.substring(0, Math.min(80, trimmed.length())));
                    stmt.execute(trimmed);
                }
            }
        }
        System.out.println("   clean_db.sql executado com sucesso");

        try (var conn = DriverManager.getConnection(TEST_URL);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM preferencias")) {
            rs.next();
            int count = rs.getInt(1);
            System.out.println("   Registros em preferencias apos limpeza: " + count);
            if (count != 0) {
                System.err.println("   FALHA: Esperado 0");
                System.exit(1);
            }
        }

        try (var conn = DriverManager.getConnection(TEST_URL);
             var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT version, description, installed_on FROM flyway_schema_history ORDER BY version");
            System.out.println("   Historio Flyway apos limpeza:");
            while (rs.next()) {
                System.out.println("      v" + rs.getString("version") + " - " + rs.getString("description"));
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history");
            rs.next();
            int count = rs.getInt(1);
            System.out.println("   Total: " + count);
            if (count != 17) {
                System.err.println("   FALHA: Historico corrompido! Esperado 17, obtido " + count);
                System.exit(1);
            }
        }

        Flyway.configure()
                .dataSource(TEST_URL, "", "")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();
        System.out.println("3. Flyway.migrate() apos limpeza executado com sucesso (sem erro de validacao)");

        try (var conn = DriverManager.getConnection(TEST_URL);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")) {
            rs.next();
            int count = rs.getInt(1);
            System.out.println("   Historico Flyway apos re-migrate: " + count);
            if (count != 17) {
                System.err.println("   FALHA: Historico corrompido apos re-migrate!");
                System.exit(1);
            }
        }

        System.out.println("\n=== TESTE PASSOU ===");

        try { new java.io.File(TEST_DB).delete(); } catch (Exception ignored) {}
    }

    private static String readResource() {
        try (var reader = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(CleanDbRunner.class.getResourceAsStream("/clean_db.sql")), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler recurso: " + "/clean_db.sql", e);
        }
    }
}

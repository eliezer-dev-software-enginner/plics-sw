package my_app.db;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public final class MigrationRunner {
    private static final String MIGRATIONS_PATH = "migrations";

    private MigrationRunner() {}

    public static void run() {
        try {
            Connection conn = DB.getInstance().connection();
            criarTabelaMigrations(conn);
            List<String> arquivos = listarArquivos();
            for (String arquivo : arquivos) {
                aplicar(conn, arquivo);
            }
        } catch (SQLException | IOException | URISyntaxException e) {
            throw new RuntimeException("Erro ao executar migrations", e);
        }
    }

    private static void criarTabelaMigrations(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS migrations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL UNIQUE,
                    aplicada_em INTEGER NOT NULL
                )
            """);
        }
    }

    private static List<String> listarArquivos() throws IOException, URISyntaxException {
        URL url = MigrationRunner.class.getClassLoader().getResource(MIGRATIONS_PATH);
        if (url == null) throw new RuntimeException("Pasta migrations não encontrada em resources");

        URI uri = url.toURI();
        Path path;

        if (uri.getScheme().equals("jar")) {
            FileSystem fs = FileSystems.newFileSystem(uri, java.util.Collections.emptyMap());
            path = fs.getPath(MIGRATIONS_PATH);
        } else {
            path = Paths.get(uri);
        }

        try (var stream = Files.list(path)) {
            return stream
                    .map(p -> p.getFileName().toString())
                    .filter(nome -> nome.endsWith(".sql"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private static void aplicar(Connection conn, String arquivo) throws SQLException, IOException {
        if (jaAplicada(conn, arquivo)) return;

        String sql = lerArquivo(arquivo);
        boolean transacional = arquivo.endsWith("_tx.sql");

        if (transacional) {
            conn.setAutoCommit(false);
            try {
                executar(conn, sql);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } else {
            executar(conn, sql);
        }

        registrar(conn, arquivo);
    }

    private static void executar(Connection conn, String sql) throws SQLException {
        String[] statements = sql.split(";");
        try (Statement st = conn.createStatement()) {
            for (String stmt : statements) {
                String trimmed = stmt.strip();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        }
    }

    private static boolean jaAplicada(Connection conn, String nome) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM migrations WHERE nome = ?")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static void registrar(Connection conn, String nome) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO migrations (nome, aplicada_em) VALUES (?, ?)")) {
            ps.setString(1, nome);
            ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    private static String lerArquivo(String arquivo) throws IOException {
        String caminho = MIGRATIONS_PATH + "/" + arquivo;
        try (var stream = MigrationRunner.class.getClassLoader().getResourceAsStream(caminho)) {
            if (stream == null) throw new RuntimeException("Migration não encontrada: " + caminho);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
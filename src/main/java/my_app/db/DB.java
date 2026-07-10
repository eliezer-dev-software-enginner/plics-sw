package my_app.db;

import net.sf.persism.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DB {

    private static final Logger log = LoggerFactory.getLogger(DB.class);
    private static final List<Session> activeSessions = new ArrayList<>();

    private final String url;

    public DB(String url) {
        this.url = url;
    }

    public static DB production() {
        return new DB("jdbc:sqlite:" + resolveDbPath());
    }

    public Connection connection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log.error("Driver SQLite não encontrado", e);
            throw new SQLException("SQLite driver não encontrado", e);
        }

        log.info("Conectando ao banco: {}", url);
        return DriverManager.getConnection(url);
    }

    public String url() {
        return url;
    }

    public static String resolveDbPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String base;

        if (os.contains("win")) {
            base = System.getenv("APPDATA") + File.separator + "plics-sw";
        } else {
            base = System.getProperty("user.home") + File.separator + ".plics-sw";
        }

        new File(base).mkdirs();

        return base + File.separator + "erp.db";
    }

    public static Session getPersismSession() throws SQLException {
        var session = new Session(production().connection());
        register(session);
        return session;
    }

    public static void register(Session session) {
        synchronized (activeSessions) {
            activeSessions.add(session);
        }
    }

    public static void unregister(Session session) {
        synchronized (activeSessions) {
            activeSessions.remove(session);
        }
    }

    public static void closeAllSessions() {
        synchronized (activeSessions) {
            var copy = new ArrayList<>(activeSessions);
            activeSessions.clear();
            for (var s : copy) {
                try { s.close(); } catch (Exception ignored) {}
            }
        }
    }
}
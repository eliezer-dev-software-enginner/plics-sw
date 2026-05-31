package my_app.db;

import net.sf.persism.Session;
import org.flywaydb.core.Flyway;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {

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
            throw new SQLException("SQLite driver não encontrado", e);
        }

        return DriverManager.getConnection(url);
    }

    public String url() {
        return url;
    }

    private static String resolveDbPath() {
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
        var db = DB.production();
        Flyway.configure()
                .dataSource(db.url(),"","")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();

        return new Session(db.connection());
    }
}
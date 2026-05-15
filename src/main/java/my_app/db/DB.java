package my_app.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {

    private static volatile DB instance;
    private Connection conn;

//    private DB(String url) throws SQLException {
//        this.conn = DriverManager.getConnection(url);
//    }

//    public static DB getInstance() throws SQLException {
//        return getInstance("jdbc:sqlite:erp.db");
//    }

    private DB(String url) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); // força o registro do driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver não encontrado", e);
        }
        this.conn = DriverManager.getConnection(url);
    }

    public static DB getInstance() throws SQLException {
        return getInstance("jdbc:sqlite:" + resolveDbPath());
    }

//    public static DB getInstance() throws SQLException {
//        return getInstance(resolveDbPath());
//    }

    //No Windows salva em AppData\Roaming\plics-sw\erp.db, no Linux em ~/.plics-sw/erp.db.
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

    public static DB getInstance(String url) throws SQLException {
        if (instance == null) {
            synchronized (DB.class) {
                if (instance == null) {
                    instance = new DB(url);
                }
            }
        }
        return instance;
    }

    public Connection connection() {
        return conn;
    }

    // usado só em testes
    public static void reset() {
        instance = null;
    }
}

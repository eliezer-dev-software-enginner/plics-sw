package my_app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {

    private static volatile DB instance;
    private Connection conn;

    private DB(String url) throws SQLException {
        this.conn = DriverManager.getConnection(url);
    }

    public static DB getInstance() throws SQLException {
        return getInstance("jdbc:sqlite:erp.db");
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

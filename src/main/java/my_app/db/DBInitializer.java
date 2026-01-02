package my_app.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBInitializer {

    private DBInitializer() {}

    public static void init() {
        try {
            Connection conn = DB.getInstance().connection();
            try (Statement st = conn.createStatement()) {

                st.execute("""
                    CREATE TABLE IF NOT EXISTS produto (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        codigo_barras TEXT UNIQUE NOT NULL,
                        descricao TEXT,
                        preco_compra REAL,
                        preco_venda REAL,
                        margem REAL,
                        lucro REAL,
                        unidade TEXT,
                        categoria TEXT,
                        fornecedor TEXT,
                        estoque INTEGER,
                        observacoes TEXT,
                        imagem TEXT
                    )
                """);

            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco", e);
        }
    }
}

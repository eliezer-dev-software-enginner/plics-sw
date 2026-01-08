package my_app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//schemas
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
                        unidade TEXT,
                        categoria_id INTEGER,
                        fornecedor_id INTEGER,
                        estoque INTEGER,
                        observacoes TEXT,
                        imagem TEXT,
                        data_criacao INTEGER,
                        FOREIGN KEY (categoria_id) REFERENCES categoria(id)
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS categoria (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL UNIQUE,
                        data_criacao INTEGER NOT NULL
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS licensas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        valor TEXT NOT NULL UNIQUE,
                        data_criacao INTEGER NOT NULL
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS fornecedor (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL UNIQUE,
                        cpfCnpj TEXT,
                        data_criacao INTEGER NOT NULL
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        senha TEXT NOT NULL,
                        cargo TEXT NOT NULL,
                        data_criacao INTEGER NOT NULL
                    )
                """);

            }
            
            // Inserir dados padrão na primeira execução
            inserirDadosPadrao();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco", e);
        }
    }
    
    private static void inserirDadosPadrao() throws SQLException {
        Connection conn = DB.getInstance().connection();
        
        // Verificar se já existe categoria padrão
        if (!existeCategoriaPadrao(conn)) {
            inserirCategoriaPadrao(conn);
        }
        
        // Verificar se já existe fornecedor padrão
        if (!existeFornecedorPadrao(conn)) {
            inserirFornecedorPadrao(conn);
        }

        if (!existeLoginPadrao(conn)) {
            inserirLoginPadrao(conn);
        }
    }

    private static boolean existeLoginPadrao(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private static boolean existeCategoriaPadrao(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Geral");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private static boolean existeFornecedorPadrao(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fornecedor WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Fornecedor Padrão");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private static void inserirCategoriaPadrao(Connection conn) throws SQLException {
        String sql = "INSERT INTO categoria (nome, data_criacao) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Geral");
            ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }
    
    private static void inserirFornecedorPadrao(Connection conn) throws SQLException {
        String sql = "INSERT INTO fornecedor (nome, data_criacao) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Fornecedor Padrão");
            ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    private static void inserirLoginPadrao(Connection conn) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, senha, cargo, data_criacao) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, "1234");
            ps.setString(3, "admin");
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }
}

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
                    CREATE TABLE IF NOT EXISTS produtos (
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
                        marca TEXT,
                        validade TEXT,
                        comissao TEXT,
                        garantia TEXT,
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
                    CREATE TABLE IF NOT EXISTS fornecedores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL UNIQUE,
                        cpf_cnpj TEXT,
                        celular TEXT,
                        email TEXT,
                        inscricao_estadual TEXT,
                        uf_selected TEXT,
                        cidade TEXT,
                        bairro TEXT,
                        rua TEXT,
                        numero TEXT,
                        observacao TEXT,
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

                st.execute("""
                    CREATE TABLE IF NOT EXISTS clientes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        cpf_cnpj TEXT,
                        celular TEXT,
                        email TEXT,
                        data_criacao INTEGER NOT NULL
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS compras (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        produto_cod TEXT NOT NULL,
                        fornecedor_id INTEGER NOT NULL,
                        quantidade REAL NOT NULL,
                        preco_compra TEXT,
                        desconto_em_reais TEXT,
                        tipo_pagamento TEXT,
                        observacao TEXT,
                        data_compra TEXT,
                        numero_nota TEXT,
                        data_validade TEXT,
                        quantidade_anterior REAL DEFAULT 0,
                        estoque_apos_compra REAL DEFAULT 0,
                        refletir_estoque TEXT DEFAULT 'Não',
                        data_criacao INTEGER NOT NULL,
                        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id)
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS empresas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT,
                        cpfCnpj TEXT,
                        celular TEXT,
                        endereco_cep TEXT,
                        endereco_cidade TEXT,
                        endereco_rua TEXT,
                        endereco_bairro TEXT,
                        local_pagamento TEXT,
                        texto_responsabilidade TEXT,
                        texto_termo_de_servico TEXT,
                        logomarca TEXT,
                        data_criacao INTEGER NOT NULL
                    )
                """);

            }
            
            // Adicionar colunas novas se não existirem (migração)
            adicionarColunasNovas(conn);
            
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

        if(!existeEmpresaPadrao(conn)){
            inserirEmpresaPadrao(conn);
        }

        if(!existeClientePadrao(conn)){
            inserirClientePadrao(conn);
        }
    }
    
    private static void adicionarColunasNovas(Connection conn) throws SQLException {
        // Adicionar colunas de estoque na tabela compras se não existirem
        try (Statement st = conn.createStatement()) {
            // Verificar se as colunas já existem e adicionar se necessário
            try {
                st.execute("ALTER TABLE compras ADD COLUMN quantidade_anterior REAL DEFAULT 0");
            } catch (SQLException e) {
                // Coluna já existe, ignorar
            }
            
            try {
                st.execute("ALTER TABLE compras ADD COLUMN estoque_apos_compra REAL DEFAULT 0");
            } catch (SQLException e) {
                // Coluna já existe, ignorar
            }
            
            try {
                st.execute("ALTER TABLE compras ADD COLUMN refletir_estoque TEXT DEFAULT 'Não'");
            } catch (SQLException e) {
                // Coluna já existe, ignorar
            }
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
        String sql = "SELECT COUNT(*) FROM fornecedores WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Fornecedor Padrão");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean existeEmpresaPadrao(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empresas WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean existeClientePadrao(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static void inserirEmpresaPadrao(Connection conn) throws SQLException {
        String sql = "INSERT INTO empresas (texto_responsabilidade, data_criacao) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, """
                    APÓS O VENCIMENTO COBRAR MULTA DE ATRASO 2,00
                    NÃO RECEBER ATRASADO
                    JUROS DE 0,01 AO DIA.""");
            ps.setLong(2, System.currentTimeMillis());
           ps.executeUpdate();
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
        String sql = "INSERT INTO fornecedores (nome, data_criacao) VALUES (?, ?)";
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

    private static void inserirClientePadrao(Connection conn) throws SQLException {
        String sql = "INSERT INTO clientes (nome, cpf_cnpj, celular, email, data_criacao) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "CLIENTE PADRÃO");
            ps.setString(2, "");
            ps.setString(3, "");
            ps.setString(4, "");
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }
}

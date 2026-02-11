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
                        data_validade REAL,
                        total_liquido REAL NOT NULL,
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

                st.execute("""
                    CREATE TABLE IF NOT EXISTS contas_pagar (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        descricao TEXT,
                        valor_original REAL,
                        valor_pago REAL DEFAULT 0,
                        valor_restante REAL,
                        data_vencimento INTEGER,
                        data_pagamento INTEGER,
                        status TEXT DEFAULT 'PENDENTE',
                        fornecedor_id INTEGER,
                        compra_id INTEGER,
                        numero_documento TEXT,
                        tipo_documento TEXT,
                        observacao TEXT,
                        data_criacao INTEGER NOT NULL,
                        data_validade TEXT,
                        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),
                        FOREIGN KEY (compra_id) REFERENCES compras(id)
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS vendas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        cliente_id INTEGER NOT NULL,
                        produto_cod TEXT NOT NULL,
                        quantidade REAL NOT NULL,
                        preco_unitario REAL NOT NULL,
                        total_liquido REAL NOT NULL,
                        desconto REAL DEFAULT 0,
                        tipo_pagamento TEXT,
                        observacao TEXT,
                        data_criacao INTEGER NOT NULL,
                        data_venda REAL,
                        data_validade REAL,
                        numero_nota TEXT,
                        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS contas_a_receber (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        cliente_id INTEGER,
                        venda_id INTEGER,
                        descricao TEXT,
                        valor_original REAL,
                        valor_recebido REAL DEFAULT 0,
                        valor_restante REAL,
                        data_vencimento INTEGER,
                        data_recebimento INTEGER,
                        status TEXT DEFAULT 'PENDENTE',
                        numero_documento TEXT,
                        tipo_documento TEXT,
                        observacao TEXT,
                        data_criacao INTEGER NOT NULL,
                        FOREIGN KEY (cliente_id) REFERENCES clientes(id),
                        FOREIGN KEY (venda_id) REFERENCES vendas(id)
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS tecnicos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL UNIQUE,
                        data_criacao INTEGER NOT NULL
                    )
                """);

                st.execute("""
                    CREATE TABLE IF NOT EXISTS ordens_de_servico (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        cliente_id INTEGER,
                        tecnico_id INTEGER,
                        numero_os REAL UNIQUE NOT NULL,
                        equipamento TEXT,
                        mao_de_obra_valor REAL,
                        pecas_valor REAL,
                        tipo_pagamento TEXT,
                        status TEXT,
                        checklist_relatorio TEXT,
                        data_escolhida TEXT,
                        total_liquido REAL NOT NULL,
                        data_criacao INTEGER NOT NULL,
                        FOREIGN KEY (cliente_id) REFERENCES clientes(id),
                        FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id)
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

        if(!existeEmpresaPadrao(conn)){
            inserirEmpresaPadrao(conn);
        }

        if(!existeClientePadrao(conn)){
            inserirClientePadrao(conn);
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

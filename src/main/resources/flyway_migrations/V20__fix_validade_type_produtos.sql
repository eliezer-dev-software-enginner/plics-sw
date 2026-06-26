ALTER TABLE produtos RENAME TO produtos_old;

CREATE TABLE produtos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_barras TEXT UNIQUE NOT NULL,
    descricao TEXT,
    preco_compra REAL,
    preco_venda REAL,
    unidade TEXT,
    categoria_id INTEGER,
    fornecedor_id INTEGER,
    estoque REAL,
    observacoes TEXT,
    imagem TEXT,
    marca TEXT,
    validade REAL,
    comissao TEXT,
    garantia TEXT,
    dataCriacao TIMESTAMP,
    total_liquido REAL NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

INSERT INTO produtos SELECT * FROM produtos_old;

DROP TABLE produtos_old

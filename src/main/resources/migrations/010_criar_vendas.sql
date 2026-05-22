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

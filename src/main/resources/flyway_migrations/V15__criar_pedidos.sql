CREATE TABLE IF NOT EXISTS pedidos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id INTEGER,
    forma_pagamento TEXT NOT NULL,
    total_liquido REAL NOT NULL,
    desconto REAL DEFAULT 0,
    observacao TEXT,
    is_fiado INTEGER DEFAULT 0,
    dataCriacao TIMESTAMP NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS pedido_itens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pedido_id INTEGER NOT NULL,
    produto_cod TEXT NOT NULL,
    quantidade REAL NOT NULL,
    preco_unitario REAL NOT NULL,
    desconto REAL DEFAULT 0,
    total_item REAL NOT NULL,
    dataCriacao TIMESTAMP NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
)

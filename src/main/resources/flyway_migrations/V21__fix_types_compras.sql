ALTER TABLE compras RENAME TO compras_old;

CREATE TABLE compras (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    produto_cod TEXT NOT NULL,
    fornecedor_id INTEGER NOT NULL,
    quantidade REAL NOT NULL,
    preco_compra REAL,
    desconto_em_reais REAL,
    tipo_pagamento TEXT,
    observacao TEXT,
    data_compra REAL,
    numero_nota TEXT,
    data_validade REAL,
    total_liquido REAL NOT NULL,
    dataCriacao REAL NOT NULL,
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id)
);

INSERT INTO compras SELECT * FROM compras_old;

DROP TABLE compras_old;

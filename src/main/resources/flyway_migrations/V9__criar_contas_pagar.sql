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
    dataCriacao INTEGER NOT NULL,
    data_validade TEXT,
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),
    FOREIGN KEY (compra_id) REFERENCES compras(id)
)

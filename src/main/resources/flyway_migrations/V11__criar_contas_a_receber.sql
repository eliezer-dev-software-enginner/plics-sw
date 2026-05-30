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
    data_criacao_millis INTEGER NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (venda_id) REFERENCES vendas(id)
)

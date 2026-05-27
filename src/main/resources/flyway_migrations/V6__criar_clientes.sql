CREATE TABLE IF NOT EXISTS clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    cpf_cnpj TEXT,
    celular TEXT,
    email TEXT,
    data_criacao_millis INTEGER NOT NULL
)

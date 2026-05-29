CREATE TABLE IF NOT EXISTS clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    cpfCnpj TEXT,
    celular TEXT,
    email TEXT,
    dataCriacao TIMESTAMP NOT NULL,
    isPessoaFisica BIT
)

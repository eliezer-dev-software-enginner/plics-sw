CREATE TABLE IF NOT EXISTS fornecedores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    cpfCnpj TEXT,
    celular TEXT,
    email TEXT,
    inscricao_estadual TEXT,
    uf_selected TEXT,
    cidade TEXT,
    bairro TEXT,
    rua TEXT,
    numero TEXT,
    observacao TEXT,
    dataCriacao INTEGER NOT NULL
)

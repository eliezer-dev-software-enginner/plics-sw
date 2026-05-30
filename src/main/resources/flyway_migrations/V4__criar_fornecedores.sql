CREATE TABLE IF NOT EXISTS fornecedores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
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
    data_criacao_millis INTEGER NOT NULL
)

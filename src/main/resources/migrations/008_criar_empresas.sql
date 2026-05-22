CREATE TABLE IF NOT EXISTS empresas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT,
    cpfCnpj TEXT,
    celular TEXT,
    endereco_cep TEXT,
    endereco_cidade TEXT,
    endereco_rua TEXT,
    endereco_bairro TEXT,
    local_pagamento TEXT,
    texto_responsabilidade TEXT,
    texto_termo_de_servico TEXT,
    logomarca TEXT,
    data_criacao INTEGER NOT NULL
)

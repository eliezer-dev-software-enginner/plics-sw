CREATE TABLE IF NOT EXISTS ordens_de_servico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id INTEGER,
    tecnico_id INTEGER,
    numero_os REAL UNIQUE NOT NULL,
    equipamento TEXT,
    mao_de_obra_valor REAL,
    pecas_valor REAL,
    tipo_pagamento TEXT,
    status TEXT,
    checklist_relatorio TEXT,
    data_escolhida TEXT,
    total_liquido REAL NOT NULL,
    dataCriacao REAL NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id)
)

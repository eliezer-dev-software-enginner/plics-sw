-- Ids das models passaram de Integer para Long.
-- O driver Xerial retorna Integer (não Long) para colunas INTEGER PRIMARY KEY (rowid),
-- o que quebrava a leitura nas properties Long. Por isso PKs e FKs passam a BIGINT.
-- No SQLite, BIGINT PRIMARY KEY não é auto-increment; os ids são gerados no app
-- (BaseRepository.salvar) via SELECT id ORDER BY id DESC LIMIT 1 + 1.
-- Tecnicos e ordens_de_servico foram removidos do app e são dropados.

ALTER TABLE categorias RENAME TO categorias_old;

CREATE TABLE categorias (
    id BIGINT PRIMARY KEY,
    nome TEXT NOT NULL UNIQUE,
    data_criacao TIMESTAMP NOT NULL
);

INSERT INTO categorias SELECT * FROM categorias_old;
DROP TABLE categorias_old;

ALTER TABLE fornecedores RENAME TO fornecedores_old;

CREATE TABLE fornecedores (
    id BIGINT PRIMARY KEY,
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
    dataCriacao TIMESTAMP NOT NULL,
    cep TEXT,
    isPessoaFisica BIT
);

INSERT INTO fornecedores SELECT * FROM fornecedores_old;
DROP TABLE fornecedores_old;

CREATE UNIQUE INDEX IF NOT EXISTS idx_fornecedores_cpfCnpj_unique
ON fornecedores(cpfCnpj) WHERE cpfCnpj IS NOT NULL AND cpfCnpj != '';

ALTER TABLE clientes RENAME TO clientes_old;

CREATE TABLE clientes (
    id BIGINT PRIMARY KEY,
    nome TEXT NOT NULL,
    cpfCnpj TEXT,
    celular TEXT,
    email TEXT,
    dataCriacao TIMESTAMP NOT NULL,
    isPessoaFisica BIT,
    cep TEXT,
    uf TEXT,
    cidade TEXT,
    bairro TEXT,
    rua TEXT,
    numero TEXT,
    is_gestante INTEGER DEFAULT 0,
    data_nascimento_bebe REAL,
    data_nascimento REAL,
    observacao TEXT
);

INSERT INTO clientes SELECT * FROM clientes_old;
DROP TABLE clientes_old;

CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_cpfCnpj_unique
ON clientes(cpfCnpj) WHERE cpfCnpj IS NOT NULL AND cpfCnpj != '';

ALTER TABLE empresas RENAME TO empresas_old;

CREATE TABLE empresas (
    id BIGINT PRIMARY KEY,
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
    dataCriacao TIMESTAMP NOT NULL
);

INSERT INTO empresas SELECT * FROM empresas_old;
DROP TABLE empresas_old;

ALTER TABLE cores RENAME TO cores_old;

CREATE TABLE cores (
    id BIGINT PRIMARY KEY,
    nome TEXT NOT NULL UNIQUE,
    dataCriacao TIMESTAMP
);

INSERT INTO cores SELECT * FROM cores_old;
DROP TABLE cores_old;

ALTER TABLE preferencias RENAME TO preferencias_old;

CREATE TABLE preferencias (
    id BIGINT PRIMARY KEY,
    credenciais_habilitadas INTEGER NOT NULL,
    tema TEXT NOT NULL,
    login TEXT,
    senha TEXT,
    primeiro_acesso INTEGER NOT NULL DEFAULT 1,
    dataCriacao REAL NOT NULL,
    licensa TEXT,
    porta_impressora TEXT,
    popup_instagram_habilitado INTEGER NOT NULL DEFAULT 1
);

INSERT INTO preferencias SELECT * FROM preferencias_old;
DROP TABLE preferencias_old;

ALTER TABLE produtos RENAME TO produtos_old;

CREATE TABLE produtos (
    id BIGINT PRIMARY KEY,
    codigo_barras TEXT UNIQUE NOT NULL,
    descricao TEXT,
    preco_compra REAL,
    preco_venda REAL,
    unidade TEXT,
    categoria_id BIGINT,
    fornecedor_id BIGINT,
    estoque REAL,
    observacoes TEXT,
    imagem TEXT,
    marca TEXT,
    validade REAL,
    garantia TEXT,
    dataCriacao TIMESTAMP,
    total_liquido REAL NOT NULL,
    cor TEXT,
    tamanho TEXT,
    modelo TEXT,
    estoque_minimo REAL DEFAULT 0,
    frete REAL DEFAULT 0,
    aceita_devolucao BIT DEFAULT 0,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

INSERT INTO produtos SELECT * FROM produtos_old;
DROP TABLE produtos_old;

ALTER TABLE compras RENAME TO compras_old;

CREATE TABLE compras (
    id BIGINT PRIMARY KEY,
    produto_cod TEXT NOT NULL,
    fornecedor_id BIGINT NOT NULL,
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

ALTER TABLE vendas RENAME TO vendas_old;

CREATE TABLE vendas (
    id BIGINT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    produto_cod TEXT NOT NULL,
    quantidade REAL NOT NULL,
    preco_unitario REAL NOT NULL,
    total_liquido REAL NOT NULL,
    desconto REAL DEFAULT 0,
    tipo_pagamento TEXT,
    observacao TEXT,
    dataCriacao TIMESTAMP NOT NULL,
    data_venda REAL,
    data_validade REAL,
    numero_nota TEXT,
    afeta_estoque INTEGER NOT NULL DEFAULT 1,
    frete REAL DEFAULT 0,
    devolvida BIT DEFAULT 0,
    data_devolucao REAL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

INSERT INTO vendas SELECT * FROM vendas_old;
DROP TABLE vendas_old;

ALTER TABLE pedidos RENAME TO pedidos_old;

CREATE TABLE pedidos (
    id BIGINT PRIMARY KEY,
    cliente_id BIGINT,
    forma_pagamento TEXT NOT NULL,
    total_liquido REAL NOT NULL,
    desconto REAL DEFAULT 0,
    observacao TEXT,
    is_fiado INTEGER DEFAULT 0,
    dataCriacao TIMESTAMP NOT NULL,
    frete REAL DEFAULT 0,
    devolvida BIT DEFAULT 0,
    data_devolucao REAL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

INSERT INTO pedidos SELECT * FROM pedidos_old;
DROP TABLE pedidos_old;

ALTER TABLE contas_a_receber RENAME TO contas_a_receber_old;

CREATE TABLE contas_a_receber (
    id BIGINT PRIMARY KEY,
    cliente_id BIGINT,
    venda_id BIGINT,
    descricao TEXT,
    valor_original REAL,
    valor_recebido REAL DEFAULT 0,
    valor_restante REAL,
    data_vencimento REAL,
    data_recebimento REAL,
    status TEXT DEFAULT 'PENDENTE',
    numero_documento TEXT,
    tipo_documento TEXT,
    observacao TEXT,
    dataCriacao TIMESTAMP NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (venda_id) REFERENCES vendas(id)
);

INSERT INTO contas_a_receber SELECT * FROM contas_a_receber_old;
DROP TABLE contas_a_receber_old;

ALTER TABLE contas_pagar RENAME TO contas_pagar_old;

CREATE TABLE contas_pagar (
    id BIGINT PRIMARY KEY,
    descricao TEXT,
    valor_original REAL,
    valor_pago REAL DEFAULT 0,
    valor_restante REAL,
    data_vencimento REAL,
    data_pagamento REAL,
    status TEXT DEFAULT 'PENDENTE',
    fornecedor_id BIGINT,
    compra_id BIGINT,
    numero_documento TEXT,
    tipo_documento TEXT,
    observacao TEXT,
    dataCriacao TIMESTAMP NOT NULL,
    data_validade TEXT,
    FOREIGN KEY (fornecedor_id) REFERENCES fornecedores(id),
    FOREIGN KEY (compra_id) REFERENCES compras(id)
);

INSERT INTO contas_pagar SELECT * FROM contas_pagar_old;
DROP TABLE contas_pagar_old;

ALTER TABLE pedido_itens RENAME TO pedido_itens_old;

CREATE TABLE pedido_itens (
    id BIGINT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_cod TEXT NOT NULL,
    quantidade REAL NOT NULL,
    preco_unitario REAL NOT NULL,
    desconto REAL DEFAULT 0,
    total_item REAL NOT NULL,
    dataCriacao TIMESTAMP NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

INSERT INTO pedido_itens SELECT * FROM pedido_itens_old;
DROP TABLE pedido_itens_old;

DROP TABLE ordens_de_servico;
DROP TABLE tecnicos;
CREATE TABLE IF NOT EXISTS cores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE,
    dataCriacao TIMESTAMP
);

INSERT INTO cores (nome, dataCriacao)
SELECT 'Azul', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Azul');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Branco', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Branco');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Preto', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Preto');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Vermelho', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Vermelho');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Verde', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Verde');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Amarelo', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Amarelo');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Rosa', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Rosa');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Laranja', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Laranja');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Roxo', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Roxo');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Cinza', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Cinza');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Marrom', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Marrom');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Bege', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Bege');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Dourado', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Dourado');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Prateado', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Prateado');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Colorido', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Colorido');

INSERT INTO cores (nome, dataCriacao)
SELECT 'Estampado', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM cores WHERE nome = 'Estampado');

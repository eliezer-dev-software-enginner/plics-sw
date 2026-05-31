INSERT INTO preferencias (tema, credenciais_habilitadas, primeiro_acesso, dataCriacao, login, senha)
SELECT 'Claro', 0, 1, strftime('%s', 'now') * 1000, 'admin', '1234'
WHERE NOT EXISTS (SELECT 1 FROM preferencias WHERE id = 1);

INSERT INTO categorias (nome, data_criacao)
SELECT 'Geral', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nome = 'Geral');

INSERT INTO fornecedores (nome, dataCriacao)
SELECT 'Fornecedor Padrão', strftime('%s', 'now') * 1000
WHERE NOT EXISTS (SELECT 1 FROM fornecedores WHERE nome = 'Fornecedor Padrão');

INSERT INTO usuarios (nome, senha, cargo, dataCriacao)
SELECT 'admin', '1234', 'admin', strftime('%s', 'now') * 1000
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE nome = 'admin');

INSERT INTO empresas (texto_responsabilidade, dataCriacao)
SELECT 'APÓS O VENCIMENTO COBRAR MULTA DE ATRASO 2,00
NÃO RECEBER ATRASADO
JUROS DE 0,01 AO DIA.', strftime('%s', 'now') * 1000
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE id = 1);

INSERT INTO clientes (nome, cpfCnpj, celular, email, dataCriacao,isPessoaFisica)
SELECT 'CLIENTE PADRÃO', '', '', '', strftime('%s', 'now') * 1000, 1
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id = 1)

ALTER TABLE vendas ADD COLUMN devolvida BIT DEFAULT 0;
ALTER TABLE vendas ADD COLUMN data_devolucao REAL;
ALTER TABLE pedidos ADD COLUMN devolvida BIT DEFAULT 0;
ALTER TABLE pedidos ADD COLUMN data_devolucao REAL;

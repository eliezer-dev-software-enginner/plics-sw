CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_cpfCnpj_unique
ON clientes(cpfCnpj) WHERE cpfCnpj IS NOT NULL AND cpfCnpj != '';

CREATE UNIQUE INDEX IF NOT EXISTS idx_fornecedores_cpfCnpj_unique
ON fornecedores(cpfCnpj) WHERE cpfCnpj IS NOT NULL AND cpfCnpj != '';

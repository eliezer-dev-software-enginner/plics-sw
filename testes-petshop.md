# Testes — Perfil PetShop "PetShop Amigo"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | PetShop Amigo | |
| CNPJ | 98.765.432/0001-10 | |
| Celular | (21) 99876-5432 | |
| Email | contato@petshopamigo.com.br | |
| Cidade | Rio de Janeiro - RJ | |
| Produtos típicos | Ração cães 15kg, Ração gatos 10kg, Areia sanitária, Brinquedos | |
| Categorias | Cachorros, Gatos, Aves, Peixes | |
| Fornecedores | Purina Pet, Royal Canin, PetBrasil | |
| Clientes | Ana Beatriz, Carlos Mendes, Sofia Rocha | |

---

## ClienteScreen

| # | Cenário | Nome | Tipo Pessoa | CPF/CNPJ | Celular | Email | Efeito Esperado | Erro/Inconsistência | Resultado |
|---|---------|------|-------------|----------|---------|-------|-----------------|---------------------|---|
| 10 | Cadastro válido - Pessoa Jurídica | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 99876-5432 | contato@petshopamigo.com.br | Salvo com sucesso. | | |
| 11 | Cadastro válido - Pessoa Física | Ana Beatriz | Fisica | 222.333.444-55 | (21) 99777-6666 | ana@email.com | Salvo com sucesso. | | |
| 12 | CNPJ duplicado | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 91234-5678 | outro@email.com | Alerta: "CNPJ já cadastrado". | | |

---

## CategoriaScreen

| # | Cenário | Nome | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------------|------|---|
| 31 | Cadastro válido - PetShop | Cachorros | Salvo com sucesso. | | |

---

## FornecedorScreen

| # | Cenário | Nome Fantasia | CNPJ | Celular | Email | Cidade | Efeito Esperado | Erro | Resultado |
|---|---------|--------------|------|---------|-------|--------|-----------------|------|---|
| 44 | Cadastro válido | Purina Pet | 44.444.444/0001-44 | (21) 95555-4444 | purina@pet.com | Rio de Janeiro | Salvo com sucesso. | | |
| 45 | Email inválido | Royal Canin | 55.555.555/0001-55 | (21) 94444-3333 | email-errado | Rio de Janeiro | Alerta: "Email inválido". | | |

---

## ComprasAPagarScreen

| # | Cenário | Descrição | Valor | Fornecedor | Vencimento | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|-------|-----------|------------|-----------------|------|---|
| 69 | Conta gerada por compra | Compra Purina Pet | R$ 1.500,00 | Purina Pet | 15/08/2026 | Gerada automaticamente pela compra a prazo. | | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Efeito Esperado | Erro | Resultado |
|---|---------|------|--------|-----------------|------|---|
| 95 | Cadastro PetShop | PetShop Amigo | Rio de Janeiro | Salvo com sucesso. | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
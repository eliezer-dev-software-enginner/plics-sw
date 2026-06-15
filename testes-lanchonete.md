# Testes — Perfil Lanchonete "Sabor & Cia"

## Perfil do Negócio

| Campo | Valor |
|-------|-------|
| Nome fantasia | Sabor & Cia Lanches |
| CNPJ | 11.222.333/0001-44 |
| Celular | (31) 99988-7766 |
| Email | saborecia@email.com.br |
| Cidade | Belo Horizonte - MG |
| Produtos típicos | Pão de queijo, Coxinha, Refrigerante, Suco natural |
| Categorias | Salgados, Bebidas, Doces, Porções |
| Fornecedores | Distribuidora Horizonte, Laticínios MG, RefriMax |
| Clientes | Pedro Alves, Luana Costa, Thiago Santos |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Efeito Esperado | Erro |
|---|---------|-----|-----------|-------|-------------|-------------|-----------|-----------|---------|-----------------|------|
| 21 | Produto unitário | BEB001 | Refrigerante Lata 350ml | UN | R$ 2,50 | R$ 5,00 | Bebidas | RefriMax | 200 | Salvo com sucesso. | |
| 22 | Produto por kg | SAL001 | Coxinha (kg) | KG | R$ 12,00 | R$ 29,90 | Salgados | Distribuidora Horizonte | 10 | Salvo com sucesso. | |
| 23 | Produto ml | BEB002 | Suco Natural 500ml | ml | R$ 3,00 | R$ 7,00 | Bebidas | Distribuidora Horizonte | 30 | Salvo com sucesso. | |
| 24 | Fornecedor não selecionado | DOC001 | Pudim | UN | R$ 4,00 | R$ 9,00 | Doces | (nenhum) | 20 | Alerta: "Fornecedor é obrigatório". | |

---

## CategoriaScreen

| # | Cenário | Nome | Efeito Esperado | Erro |
|---|---------|------|-----------------|------|
| 32 | Cadastro válido - Lanchonete | Bebidas | Salvo com sucesso. | |

---

## VendaMercadoriaScreen

| # | Cenário | Produto | Qtd | Preço | Pagamento | Estoque | Efeito Esperado | Erro |
|---|---------|---------|-----|-------|-----------|---------|-----------------|------|
| 55 | Venda balcão (padrão) | Coxinha KG | 0,5 | R$ 29,90/kg | A VISTA | Sim | Venda registrada R$ 14,95. Estoque 9,5 kg. | |
| 56 | Venda múltiplos itens | Refri Lata + Coxinha | 2 + 1 | R$ 5,00 + R$ 29,90 | DEBITO | Sim | Venda registrada total R$ 39,90. | |

---

## ContasAReceberScreen

| # | Cenário | Descrição | Valor | Cliente | Status | Efeito Esperado | Erro |
|---|---------|-----------|-------|--------|--------|-----------------|------|
| 74 | Venda fiado no PDV | Venda fiado Pedro Alves | R$ 25,00 | Pedro Alves | PENDENTE | (automático) | Gera conta a receber automaticamente. | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Local Pagamento | Efeito Esperado | Erro |
|---|---------|------|--------|----------------|-----------------|------|
| 96 | Cadastro Lanchonete | Sabor & Cia Lanches | Belo Horizonte | Pagável em qualquer lotérica | Salvo com sucesso. | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada

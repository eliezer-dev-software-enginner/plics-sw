# Testes — Perfil Mercado "SuperMax"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | SuperMax Mercado | |
| CNPJ | 77.888.999/0001-55 | |
| Celular | (61) 99666-5555 | |
| Email | supermax@mercado.com.br | |
| Cidade | Brasília - DF | |
| Produtos típicos | Arroz 5kg, Feijão 1kg, Óleo 900ml, Café 500g, Leite 1L | |
| Categorias | Alimentos, Bebidas, Limpeza, Higiene | |
| Fornecedores | Alimentos Brasil, Bebidas DF, LimpMax | |
| Clientes | Fernanda Lima, Ricardo Gomes, Juliana Costa | |

---


---


---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Marca | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Perecível | Validade | Efeito Esperado | Erro | Resultado |
|---|---------|-----|-----------|-------|-------|-------------|-------------|-----------|-----------|---------|-----------|----------|-----------------|------|---|
| 145 | Cadastro válido arroz | SKU-ARROZ | Arroz 5kg | UN | Alimentos Brasil | R$ 8,00 | R$ 12,00 | Alimentos | Alimentos Brasil | 200 | Nao | - | Salvo com sucesso. | | |
| 146 | Cadastro válido feijão | SKU-FEIJAO | Feijão 1kg | UN | Alimentos Brasil | R$ 5,00 | R$ 8,00 | Alimentos | Alimentos Brasil | 150 | Nao | - | Salvo com sucesso. | | |
| 147 | Cadastro válido óleo | SKU-OLEO | Óleo 900ml | ml | Alimentos Brasil | R$ 4,50 | R$ 7,50 | Alimentos | Alimentos Brasil | 100 | Nao | - | Salvo com sucesso. | | |

---

## PDVScreen

| # | Cenário | Produto | Qtd | Total | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|-----------------|------|---|
| 90 | Venda mercado | Arroz 5kg | 2 | R$ 20,00 | Venda registrada. Estoque atualizado. | | |
| 91 | Venda múltiplos itens | Arroz + Feijão + Óleo | 1+2+1 | Total calculado | Venda registrada. | | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Telefone | Efeito Esperado | Erro | Resultado |
|---|---------|------|--------|---------|-----------------|------|---|
| 98 | Cadastro Mercado | SuperMax Mercado | Brasília | (61) 99666-5555 | Salvo com sucesso. | | |

---

## Testes de Fluxo Completo

### Fluxo 2 — Mercado: Compra a Prazo -> Pagamento

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro | Resultado |
|-------|------|------|-------|-----------------|------|---|
| 1 | Categoria | Criar "Alimentos" | Nome: Alimentos | OK | | |
| 2 | Fornecedor | Criar "Alimentos Brasil" | CNPJ: 66.666.666/0001-66, DF | OK | | |
| 3 | Produto | Criar "Arroz 5kg" | SKU-ARROZ, R$ 8,00/R$ 12,00, UN, Est: 200 | OK | | |
| 4 | Compras | Comprar 100 Arroz a prazo | Alimentos Brasil, R$ 8,00, A PRAZO (3x) | Estoque: 300. Gera 3 contas a pagar. | | |
| 5 | Contas a Pagar | Verificar | 3 parcelas geradas automaticamente | OK | | |
| 6 | Contas a Pagar | Pagar 1ª parcela | Pagamento total da 1ª | Status: PARCIAL | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
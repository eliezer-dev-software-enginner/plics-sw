# Testes — Perfil Lanchonete "Sabor & Cia"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | Sabor & Cia Lanches | |
| CNPJ | 11.222.333/0001-81 | |
| Celular | (31) 99988-7766 | |
| Email | saborecia@email.com.br | |
| Cidade | Belo Horizonte - MG | |
| Produtos típicos | Pão de queijo, Coxinha, Refrigerante, Suco natural | |
| Categorias | Salgados, Bebidas, Doces, Porções | |
| Fornecedores | Distribuidora Horizonte, Laticínios MG, RefriMax | |
| Clientes | Pedro Alves | |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Cor | Tamanho | Modelo | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Garantia | Comissão | Observações | Imagem | Efeito Esperado | Erro | Resultado |
|---|---------|-----|-----------|-------|-----|---------|--------|-------------|-------------|-----------|-----------|---------|----------|----------|-------------|--------|-----------------|------|---|
| 21 | Produto unitário | BEB001 | Refrigerante Lata 350ml | UN | - | - | - | R$ 2,50 | R$ 5,00 | Bebidas | RefriMax | 200 | - | - | Refrigerante cola 350ml lata | - | Salvo com sucesso. | | |
| 22 | Produto por kg | SAL001 | Coxinha (kg) | KG | - | - | - | R$ 12,00 | R$ 29,90 | Salgados | Distribuidora Horizonte | 10 | - | 5% | Coxinha de frango caseira | - | Salvo com sucesso. | | |
| 23 | Produto ml | BEB002 | Suco Natural 500ml | ml | - | - | - | R$ 3,00 | R$ 7,00 | Bebidas | Distribuidora Horizonte | 30 | - | - | Suco natural sem conservantes | - | Salvo com sucesso. | | |
| 24 | Fornecedor não selecionado | DOC001 | Pudim | UN | - | - | - | R$ 4,00 | R$ 9,00 | Doces | (nenhum) | 20 | - | - | - | - | Alerta: "Fornecedor é obrigatório". | | |
| 125 | Produto com imagem | BEB003 | Café Especial 250g | g | - | - | - | R$ 8,00 | R$ 18,00 | Bebidas | RefriMax | 50 | - | - | Café torrado e moído | (arquivo jpg) | Salvo com sucesso. Imagem registrada. | | |

---


---

## VendaMercadoriaScreen

| # | Cenário | Produto | Qtd | Preço | Pagamento | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|-----------|---------|-----------------|------|---|
| 55 | Venda balcão (padrão) | Coxinha KG | 0,5 | R$ 29,90/kg | A VISTA | Sim | Venda registrada R$ 14,95. Estoque 9,5 kg. | | |
| 56 | Venda múltiplos itens | Refri Lata + Coxinha | 2 + 1 | R$ 5,00 + R$ 29,90 | DEBITO | Sim | Venda registrada total R$ 39,90. | | |

---

## ContasAReceberScreen

| # | Cenário | Descrição | Valor | Cliente | Status | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|-------|--------|--------|-----------------|------|---|
| 74 | Venda fiado no PDV | Venda fiado Pedro Alves | R$ 25,00 | Pedro Alves | PENDENTE | (automático) | Gera conta a receber automaticamente. | | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Local Pagamento | Efeito Esperado | Erro | Resultado |
|---|---------|------|--------|----------------|-----------------|------|---|
| 96 | Cadastro Lanchonete | Sabor & Cia Lanches | Belo Horizonte | Pagável em qualquer lotérica | Salvo com sucesso. | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
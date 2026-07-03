# Testes — Perfil PetShop "PetShop Amigo"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | PetShop Amigo | |
| CNPJ | 98.765.432/0001-98 | |
| Celular | (21) 99876-5432 | |
| Email | contato@petshopamigo.com.br | |
| Cidade | Rio de Janeiro - RJ | |
| Produtos típicos | Ração cães 15kg, Ração gatos 10kg, Areia sanitária, Brinquedos | |
| Categorias | Cachorros, Gatos, Aves, Peixes | |
| Fornecedores | Purina Pet, Royal Canin, PetBrasil | |
| Clientes | Ana Beatriz, Carlos Mendes, Sofia Rocha | |

---


---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Marca | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Perecível | Validade | Efeito Esperado | Erro | Resultado |
|---|---------|-----|-----------|-------|-------|-------------|-------------|-----------|-----------|---------|-----------|----------|-----------------|------|---|
| 113 | Ração cães 15kg | RAC-C-001 | Ração Cães 15kg | KG | Purina Pet | R$ 45,00 | R$ 79,90 | Cachorros | Purina Pet | 20 | Sim | 15/12/2026 | Salvo com sucesso. | | |
| 114 | Ração gatos 10kg | RAC-G-001 | Ração Gatos 10kg | KG | Royal Canin | R$ 55,00 | R$ 89,90 | Gatos | Royal Canin | 15 | Sim | 20/01/2027 | Salvo com sucesso. | | |
| 115 | Areia sanitária 4kg | ARE-001 | Areia Sanitária 4kg | KG | PetBrasil | R$ 12,00 | R$ 24,90 | Gatos | PetBrasil | 30 | Nao | - | Salvo com sucesso. | | |
| 116 | Brinquedo (unidade) | BRI-001 | Brinquedo Osso Borracha | UN | PetBrasil | R$ 3,50 | R$ 9,90 | Cachorros | PetBrasil | 50 | Nao | - | Salvo com sucesso com unidade UN. | | |
| 117 | Shampoo 500ml | SHA-001 | Shampoo Cães 500ml | ml | PetBrasil | R$ 8,00 | R$ 19,90 | Cachorros | PetBrasil | 40 | Nao | - | Salvo com sucesso com unidade ml. | | |
| 118 | Petisco 100g (gramas) | PET-001 | Petisco Cães 100g | g | Purina Pet | R$ 5,00 | R$ 12,90 | Cachorros | Purina Pet | 60 | Nao | - | Salvo com sucesso com unidade "g". | | |
| 119 | Perecível sem validade | RAC-T-001 | Ração Teste | KG | Purina Pet | R$ 20,00 | R$ 39,90 | Cachorros | Purina Pet | 10 | Sim | (vazio) | Alerta: "Data de validade é obrigatória para produtos perecíveis". | | |

---


---

## VendaMercadoriaScreen

| # | Cenário | Data | Cliente | NF | Produto | Qtd | Preço unit. | Pagamento | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|----|---------|-----|------------|-----------|---------|-----------------|------|---|
| 120 | Venda ração kg inteiro | 15/06/2026 | Ana Beatriz | NF-PET-001 | Ração Cães 15kg (RAC-C-001) | 1 | R$ 79,90 | A VISTA | Sim | Venda registrada. Estoque 20 -> 19 unidades. | | |
| 121 | Venda ração fracionada (kg em gramas) | 15/06/2026 | Carlos Mendes | NF-PET-002 | Ração Cães 15kg (RAC-C-001) | 0,500 | R$ 79,90/kg | A VISTA | Sim | Venda registrada R$ 39,95 (0,5 kg). Deve converter para fração de KG. Estoque 19 -> 18,5 kg. | | |
| 122 | Venda brinquedo | 15/06/2026 | Sofia Rocha | NF-PET-003 | Brinquedo Osso Borracha (BRI-001) | 2 | R$ 9,90 | DEBITO | Sim | Venda registrada R$ 19,80. Estoque 50 -> 48. | | |
| 123 | Venda shampoo ml | 15/06/2026 | Ana Beatriz | NF-PET-004 | Shampoo Cães 500ml (SHA-001) | 1 | R$ 19,90 | PIX | Sim | Venda registrada R$ 19,90. Estoque 40 -> 39. | | |
| 124 | Venda petisco gramas | 15/06/2026 | Carlos Mendes | NF-PET-005 | Petisco Cães 100g (PET-001) | 200 | g | A VISTA | Sim | Venda registrada. Estoque 60 -> 58 unidades (cada unidade = 100g). | | |
| 125 | Venda com desconto | 15/06/2026 | Sofia Rocha | NF-PET-006 | Ração Gatos 10kg (RAC-G-001) | 1 | R$ 89,90 | CREDITO | Sim | Venda registrada com desconto de R$ 10,00. Total R$ 79,90. | | |

---

## ComprasScreen

| # | Cenário | Data | Fornecedor | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------|----|---------|-----|-------|-------|-----------|---------|-----------------|------|---|
| 126 | Compra ração cães | 14/06/2026 | Purina Pet | NF-COM-PET-001 | Ração Cães 15kg (RAC-C-001) | 10 | R$ 45,00 | R$ 0 | A VISTA | Sim | Estoque 20 -> 30. Custo registrado. | | |
| 127 | Compra ração gatos a prazo | 14/06/2026 | Royal Canin | NF-COM-PET-002 | Ração Gatos 10kg (RAC-G-001) | 8 | R$ 55,00 | R$ 0 | A PRAZO | Sim | Compra registrada. Estoque 15 -> 23. Gera 3 contas a pagar de R$ 146,67 (total R$ 440,00). | | |
| 128 | Compra areia sanitária | 14/06/2026 | PetBrasil | NF-COM-PET-003 | Areia Sanitária 4kg (ARE-001) | 20 | R$ 12,00 | R$ 20,00 | A VISTA | Sim | Compra registrada com desconto. Estoque 30 -> 50. | | |
| 129 | Compra brinquedos sem refletir estoque | 14/06/2026 | PetBrasil | - | Brinquedo Osso Borracha (BRI-001) | 25 | R$ 3,50 | R$ 0 | A VISTA | Nao | Compra registrada. Estoque permanece 50. | | |

---

## OrdemServicoScreen

| # | Cenário | Data | Cliente | Técnico | Equipamento/Serviço | Mão de Obra | Peças | Pagamento | Status | Checklist | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|---------|---------------------|-------------|-------|-----------|--------|-----------|-----------------|------|---|
| 130 | Banho simples | 15/06/2026 | Ana Beatriz | (tosador) | Banho cães pequeno porte | R$ 35,00 | R$ 0 | A VISTA | Aberto | "Banho, secagem" | Salva com total R$ 35,00. | | |
| 131 | Banho + Tosa completa | 16/06/2026 | Carlos Mendes | (tosador) | Banho + Tosa cães médio porte | R$ 55,00 | R$ 0 | PIX | Finalizado | "Tosa higiênica, banho, corte unhas" | OS finalizada. | | |
| 132 | Cliente não selecionado | 15/06/2026 | (vazio) | (tosador) | Banho | R$ 35,00 | R$ 0 | A VISTA | Aberto | - | Alerta: "Selecione um cliente". | | |
| 133 | Técnico não selecionado | 15/06/2026 | Sofia Rocha | (vazio) | Tosa | R$ 45,00 | R$ 0 | - | Aberto | - | Alerta: "Selecione um técnico". | | |

---

## TecnicoScreen

| # | Cenário | Nome | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------------|------|---|
| 134 | Cadastro tosador | Carlos Tosador | Salvo com sucesso. | | |
| 135 | Cadastro banhista | Juliana Banhista | Salvo com sucesso. | | |
| 136 | Nome duplicado | Carlos Tosador | Alerta: "Técnico já cadastrado". | | |

---

## PDVScreen

| # | Cenário | Produto | Qtd | Total | Recebido | Fiado | Cliente (fiado) | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|----------|-------|-----------------|-----------------|------|---|
| 137 | Venda avulsa ração 15kg | Ração Cães 15kg | 1 | R$ 79,90 | R$ 100,00 | Não | - | Troco: R$ 20,10. Pedido registrado. | | |
| 138 | Venda mix (ração + brinquedo) | Ração Cães + Brinquedo | 1+2 | R$ 99,70 | R$ 100,00 | Não | - | Troco: R$ 0,30. Estoque ração 30 -> 29, brinquedo 50 -> 48. | | |
| 139 | Venda fiada para cliente fiel | Ração Gatos 10kg | 1 | R$ 89,90 | - | Sim | Sofia Rocha | Pedido fiado registrado. Gera conta a receber. | | |
| 140 | Carrinho vazio | (nenhum) | 0 | R$ 0 | - | - | - | Alerta: "Adicione produtos ao carrinho". | | |

---

## ContasAReceberScreen

| # | Cenário | Descrição | Valor Original | Cliente | Status | Vencimento | Tipo Doc | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|--------------|--------|--------|------------|----------|-----------------|------|---|
| 141 | Conta gerada por venda fiada | Venda fiada Sofia Rocha (Ração Gatos) | R$ 89,90 | Sofia Rocha | PENDENTE | 15/07/2026 | - | Gerada automaticamente. | | |
| 142 | Recebimento total | (conta existente) | Restante integral | Sofia Rocha | RECEBIDO | - | - | Status alterado para RECEBIDO. | | |

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

## Testes de Fluxo Completo

### Fluxo 4 — PetShop: Compra -> Venda -> OS (Banho)

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro | Resultado |
|-------|------|------|-------|-----------------|------|---|
| 1 | Categoria | Criar "Cachorros" | Nome: Cachorros | OK | | |
| 2 | Fornecedor | Criar "Purina Pet" | CNPJ: 44.444.444/0001-91, RJ | OK | | |
| 3 | Cliente | Criar "Ana Beatriz" | CPF: 222.333.444-55 | OK | | |
| 4 | Produto | Criar "Ração Cães 15kg" | RAC-C-001, R$ 45,00/R$ 79,90, KG, Est: 20, perecível 15/12/2026 | OK | | |
| 5 | Produto | Criar "Brinquedo Osso" | BRI-001, R$ 3,50/R$ 9,90, UN, Est: 50 | OK | | |
| 6 | Técnico | Criar "Carlos Tosador" | Nome: Carlos Tosador | OK | | |
| 7 | Compras | Comprar 10 Rações | Purina Pet, A VISTA, refletir estoque | Estoque: 30 | | |
| 8 | Vendas | Vender 1 Ração + 2 Brinquedos | NF-PET-010, Ana Beatriz, PIX | Estoque: ração 29, brinquedos 48 | | |
| 9 | OS | Abrir OS Banho | Ana Beatriz, Carlos Tosador, "Banho + Tosa", R$ 55,00 | OS Aberta | | |
| 10 | OS | Finalizar OS | Status: Finalizado | OS finalizada | | |
| 11 | PDV | Vender 1 Ração avulsa | R$ 79,80 recebido R$ 100,00 | Troco: R$ 20,20. Estoque ração: 28 | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
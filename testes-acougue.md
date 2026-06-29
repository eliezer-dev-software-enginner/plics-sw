# Testes — Perfil Açougue "Boi Nobre"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | Boi Nobre Carnes | |
| CNPJ | 55.666.777/0001-88 | |
| Celular | (41) 99777-8888 | |
| Email | boinobre@carnes.com.br | |
| Cidade | Curitiba - PR | |
| Produtos típicos | Picanha kg, Alcatra kg, Coxa de frango kg, Linguiça kg | |
| Categorias | Bovinos, Aves, Suínos, Embutidos | |
| Fornecedores | Frigorífico Paraná, Avícola Sul, Distribuidora de Carnes PR | |
| Clientes | José Moura, Renata Oliveira, Paulo Sérgio | |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Preço Compra | Preço Venda | Categoria | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|-----|-----------|-------|-------------|-------------|-----------|---------|-----------------|------|---|
| 25 | Produto kg | CAR001 | Picanha | KG | R$ 45,00 | R$ 79,90 | Bovinos | 50 | Salvo com sucesso. | | |
| 26 | Produto kg aves | CAR002 | Coxa de Frango | KG | R$ 8,00 | R$ 14,90 | Aves | 80 | Salvo com sucesso. | | |
| 27 | Produto em gramas | CAR003 | Bacon fatiado 500g | g | R$ 18,00 | R$ 35,00 | Embutidos | 30 | Salvo com sucesso com unidade "g". | | |
| 28 | Unidade não selecionada | CAR004 | Linguiça | (vazio) | R$ 10,00 | R$ 19,90 | Embutidos | 40 | Alerta: "Unidade é obrigatória". | | |
| 29 | Preço venda zerado | CAR005 | Alcatra | KG | R$ 35,00 | R$ 0,00 | Bovinos | 30 | Alerta: "Preço de venda deve ser maior que zero". | | |

---


---

## VendaMercadoriaScreen

| # | Cenário | Data | Cliente | NF | Produto | Qtd | Preço unit. | Pagamento | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|----|---------|-----|------------|-----------|---------|-----------------|------|-----------|
| 57 | Venda de produto em gramas | 15/06/2026 | José Moura | NF-AC-001 | Bacon fatiado (CAR003) | 500 | R$ 0,07/g | A VISTA | Sim | Venda registrada R$ 35,00 (500g x R$ 0,07/g). Estoque 5030 -> 4530 g. | | |
| 107 | Venda de kg em gramas (conversão automática) | 15/06/2026 | José Moura | NF-AC-003 | Picanha (CAR001) | 0,300 | R$ 79,90/kg | A VISTA | Sim | Venda registrada com 0,300 kg de Picanha (R$ 79,90/kg = R$ 23,97). Deve converter 300g para 0,300 kg. Estoque 80 -> 79,7 kg. | | |

---

## ComprasScreen

| # | Cenário | Fornecedor | Produto | Qtd | Unid. | Preço unit. | Pagamento | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|---------|-----|-------|------------|-----------|-----------------|------|---|
| 61 | Compra de carne em kg | Frigorífico Paraná | Picanha | 30 | KG | R$ 45,00 | A PRAZO | Compra registrada. Estoque 50 -> 80 kg. Gera contas a pagar. | | |
| 62 | Compra de produto em gramas | Frigorífico Paraná | Bacon fatiado | 5000 | g | R$ 18,00 | A VISTA | Compra registrada em gramas. Estoque 30 -> 5030 g. | | |
| 109 | Compra de kg em gramas | Frigorífico Paraná | Picanha | 15000 | g | R$ 45,00/kg | A VISTA | Compra registrada. Deve converter 15000g para 15 kg. Estoque 80 -> 95 kg. | | |

---

## PDVScreen

| # | Cenário | Produto | Qtd | Unid. | Total | Recebido | Troco | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|-------|----------|-------|-----------------|------|---|
| 89 | Venda produto em gramas | Bacon fatiado | 300 | g | R$ 21,00 | R$ 25,00 | R$ 4,00 | Venda registrada em gramas. Estoque 4530 -> 4230 g. | | |
| 108 | Venda kg em gramas | Picanha | 0,300 | KG | R$ 23,97 | R$ 25,00 | R$ 1,03 | Venda registrada (0,300 kg x R$ 79,90/kg = R$ 23,97). Estoque 80 -> 79,7 kg. | | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Texto Responsabilidade | Efeito Esperado | Erro | Resultado |
|---|---------|------|--------|----------------------|-----------------|------|---|
| 97 | Cadastro Açougue | Boi Nobre Carnes | Curitiba | "Vendemos somente carnes inspecionadas" | Salvo com sucesso. | | |

---

## Testes de Fluxo Completo

### Fluxo 3 — Açougue: OS + Venda

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro | Resultado |
|-------|------|------|-------|-----------------|------|---|
| 1 | Técnico | Criar "João Mecânico" | Nome: João Mecânico | OK | | |
| 2 | Cliente | Criar "José Moura" | CPF: 555.666.777-88 | OK | | |
| 3 | Produto | Criar "Picanha kg" | CAR001, R$ 45,00/R$ 79,90, KG, Est: 50 | OK | | |
| 4 | Produto | Criar "Bacon fatiado g" | CAR003, R$ 18,00/R$ 35,00, g, Est: 5000 | OK. Unidade "g" selecionada e salva. | | |
| 5 | Ordem Serviço | Abrir OS | José Moura, João Mecânico, "Serra fita", R$ 200/R$ 100 | OS Aberta, Total R$ 300 | | |
| 6 | Ordem Serviço | Finalizar OS | Status: Finalizado | OS finalizada | | |
| 7 | PDV | Vender Picanha | 2kg, R$ 79,90/kg, Total R$ 159,80 | Estoque: 48 kg | | |
| 8 | PDV | Vender Bacon em gramas | 300g Bacon, R$ 0,07/g, Total R$ 21,00 | Estoque: 5000 g -> 4700 g. | | |
| 9 | PDV | Vender Picanha em gramas (kg→g) | 300g Picanha (R$ 79,90/kg = R$ 0,0799/g), Total R$ 23,97 | Sistema converte 300g para 0,300 kg automaticamente. Estoque: 79,7 kg -> 79,4 kg. | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
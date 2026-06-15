# Testes — Perfil Açougue "Boi Nobre"

## Perfil do Negócio

| Campo | Valor |
|-------|-------|
| Nome fantasia | Boi Nobre Carnes |
| CNPJ | 55.666.777/0001-88 |
| Celular | (41) 99777-8888 |
| Email | boinobre@carnes.com.br |
| Cidade | Curitiba - PR |
| Produtos típicos | Picanha kg, Alcatra kg, Coxa de frango kg, Linguiça kg |
| Categorias | Bovinos, Aves, Suínos, Embutidos |
| Fornecedores | Frigorífico Paraná, Avícola Sul, Distribuidora de Carnes PR |
| Clientes | José Moura, Renata Oliveira, Paulo Sérgio |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Preço Compra | Preço Venda | Categoria | Estoque | Efeito Esperado | Erro |
|---|---------|-----|-----------|-------|-------------|-------------|-----------|---------|-----------------|------|
| 25 | Produto kg | CAR001 | Picanha | KG | R$ 45,00 | R$ 79,90 | Bovinos | 50 | Salvo com sucesso. | |
| 26 | Produto kg aves | CAR002 | Coxa de Frango | KG | R$ 8,00 | R$ 14,90 | Aves | 80 | Salvo com sucesso. | |
| 27 | Produto em gramas | CAR003 | Bacon fatiado 500g | g | R$ 18,00 | R$ 35,00 | Embutidos | 30 | Salvo com sucesso com unidade "g". | |
| 28 | Unidade não selecionada | CAR004 | Linguiça | (vazio) | R$ 10,00 | R$ 19,90 | Embutidos | 40 | Alerta: "Unidade é obrigatória". | |
| 29 | Preço venda zerado | CAR005 | Alcatra | KG | R$ 35,00 | R$ 0,00 | Bovinos | 30 | Alerta: "Preço de venda deve ser maior que zero". | |

---

## VendaMercadoriaScreen

| # | Cenário | Data | Cliente | NF | Produto | Qtd (g) | Preço/g | Pagamento | Estoque | Efeito Esperado | Erro |
|---|---------|------|---------|----|---------|---------|---------|-----------|---------|-----------------|------|
| 57 | Venda de produto em gramas | 15/06/2026 | José Moura | NF-AC-001 | Bacon fatiado (CAR003) | 500 | R$ 0,07 | A VISTA | Sim | Venda registrada R$ 35,00 (500g x R$ 0,07/g). Estoque 5030 -> 4530 g. | |

---

## ComprasScreen

| # | Cenário | Fornecedor | Produto | Qtd | Unid. | Preço unit. | Pagamento | Efeito Esperado | Erro |
|---|---------|-----------|---------|-----|-------|------------|-----------|-----------------|------|
| 61 | Compra de carne em kg | Frigorífico Paraná | Picanha | 30 | KG | R$ 45,00 | A PRAZO | Compra registrada. Estoque 50 -> 80 kg. Gera contas a pagar. | |
| 62 | Compra de produto em gramas | Frigorífico Paraná | Bacon fatiado | 5000 | g | R$ 18,00 | A VISTA | Compra registrada em gramas. Estoque 30 -> 5030 g. | |

---

## PDVScreen

| # | Cenário | Produto | Qtd | Unid. | Total | Recebido | Troco | Efeito Esperado | Erro |
|---|---------|---------|-----|-------|-------|----------|-------|-----------------|------|
| 89 | Venda produto em gramas | Bacon fatiado | 300 | g | R$ 21,00 | R$ 25,00 | R$ 4,00 | Venda registrada em gramas. Estoque 4530 -> 4230 g. | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Texto Responsabilidade | Efeito Esperado | Erro |
|---|---------|------|--------|----------------------|-----------------|------|
| 97 | Cadastro Açougue | Boi Nobre Carnes | Curitiba | "Vendemos somente carnes inspecionadas" | Salvo com sucesso. | |

---

## Testes de Fluxo Completo

### Fluxo 3 — Açougue: OS + Venda

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro |
|-------|------|------|-------|-----------------|------|
| 1 | Técnico | Criar "João Mecânico" | Nome: João Mecânico | OK | |
| 2 | Cliente | Criar "José Moura" | CPF: 555.666.777-88 | OK | |
| 3 | Produto | Criar "Picanha kg" | CAR001, R$ 45,00/R$ 79,90, KG, Est: 50 | OK | |
| 4 | Produto | Criar "Bacon fatiado g" | CAR003, R$ 18,00/R$ 35,00, g, Est: 5000 | OK. Unidade "g" selecionada e salva. | |
| 5 | Ordem Serviço | Abrir OS | José Moura, João Mecânico, "Serra fita", R$ 200/R$ 100 | OS Aberta, Total R$ 300 | |
| 6 | Ordem Serviço | Finalizar OS | Status: Finalizado | OS finalizada | |
| 7 | PDV | Vender Picanha | 2kg, R$ 79,90/kg, Total R$ 159,80 | Estoque: 48 kg | |
| 8 | PDV | Vender Bacon em gramas | 300g Bacon, R$ 0,07/g, Total R$ 21,00 | Estoque: 5000 g -> 4700 g. | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada

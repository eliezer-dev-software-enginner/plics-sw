# Testes — Perfil Açougue "Boi Nobre"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | Boi Nobre Carnes | |
| CNPJ | 55.666.777/0001-81 | |
| Celular | (41) 99777-8888 | |
| Email | boinobre@carnes.com.br | |
| Cidade | Curitiba - PR | |
| Produtos típicos | Picanha kg, Alcatra kg, Coxa de frango kg, Linguiça kg | |
| Categorias | Bovinos, Aves, Suínos, Embutidos | |
| Fornecedores | Frigorífico Paraná | |
| Clientes | José Moura | |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Marca | Fornecedor | Cor | Tamanho | Modelo | Preço Compra | Preço Venda | Frete | Categoria | Estoque | Est. Mínimo | Perecível | Validade | Garantia | Devolução | Observações | Imagem | Efeito Esperado | Erro | Resultado |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 25 | Produto kg | CAR001 | Picanha | KG | Generica | Frigorífico Paraná | - | - | - | R$ 45,00 | R$ 79,90 | R$ 0,00 | Bovinos | 50 | 10 | Sim | 05/08/2026 | - | Não | Carne bovina premium | - | Salvo com sucesso. |  |  |
| 26 | Produto kg aves | CAR002 | Coxa de Frango | KG | Generica | Frigorífico Paraná | - | - | - | R$ 8,00 | R$ 14,90 | R$ 0,00 | Aves | 80 | 15 | Sim | 30/07/2026 | - | Não | Frango resfriado | - | Salvo com sucesso. |  |  |
| 27 | Produto em gramas | CAR003 | Bacon fatiado 500g | g | Generica | Frigorífico Paraná | - | - | - | R$ 18,00 | R$ 35,00 | R$ 0,00 | Embutidos | 30 | 5 | Sim | 10/09/2026 | - | Não | Bacon defumado fatiado | - | Salvo com sucesso com unidade "g". |  |  |
| 28 | Unidade não selecionada | CAR004 | Linguiça | (vazio) | Generica | Frigorífico Paraná | - | - | - | R$ 10,00 | R$ 19,90 | R$ 0,00 | Embutidos | 40 | 5 | Nao | - | - | Não | - | - | Alerta: "Unidade é obrigatória". |  |  |
| 29 | Preço venda zerado | CAR005 | Alcatra | KG | Generica | Frigorífico Paraná | - | - | - | R$ 35,00 | R$ 0,00 | R$ 0,00 | Bovinos | 30 | 5 | Nao | - | - | Não | - | - | Alerta: "Preço de venda deve ser maior que zero". |  |  |
| 126 | Produto com garantia | CAR006 | Acém | KG | Generica | Frigorífico Paraná | - | - | - | R$ 28,00 | R$ 49,90 | R$ 0,00 | Bovinos | 40 | 5 | Nao | - | 3 dias | Não | Carne bovina para cozimento lento | - | Salvo com sucesso. Garantia de validade registrada. |  |  |
| 163 | Produto perecível sem validade | CAR007 | Costela Bovina | KG | Generica | Frigorífico Paraná | - | - | - | R$ 22,00 | R$ 39,90 | R$ 0,00 | Bovinos | 25 | 5 | Sim | (vazio) | - | Não | - | - | Alerta: "Data de validade é obrigatória para produtos perecíveis". |  |  |
| 185 | Produto com frete e aceita troca | CAR008 | Linguiça Artesanal Congelada | KG | Generica | Frigorífico Paraná | - | - | - | R$ 18,00 | R$ 32,90 | R$ 2,50 | Suínos | 35 | 5 | Sim | 30/09/2026 | - | Sim | Aceita troca se ainda lacrada e dentro da validade | - | Salvo com sucesso. Ganho líquido estimado descontado o frete. |  |  |

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
| 2 | Cliente | Reaproveitar cadastro de "José Moura" (dados de teste — Clientes, testes-gerais.md) | CPF: 555.666.777-88 | OK | | |
| 3 | Produto | Reaproveitar cadastro de "Picanha kg" (caso #25, ProdutoScreen) | CAR001, R$ 45,00/R$ 79,90, KG, Est: 50 | OK | | |
| 4 | Produto | Reaproveitar cadastro de "Bacon fatiado g" (caso #27, ProdutoScreen) | CAR003, R$ 18,00/R$ 35,00, g, Est: 5000 | OK. Unidade "g" selecionada e salva. | | |
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
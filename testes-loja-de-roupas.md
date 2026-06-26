# Testes — Perfil Loja de Roupas "Moda & Estilo"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | Moda & Estilo Ltda | |
| CNPJ | 12.345.678/0001-90 | |
| Celular | (11) 98765-4321 | |
| Email | contato@modaestilo.com.br | |
| Cidade | São Paulo - SP | |
| Produtos típicos | Camisetas, Calças, Vestidos, Jaquetas | |
| Categorias | Masculino, Feminino, Infantil, Acessórios | |
| Fornecedores | Malharia Silva, Tecidos ABC, Avan Calçados | |
| Clientes | Maria Souza, João Pedro, Carla Lima | |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Marca | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Perecível | Validade | Efeito Esperado | Erro                                                                                          | Resultado |
|---|---------|-----|-----------|-------|-------|-------------|-------------|-----------|-----------|---------|-----------|----------|-----------------|-----------------------------------------------------------------------------------------------|-----------|
| 13 | Cadastro válido produto simples | SKU001 | Camiseta Masculina M | UN | Malharia Silva | R$ 15,00 | R$ 39,90 | Masculino | Malharia Silva | 100 | Nao | - | Salvo com sucesso. |                                                                                               | ok        |
| 14 | Cadastro válido produto perecível | SKU002 | Vestido Feminino Algodão | UN | Tecidos ABC | R$ 35,00 | R$ 89,90 | Feminino | Tecidos ABC | 50 | Sim | 15/12/2026 | Salvo com sucesso. | ~~For input string: "1797044400000"~~ Corrigido — validade REAL aceita Long. | ok        |
| 15 | SKU duplicado | SKU001 | Jaqueta | UN | Avan Calçados | R$ 80,00 | R$ 199,90 | Masculino | Avan Calçados | 30 | Nao | - | Alerta: "Código de barras já cadastrado". | ~~Desmarquei "é perecível" para poder salvar esse produto, e o erro foi semelhante ao de cima. For input string: "1797044400000"~~ Corrigido — fillModelFromForm só seta validade se perecível for "Sim". | ok        |
| 16 | Descrição vazia | SKU003 | (vazio) | UN | Generica | R$ 10,00 | R$ 25,00 | Acessórios | Malharia Silva | 20 | Nao | - | Alerta: "Descrição é obrigatória". |                                                                                               |           |
| 17 | SKU vazio | (vazio) | Produto sem SKU | UN | Generica | R$ 10,00 | R$ 25,00 | Acessórios | Malharia Silva | 20 | Nao | - | Alerta: "Código de barras é obrigatório". |                                                                                               | ok        |
| 18 | Preço compra maior que venda | SKU004 | Calça Jeans | UN | Tecidos ABC | R$ 100,00 | R$ 80,00 | Feminino | Tecidos ABC | 30 | Nao | - | Alerta de margem negativa, mas salva? (Validar comportamento). |                                                                                               |           |
| 19 | Perecível sem validade | SKU005 | Leite | UN | Marca X | R$ 3,00 | R$ 5,00 | Alimentos | Malharia Silva | 50 | Sim | (vazio) | Alerta: "Data de validade é obrigatória para produtos perecíveis". |                                                                                               |           |
| 20 | Excluir produto vinculado a venda | (produto com venda) | - | - | - | - | - | - | - | - | - | - | Alerta: "Produto possui vendas vinculadas". |                                                                                               |           |

---

## CategoriaScreen

| # | Cenário | Nome | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------------|------|---|
| 30 | Cadastro válido - Loja Roupas | Masculino | Salvo com sucesso. | | |

---

## FornecedorScreen

| # | Cenário | Nome Fantasia | CNPJ | Celular | IE | Email | UF | Cidade | Bairro | Rua | Número | Obs | Efeito Esperado | Erro | Resultado |
|---|---------|--------------|------|---------|----|-------|----|--------|--------|-----|--------|-----|-----------------|------|---|
| 39 | Cadastro completo | Malharia Silva | 11.111.111/0001-11 | (11) 98888-7777 | 111.222.333.444 | silva@malharia.com | SP | São Paulo | Brás | Rua da Malharia | 123 | - | Salvo com sucesso. | | |
| 40 | Cadastro mínimo obrigatório | Avan Calçados | 22.222.222/0001-22 | (11) 97777-6666 | - | - | SP | São Paulo | - | - | - | - | Salvo com sucesso (nome e CNPJ obrigatórios?). | | |
| 41 | Nome vazio | (vazio) | 33.333.333/0001-33 | - | - | - | - | - | - | - | - | - | Alerta: "Nome é obrigatório". | | |
| 42 | CNPJ duplicado | (CNPJ já existente) | 11.111.111/0001-11 | - | - | - | - | - | - | - | - | - | Alerta: "CNPJ já cadastrado". | | |
| 43 | Editar dados do fornecedor | Malharia Silva (novo telefone) | - | (11) 99999-0000 | - | - | - | - | - | - | - | - | Atualizado com sucesso. | | |

---

## VendaMercadoriaScreen

| # | Cenário | Data | Cliente | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Obs | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|----|---------|-----|-------|-------|-----------|---------|-----|-----------------|------|---|
| 47 | Venda à vista completa | 15/06/2026 | Maria Souza | NF001 | Camiseta M (SKU001) | 2 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Venda registrada. Estoque vai de 100 para 98. | | |
| 48 | Venda com desconto | 15/06/2026 | João Pedro | NF002 | Calça Jeans (SKU004) | 1 | R$ 80,00 | R$ 10,00 | CREDITO | Sim | - | Venda registrada com total líquido R$ 70,00. | | |
| 49 | Venda a prazo (parcelado) | 15/06/2026 | Carla Lima | - | Vestido (SKU002) | 1 | R$ 89,90 | R$ 0 | A PRAZO | Sim | - | Gera 3 parcelas no Contas a Receber. | | |
| 50 | Produto sem estoque | 15/06/2026 | Maria Souza | - | Camiseta M | 500 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Alerta: "Estoque insuficiente". | | |
| 51 | Cliente não selecionado | 15/06/2026 | (vazio) | - | Camiseta M | 1 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Alerta: "Selecione um cliente". | | |
| 52 | Produto não encontrado | 15/06/2026 | Maria Souza | - | INEXISTENTE | 1 | - | - | - | - | - | Alerta: "Produto não encontrado". | | |
| 53 | Venda sem refletir estoque | 15/06/2026 | Maria Souza | NF003 | Jaqueta (SKU003) | 1 | R$ 199,90 | R$ 0 | DEBITO | Nao | - | Venda registrada. Estoque permanece 30. | | |
| 54 | Venda com observação | 15/06/2026 | João Pedro | NF004 | Camiseta M | 3 | R$ 39,90 | R$ 5,00 | PIX | Sim | "Presente" | Venda registrada com observação. | | |

---

## ComprasScreen

| # | Cenário | Data | Fornecedor | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------|----|---------|-----|-------|-------|-----------|---------|-----------------|------|---|
| 58 | Compra à vista | 14/06/2026 | Malharia Silva | NF-COM-001 | Camiseta M | 50 | R$ 15,00 | R$ 0 | A VISTA | Sim | Estoque 100 -> 150. Custo registrado. | | |
| 59 | Compra com desconto | 14/06/2026 | Tecidos ABC | NF-COM-002 | Vestido | 20 | R$ 35,00 | R$ 50,00 | A PRAZO | Sim | Compra registrada. Gera contas a pagar. | | |
| 60 | Não refletir no estoque | 14/06/2026 | Avan Calçados | - | Jaqueta | 10 | R$ 80,00 | R$ 0 | A VISTA | Nao | Compra registrada. Estoque não alterado. | | |
| 61 | Fornecedor não selecionado | 14/06/2026 | (vazio) | - | Camiseta M | 10 | R$ 15,00 | - | A VISTA | Sim | Alerta: "Selecione um fornecedor". | | |

---

## ComprasAPagarScreen

| # | Cenário | Descrição | Valor Original | Fornecedor | Status | Vencimento | Tipo Doc | Nº Doc | Observação | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|--------------|-----------|--------|------------|----------|--------|------------|-----------------|------|---|
| 63 | Cadastro manual de conta | Compra Tecidos ABC | R$ 700,00 | Tecidos ABC | PENDENTE | 15/07/2026 | DUPLICATA | NF-COM-002 | - | Salvo com sucesso. | | |
| 64 | Pagamento parcial | (conta existente) | - | - | PARCIAL | - | - | - | - | Pagamento parcial registrado. Status atualizado. | | |
| 65 | Pagamento total | (conta existente) | - | - | PAGO | - | - | - | - | Status alterado para PAGO. | | |
| 66 | Descrição vazia | (vazio) | R$ 100,00 | Fornecedor X | PENDENTE | 15/07/2026 | - | - | - | Alerta: "Descrição é obrigatória". | | |
| 67 | Valor zerado | Conta teste | R$ 0,00 | Fornecedor X | PENDENTE | 15/07/2026 | - | - | - | Alerta: "Valor deve ser maior que zero". | | |
| 66 | Pagamento maior que restante | (conta R$ 100) | Pagamento: R$ 200 | - | - | - | - | - | - | Alerta: "Valor do pagamento excede o restante". | | |

---

## ContasAReceberScreen

| # | Cenário | Descrição | Valor Original | Cliente | Status | Vencimento | Tipo Doc | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|--------------|--------|--------|------------|----------|-----------------|------|---|
| 70 | Conta gerada por venda a prazo | Venda Carla Lima (Vestido) | R$ 89,90 | Carla Lima | PENDENTE | 15/07/2026 | - | Gerada automaticamente. 3 parcelas de ~R$ 29,97. | | |
| 71 | Recebimento parcial | (conta existente) | Recebimento: R$ 50,00 | - | PARCIAL | - | - | Valor restante atualizado. | | |
| 72 | Recebimento total | (conta existente) | Restante integral | - | RECEBIDO | - | - | Status alterado para RECEBIDO. | | |
| 73 | Descrição vazia | (vazio) | R$ 200,00 | Maria Souza | PENDENTE | 15/08/2026 | - | Alerta: "Descrição é obrigatória". | | |

---

## OrdemServicoScreen

| # | Cenário | Data | Cliente | Técnico | Equipamento | Mão de Obra | Peças | Pagamento | Status | Checklist | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|---------|-------------|-------------|-------|-----------|--------|-----------|-----------------|------|---|
| 75 | OS completa | 15/06/2026 | Maria Souza | (técnico) | Máquina Costura Industrial | R$ 150,00 | R$ 80,00 | A VISTA | Aberto | "Trocar motor, lubrificar" | Salva com total R$ 230,00. | | |
| 76 | OS com status Alterado | 16/06/2026 | João Pedro | (técnico) | Prensa estamparia | R$ 200,00 | R$ 50,00 | PIX | Finalizado | "Reparo concluído" | Status alterado. | | |
| 77 | Cliente não selecionado | 15/06/2026 | (vazio) | (técnico) | Equipamento | R$ 100,00 | R$ 0 | A VISTA | Aberto | - | Alerta: "Selecione um cliente". | | |
| 78 | Valor zerado total | 15/06/2026 | Maria Souza | (técnico) | Equipamento | R$ 0 | R$ 0 | - | Aberto | - | Alerta: "Informe valor de mão de obra ou peças". | | |

---

## PDVScreen

| # | Cenário | Produto | Qtd | Total | Recebido | Fiado | Cliente (fiado) | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|----------|-------|-----------------|-----------------|------|---|
| 84 | Venda avulsa (não fiada) | Camiseta M | 2 | R$ 79,80 | R$ 100,00 | Não | - | Troco: R$ 20,20. Pedido registrado. | | |
| 85 | Venda fiada | Calça Jeans | 1 | R$ 80,00 | - | Sim | Maria Souza | Pedido registrado como fiado. Gera conta a receber. | | |
| 86 | Venda fiada sem cliente | Jaqueta | 1 | R$ 199,90 | - | Sim | (vazio) | Alerta: "Selecione um cliente para venda fiada". | | |
| 87 | Recebimento menor que total | Camiseta M | 3 | R$ 119,70 | R$ 50,00 | Não | - | Alerta: "Valor recebido é insuficiente". | | |
| 88 | Carrinho vazio | (nenhum) | 0 | R$ 0 | - | - | - | Alerta: "Adicione produtos ao carrinho". | | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Celular | CEP | Cidade | Bairro | Rua | Local Pagamento | Resp. | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|-----|--------|--------|-----|----------------|-------|-----------------|------|---|
| 93 | Cadastro completo | Moda & Estilo Ltda | (11) 98765-4321 | 01001000 | São Paulo | Centro | Rua da Moda | Pagável em qualquer banco | "Não aceitamos devoluções" | Salvo com sucesso. | | |
| 94 | Nome vazio | (vazio) | - | - | - | - | - | - | - | Alerta: "Nome da empresa é obrigatório". | | |

---

## Testes de Fluxo Completo

### Fluxo 1 — Loja de Roupas: Compra -> Venda -> Financeiro

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro | Resultado |
|-------|------|------|-------|-----------------|------|---|
| 1 | Categoria | Criar "Masculino" | Nome: Masculino | OK | | |
| 2 | Fornecedor | Criar "Malharia Silva" | CNPJ: 11.111.111/0001-11, SP | OK | | |
| 3 | Produto | Criar "Camiseta M" | SKU001, R$ 15,00/R$ 39,90, UN, Est: 100 | OK | | |
| 4 | Cliente | Criar "Maria Souza" | CPF: 123.456.789-09 | OK | | |
| 5 | Compras | Comprar 50 Camisetas | Malharia Silva, R$ 15,00, A VISTA, refletir estoque | Estoque: 150 | | |
| 6 | Vendas | Vender 3 Camisetas | Maria Souza, R$ 39,90, PIX, refletir estoque | Estoque: 147 | | |
| 7 | PDV | Vender 2 Camisetas | Avulsa, R$ 79,80, R$ 100 recebido | Troco: R$ 20,20. Estoque: 145 | | |
| 8 | Contas a Receber | Verificar | Nenhuma pendente (vendas à vista) | Vazio | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
# Testes — Perfil Loja de Roupas "Moda & Estilo"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | Moda & Estilo Ltda | |
| CNPJ | 12.345.678/0001-95 | |
| Celular | (11) 98765-4321 | |
| Email | contato@modaestilo.com.br | |
| Cidade | São Paulo - SP | |
| Produtos típicos | Camisetas, Calças, Vestidos, Jaquetas | |
| Categorias | Masculino, Feminino, Infantil, Acessórios | |
| Fornecedores | Malharia Silva, Tecidos ABC, Avan Calçados | |
| Clientes | Maria Souza, João Pedro, Carla Lima | |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Marca | Cor | Tamanho | Modelo | Preço Compra | Preço Venda | Frete | Categoria | Fornecedor | Estoque | Est. Mínimo | Perecível | Validade | Garantia | Devolução | Observações | Imagem | Efeito Esperado | Erro | Resultado |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13 | Cadastro válido produto simples | SKU001 | Camiseta Masculina M | UN | Malharia Silva | Azul | M | - | R$ 15,00 | R$ 39,90 | R$ 0,00 | Masculino | Malharia Silva | 100 | 10 | Nao | - | - | Não | Tecido algodão | - | Salvo com sucesso. |  | ok |
| 14 | Cadastro válido produto perecível | SKU002 | Vestido Feminino Algodão | UN | Tecidos ABC | Vermelho | P | - | R$ 35,00 | R$ 89,90 | R$ 0,00 | Feminino | Tecidos ABC | 50 | 5 | Sim | 15/12/2026 | - | Não | - | - | Salvo com sucesso. | ~~For input string: "1797044400000"~~ Corrigido — validade REAL aceita Long. | ok |
| 15 | SKU duplicado | SKU001 | Jaqueta | UN | Avan Calçados | Preto | G | - | R$ 80,00 | R$ 199,90 | R$ 0,00 | Masculino | Avan Calçados | 30 | 5 | Nao | - | - | Não | - | - | Alerta: "Código de barras já cadastrado". | ~~Desmarquei "é perecível" para poder salvar esse produto, e o erro foi semelhante ao de cima. For input string: "1797044400000"~~ Corrigido — fillModelFromForm só seta validade se perecível for "Sim". | ok |
| 16 | Descrição vazia | SKU003 | (vazio) | UN | Generica | - | - | - | R$ 10,00 | R$ 25,00 | R$ 0,00 | Acessórios | Malharia Silva | 20 | 5 | Nao | - | - | Não | - | - | Alerta: "Descrição é obrigatória". |  | ok |
| 17 | SKU vazio | (vazio) | Produto sem SKU | UN | Generica | - | - | - | R$ 10,00 | R$ 25,00 | R$ 0,00 | Acessórios | Malharia Silva | 20 | 5 | Nao | - | - | Não | - | - | Alerta: "Código de barras é obrigatório". |  | ok |
| 18 | Preço compra maior que venda | SKU006 | Calça Jeans | UN | Tecidos ABC | Azul | G | Reta | R$ 100,00 | R$ 80,00 | R$ 0,00 | Feminino | Tecidos ABC | 30 | 5 | Nao | - | - | Não | - | - | Alerta de margem negativa, mas salva? (Validar comportamento). |  |  |
| 19 | Perecível sem validade | SKU005 | Camiseta Promocional | UN | Malharia Silva | Branco | M | - | R$ 15,00 | R$ 29,90 | R$ 0,00 | Masculino | Malharia Silva | 50 | 5 | Sim | (vazio) | - | Não | - | - | Alerta: "Data de validade é obrigatória para produtos perecíveis". |  | ok |
| 20 | Excluir produto vinculado a venda | (produto com venda) | - | - | - | - | - | - | - | - | R$ 0,00 | - | - | - | - | - | - | - | Não | - | - | Alerta: "Produto possui vendas vinculadas". |  |  |
| 143 | Cadastro válido jaqueta | SKU003 | Jaqueta | UN | Avan Calçados | Preto | G | Couro | R$ 60,00 | R$ 199,90 | R$ 0,00 | Masculino | Avan Calçados | 30 | 5 | Nao | - | 3 meses | Não | Jaqueta de couro sintético | - | Salvo com sucesso. |  | ok |
| 144 | Cadastro válido calça | SKU004 | Calça Jeans | UN | Tecidos ABC | Azul | M | Skinny | R$ 40,00 | R$ 80,00 | R$ 0,00 | Feminino | Tecidos ABC | 30 | 5 | Nao | - | - | Não | Calça jeans stretch | - | Salvo com sucesso. |  | ok |
| 148 | Produto com garantia e imagem | SKU007 | Blazer Feminino | UN | Tecidos ABC | Preto | M | Social | R$ 120,00 | R$ 299,90 | R$ 0,00 | Feminino | Tecidos ABC | 15 | 3 | Nao | - | 6 meses | Não | Blazer social feminino, tecido importado | (arquivo jpg) | Salvo com sucesso. Imagem armazenada. |  |  |
| 153 | Produto com múltiplas cores | SKU008 | Camiseta Estampada | UN | Malharia Silva | Azul, Vermelho, Preto | M | - | R$ 18,00 | R$ 45,00 | R$ 0,00 | Masculino | Malharia Silva | 60 | 10 | Nao | - | - | Não | Disponível em 3 cores | - | Salvo com sucesso. As 3 cores selecionadas ficam registradas. |  |  |
| 182 | Produto com frete e aceita troca | SKU009 | Bota de Couro | UN | Avan Calçados | Marrom | 40 | - | R$ 90,00 | R$ 249,90 | R$ 15,00 | Masculino | Avan Calçados | 25 | 5 | Nao | - | 6 meses | Sim | Aceita troca em até 30 dias | - | Salvo com sucesso. Ganho líquido estimado descontado o frete. |  |  |

---


---


---

## VendaMercadoriaScreen

| # | Cenário | Data | Cliente | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Obs | Efeito Esperado | Erro | Resultado                                                           |
|---|---------|------|---------|----|---------|-----|-------|-------|-----------|---------|-----|-----------------|------|---------------------------------------------------------------------|
| 47 | Venda à vista completa | 15/06/2026 | Maria Souza | NF001 | Camiseta M (SKU001) | 2 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Venda registrada. Estoque vai de 100 para 98. | | ok                                                                  |
| 48 | Venda com desconto | 15/06/2026 | João Pedro | NF002 | Calça Jeans (SKU004) | 1 | R$ 80,00 | R$ 10,00 | CREDITO | Sim | - | Venda registrada com total líquido R$ 70,00. | | ok                                                                  |
| 49 | Venda a prazo (parcelado) | 15/06/2026 | Carla Lima | - | Vestido (SKU002) | 1 | R$ 89,90 | R$ 0 | A PRAZO | Sim | - | Gera 3 parcelas no Contas a Receber. | ~~ArrayIndexOutOfBoundsException ao clicar "Adicionar" sem preencher quantidade.~~ Corrigido — validação de quantidade adicionada em handleAddOrUpdate(). | OK (fix: validação de quantidade em VendaMercadoriaScreenViewModel) |
| 50 | Produto sem estoque | 15/06/2026 | Maria Souza | - | Camiseta M | 500 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Alerta: "Estoque insuficiente". | | ok                                                                  |
| 52 | Produto não encontrado | 15/06/2026 | Maria Souza | - | INEXISTENTE | 1 | - | - | - | - | - | Alerta: "Produto não encontrado". | | ok                                                                  |
| 53 | Venda sem refletir estoque | 15/06/2026 | Maria Souza | NF003 | Jaqueta (SKU003) | 1 | R$ 199,90 | R$ 0 | DEBITO | Nao | - | Venda registrada. Estoque permanece 30. | | ok                                                                  |
| 54 | Venda com observação | 15/06/2026 | João Pedro | NF004 | Camiseta M | 3 | R$ 39,90 | R$ 5,00 | PIX | Sim | "Presente" | Venda registrada com observação. | | ok                                                                  |
| 154 | Venda deixa estoque abaixo do mínimo | 15/06/2026 | Maria Souza | NF005 | Blazer Feminino (SKU007) | 13 | R$ 299,90 | R$ 0 | A VISTA | Sim | - | Alerta de confirmação: estoque ficará em 2 (abaixo do mínimo 3). Ao confirmar, venda é registrada. | |                                                                       |

---

## ComprasScreen

| # | Cenário | Data | Fornecedor | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Obs | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------|----|---------|-----|-------|-------|-----------|---------|-----|-----------------|-|-----------|
| 58 | Compra à vista | 14/06/2026 | Malharia Silva | NF-COM-001 | Camiseta M | 50 | R$ 15,00 | R$ 0 | A VISTA | Sim | - | Estoque 100 -> 150. Custo registrado. | | ok        |
| 59 | Compra com desconto | 14/06/2026 | Tecidos ABC | NF-COM-002 | Vestido | 20 | R$ 35,00 | R$ 50,00 | A PRAZO | Sim | - | Compra registrada. Gera contas a pagar. | ~~Não cliquei em "Gerar parcelas" e cliquei normalmente em "Adicionar", e então a compra foi registrada normalmente, deveria exibir um alert: "Você não gerou nenhuma parcela para vendas a prazo."~~ Corrigido | ok        |
| 155 | Compra a prazo sem gerar parcelas | 14/06/2026 | Tecidos ABC | NF-COM-003 | Vestido | 20 | R$ 35,00 | R$ 50,00 | A PRAZO | Sim | "Não cliquei em Gerar parcelas" | Alerta: "Parcelas não informadas". Compra não gera contas a pagar. | | |
| 60 | Não refletir no estoque | 14/06/2026 | Avan Calçados | - | Jaqueta | 10 | R$ 80,00 | R$ 0 | A VISTA | Nao | Item frágil, conferir embalagem | Compra registrada. Estoque não alterado. | | ok        |
| 156 | Compra a prazo com data da 1ª parcela e nº de parcelas variáveis | 14/06/2026 | Tecidos ABC | NF-COM-004 | Vestido | 10 | R$ 35,00 | R$ 0 | A PRAZO | Sim | - | Data 1ª parcela: 15/07/2026, Qtd parcelas: 5. Clicar "Gerar parcelas" gera 5 parcelas mensais a partir de 15/07/2026. Compra registrada, gera 5 contas a pagar. | | |

---

## ComprasAPagarScreen

| # | Cenário | Descrição | Valor Original | Fornecedor | Status | Vencimento | Tipo Doc | Nº Doc | Data Pagamento | Observação | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|--------------|-----------|--------|------------|----------|--------|----------------|------------|-----------------|------|-----------|
| 63 | Cadastro manual de conta | Compra Tecidos ABC | R$ 700,00 | Tecidos ABC | PENDENTE | 15/07/2026 | DUPLICATA | NF-COM-002 | - | - | Salvo com sucesso. | | ok         |
| 64 | Pagamento parcial | (conta existente) | - | - | PARCIAL | - | - | - | 20/07/2026 | - | Pagamento parcial registrado. Status atualizado. | |           |
| 65 | Pagamento total | (conta existente) | - | - | PAGO | - | - | - | 25/07/2026 | - | Status alterado para PAGO. | |           |
| 66 | Descrição vazia | (vazio) | R$ 100,00 | Fornecedor X | PENDENTE | 15/07/2026 | - | - | - | - | Alerta: "Descrição é obrigatória". | |           |
| 67 | Valor zerado | Conta teste | R$ 0,00 | Fornecedor X | PENDENTE | 15/07/2026 | - | - | - | - | Alerta: "Valor deve ser maior que zero". | |           |
| 157 | Pagamento maior que restante | (conta R$ 100) | Pagamento: R$ 200 | - | - | - | - | - | - | - | Alerta: "Valor do pagamento excede o restante". | |           |
| 121 | Conta com N° Doc e observação | Compra Avan Calçados | R$ 2.400,00 | Avan Calçados | PENDENTE | 01/08/2026 | BOLETO | BOL-7890 | - | Compra de jaquetas para revenda | Salvo com sucesso. N° Doc e observação registrados. | |           |

---

## ContasAReceberScreen

| # | Cenário | Descrição | Valor Original | Cliente | Status | Vencimento | Tipo Doc | N° Doc | Data Recebimento | Observação | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|--------------|--------|--------|------------|----------|--------|------------------|------------|-----------------|------|---|
| 70 | Conta gerada por venda a prazo | Venda Carla Lima (Vestido) | R$ 89,90 | Carla Lima | PENDENTE | 15/07/2026 | - | - | - | - | Gerada automaticamente. 3 parcelas de ~R$ 29,97. | | |
| 71 | Recebimento parcial | (conta existente) | Recebimento: R$ 50,00 | - | PARCIAL | - | - | - | 20/07/2026 | - | Valor restante atualizado. | | |
| 72 | Recebimento total | (conta existente) | Restante integral | - | RECEBIDO | - | - | - | 25/07/2026 | Quitado | Status alterado para RECEBIDO. | | |
| 73 | Descrição vazia | (vazio) | R$ 200,00 | Maria Souza | PENDENTE | 15/08/2026 | - | - | - | - | Alerta: "Descrição é obrigatória". | | |
| 120 | Conta com N° Doc e observação | Venda João Pedro (Camisetas) | R$ 119,70 | João Pedro | PENDENTE | 20/07/2026 | NOTA FISCAL | NF-0042 | - | Cliente fiado recorrente | Salvo com sucesso. N° Doc e observação registrados. | | |

---

## OrdemServicoScreen

Nota: o formulário de cadastro/edição de OS não tem um controle de Status — toda OS nova é salva com status "Aberto" fixo (não há como escolher/alterar o status pela tela; "Status" só aparece como coluna somente-leitura na tabela e no modal de detalhes).

| # | Cenário | Data | Cliente | Técnico | Equipamento | Mão de Obra | Peças | Pagamento | Checklist | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|---------|-------------|-------------|-------|-----------|-----------|-----------------|------|---|
| 75 | OS completa | 15/06/2026 | Maria Souza | (técnico) | Máquina Costura Industrial | R$ 150,00 | R$ 80,00 | A VISTA | "Trocar motor, lubrificar" | Salva com total R$ 230,00, status "Aberto". | | |
| 76 | Editar OS existente | 16/06/2026 | João Pedro | (técnico) | Prensa estamparia | R$ 200,00 | R$ 50,00 | PIX | "Reparo concluído" | Dados atualizados com sucesso (status permanece "Aberto", tela não permite alterá-lo). | | |
| 77 | Cliente não selecionado | 15/06/2026 | (vazio) | (técnico) | Equipamento | R$ 100,00 | R$ 0 | A VISTA | - | Alerta: "Cliente é obrigatório". | | |
| 78 | Valor zerado total | 15/06/2026 | Maria Souza | (técnico) | Equipamento | R$ 0 | R$ 0 | - | - | Alerta: "Informe valor de mão de obra ou peças". | | |

---

## PDVScreen

| # | Cenário | Produto | Qtd | Total | Recebido | Fiado | Cliente (fiado) | Nº Parcelas | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|----------|-------|-----------------|-------------|-----------------|------|-----------|
| 84 | Venda avulsa (não fiada) | Camiseta M | 2 | R$ 79,80 | R$ 100,00 | Não | - | - | Troco: R$ 20,20. Pedido registrado. | | ok        |
| 85 | Venda fiada | Calça Jeans | 1 | R$ 80,00 | - | Sim | Maria Souza | 3 | Pedido registrado como fiado. Gera 3 parcelas na conta a receber. | |           |
| 86 | Venda fiada sem cliente | Jaqueta | 1 | R$ 199,90 | - | Sim | (vazio) | 1 | Alerta: "Selecione um cliente para venda fiada". | |           |
| 87 | Recebimento menor que total | Camiseta M | 3 | R$ 119,70 | R$ 50,00 | Não | - | - | Alerta: "Valor recebido é insuficiente". | |           |
| 88 | Carrinho vazio | (nenhum) | 0 | R$ 0 | - | - | - | - | Alerta: "Adicione produtos ao carrinho". | |           |
| 119 | Venda fiada 1 parcela | Vestido | 1 | R$ 89,90 | - | Sim | Carla Lima | 1 | Pedido fiado com 1 parcela (valor total). | |           |
| 159 | Criar cliente direto do PDV | Camiseta M | 1 | R$ 39,90 | R$ 40,00 | Não | (novo, via "+ Criar cliente") | - | Modal de cadastro de cliente abre inline; cliente criado fica selecionado na venda sem sair do PDV. | |           |
| 160 | Venda no crédito/débito/PIX (à vista) | Calça Jeans | 1 | R$ 80,00 | R$ 80,00 | Não | - | - | Pedido registrado com forma de pagamento CREDITO/DEBITO/PIX (testar as 3), sem gerar parcela. | |           |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Celular | CEP | Cidade | Bairro | Rua | Local Pagamento | Resp. | Efeito Esperado | Erro | Resultado |
|---|---------|------|---------|-----|--------|--------|-----|----------------|-------|-----------------|------|---|
| 93 | Cadastro completo | Moda & Estilo Ltda | (11) 98765-4321 | 01001000 | São Paulo | Centro | Rua da Moda | Pagável em qualquer banco | "Não aceitamos devoluções" | Salvo com sucesso. | | |
| 94 | Nome vazio | (vazio) | - | - | - | - | - | - | - | Alerta: "Nome da empresa é obrigatório". | | |
| 158 | Trocar logomarca | Moda & Estilo Ltda | (11) 98765-4321 | 01001000 | São Paulo | Centro | Rua da Moda | Pagável em qualquer banco | "Não aceitamos devoluções" | Nova imagem selecionada via "Mudar logomarca" é salva e passa a aparecer no cabeçalho/nota de venda. | | |

---

## Testes de Fluxo Completo

### Fluxo 1 — Loja de Roupas: Compra -> Venda -> Financeiro

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro | Resultado |
|-------|------|------|-------|-----------------|------|---|
| 1 | Categoria | Reaproveitar cadastro de "Masculino" (dados de teste — Categorias, testes-gerais.md) | Nome: Masculino | OK | | |
| 2 | Fornecedor | Reaproveitar cadastro de "Malharia Silva" (caso #39, testes-gerais.md) | CNPJ: 11.111.111/0001-91, SP | OK | | |
| 3 | Produto | Reaproveitar cadastro de "Camiseta M" (caso #13, ProdutoScreen) | SKU001, R$ 15,00/R$ 39,90, UN, Est: 100 | OK | | |
| 4 | Cliente | Reaproveitar cadastro de "Maria Souza" (caso #1, testes-gerais.md) | CPF: 123.456.789-09 | OK | | |
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
# Testes do Sistema Plics SW

## Como usar este documento

Cada teste contém:
- **Perfil**: o tipo de negócio simulado (loja de roupa, casa de ração, etc.)
- **Tela**: qual módulo está sendo testado
- **Cenário**: descrição do caso de teste
- **Dados de entrada**: valores preenchidos em cada campo
- **Efeito esperado**: comportamento esperado do sistema
- **Erro/Inconsistência**: campo livre para anotar problemas encontrados

---

## Sumário

1. [Perfis de Negócio](#1-perfis-de-negócio)
2. [Testes por Tela](#2-testes-por-tela)
   - [2.1 ClienteScreen](#21-clientescreen)
   - [2.2 ProdutoScreen](#22-produtoscreen)
   - [2.3 CategoriaScreen](#23-categoriascreen)
   - [2.4 FornecedorScreen](#24-fornecedorscreen)
   - [2.5 VendaMercadoriaScreen](#25-vendamercadoriascreen)
   - [2.6 ComprasScreen](#26-comprasscreen)
   - [2.7 ComprasAPagarScreen](#27-comprasapagarscreen)
   - [2.8 ContasAReceberScreen](#28-contasareceberscreen)
   - [2.9 OrdemServicoScreen](#29-ordemservicoscreen)
   - [2.10 TecnicoScreen](#210-tecnicoscreen)
   - [2.11 PDVScreen](#211-pdvscreen)
   - [2.12 CadastroEmpresaScreen](#212-cadastroempresascreen)
   - [2.13 AuthScreen](#213-authscreen)
   - [2.14 PreferenciasScreen](#214-preferenciascreen)

---

## 1. Perfis de Negócio

### Perfil A — Loja de Roupas "Moda & Estilo"

| Campo | Valor |
|-------|-------|
| Nome fantasia | Moda & Estilo Ltda |
| CNPJ | 12.345.678/0001-90 |
| Celular | (11) 98765-4321 |
| Email | contato@modaestilo.com.br |
| Cidade | São Paulo - SP |
| Produtos típicos | Camisetas, Calças, Vestidos, Jaquetas |
| Categorias | Masculino, Feminino, Infantil, Acessórios |
| Fornecedores | Malharia Silva, Tecidos ABC, Avan Calçados |
| Clientes | Maria Souza, João Pedro, Carla Lima |

### Perfil B — Casa de Ração "PetShop Amigo"

| Campo | Valor |
|-------|-------|
| Nome fantasia | PetShop Amigo |
| CNPJ | 98.765.432/0001-10 |
| Celular | (21) 99876-5432 |
| Email | contato@petshopamigo.com.br |
| Cidade | Rio de Janeiro - RJ |
| Produtos típicos | Ração cães 15kg, Ração gatos 10kg, Areia sanitária, Brinquedos |
| Categorias | Cachorros, Gatos, Aves, Peixes |
| Fornecedores | Purina Pet, Royal Canin, PetBrasil |
| Clientes | Ana Beatriz, Carlos Mendes, Sofia Rocha |

### Perfil C — Lanchonete "Sabor & Cia"

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

### Perfil D — Açougue "Boi Nobre"

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

### Perfil E — Mercado "SuperMax"

| Campo | Valor |
|-------|-------|
| Nome fantasia | SuperMax Mercado |
| CNPJ | 77.888.999/0001-55 |
| Celular | (61) 99666-5555 |
| Email | supermax@mercado.com.br |
| Cidade | Brasília - DF |
| Produtos típicos | Arroz 5kg, Feijão 1kg, Óleo 900ml, Café 500g, Leite 1L |
| Categorias | Alimentos, Bebidas, Limpeza, Higiene |
| Fornecedores | Alimentos Brasil, Bebidas DF, LimpMax |
| Clientes | Fernanda Lima, Ricardo Gomes, Juliana Costa |

---

## 2. Testes por Tela

---

### 2.1 ClienteScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Nome | Tipo Pessoa | CPF/CNPJ | Celular | Email | Efeito Esperado | Erro/Inconsistência |
|---|---------|------|-------------|----------|---------|-------|-----------------|---------------------|
| 1 | Cadastro válido - Pessoa Física | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria@email.com | Salvo com sucesso. Mostra na tabela. | |
| 2 | Cadastro válido - Pessoa Jurídica | Moda & Estilo Ltda | Juridica | 12.345.678/0001-90 | (11) 98765-4321 | contato@modaestilo.com.br | Salvo com sucesso. | |
| 3 | Campo Nome vazio | (vazio) | Fisica | 123.456.789-09 | (11) 98765-4321 | teste@email.com | Alerta de erro: "Nome é obrigatório". Não salva. | |
| 4 | Email inválido | João Pedro | Fisica | 987.654.321-00 | (11) 91234-5678 | email-invalido | Alerta de erro: "Email inválido". Não salva. | |
| 5 | Celular incompleto | Carla Lima | Fisica | 456.789.123-00 | (11) 9999-9999 | carla@email.com | Alerta de erro: "Celular inválido". Não salva. | |
| 6 | CPF repetido | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria2@email.com | Alerta de erro: "CPF já cadastrado". Não salva. | |
| 7 | Todos campos opcionais vazios | Pedro Alves | Fisica | 111.222.333-44 | (vazio) | (vazio) | Salvo com sucesso (nome é único obrigatório). | |
| 8 | Editar cliente existente | Maria Souza (alterar celular) | Fisica | 123.456.789-09 | (11) 99999-8888 | maria@email.com | Atualizado com sucesso. | |
| 9 | Excluir cliente com vendas | (cliente com vínculo) | - | - | - | - | Alerta: "Cliente possui vendas vinculadas". Não exclui. | |

#### Perfil B — PetShop Amigo

| # | Cenário | Nome | Tipo Pessoa | CPF/CNPJ | Celular | Email | Efeito Esperado | Erro/Inconsistência |
|---|---------|------|-------------|----------|---------|-------|-----------------|---------------------|
| 10 | Cadastro válido - Pessoa Jurídica | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 99876-5432 | contato@petshopamigo.com.br | Salvo com sucesso. | |
| 11 | Cadastro válido - Pessoa Física | Ana Beatriz | Fisica | 222.333.444-55 | (21) 99777-6666 | ana@email.com | Salvo com sucesso. | |
| 12 | CNPJ duplicado | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 91234-5678 | outro@email.com | Alerta: "CNPJ já cadastrado". | |

---

### 2.2 ProdutoScreen

#### Perfil A — Loja de Roupas

| # | Cenário | SKU | Descrição | Unid. | Marca | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Perecível | Validade | Efeito Esperado | Erro |
|---|---------|-----|-----------|-------|-------|-------------|-------------|-----------|-----------|---------|-----------|----------|-----------------|------|
| 13 | Cadastro válido produto simples | SKU001 | Camiseta Masculina M | UN | Malharia Silva | R$ 15,00 | R$ 39,90 | Masculino | Malharia Silva | 100 | Nao | - | Salvo com sucesso. | |
| 14 | Cadastro válido produto perecível | SKU002 | Vestido Feminino Algodão | UN | Tecidos ABC | R$ 35,00 | R$ 89,90 | Feminino | Tecidos ABC | 50 | Sim | 15/12/2026 | Salvo com sucesso. | |
| 15 | SKU duplicado | SKU001 | Jaqueta | UN | Avan Calçados | R$ 80,00 | R$ 199,90 | Masculino | Avan Calçados | 30 | Nao | - | Alerta: "Código de barras já cadastrado". | |
| 16 | Descrição vazia | SKU003 | (vazio) | UN | Generica | R$ 10,00 | R$ 25,00 | Acessórios | Malharia Silva | 20 | Nao | - | Alerta: "Descrição é obrigatória". | |
| 17 | SKU vazio | (vazio) | Produto sem SKU | UN | Generica | R$ 10,00 | R$ 25,00 | Acessórios | Malharia Silva | 20 | Nao | - | Alerta: "Código de barras é obrigatório". | |
| 18 | Preço compra maior que venda | SKU004 | Calça Jeans | UN | Tecidos ABC | R$ 100,00 | R$ 80,00 | Feminino | Tecidos ABC | 30 | Nao | - | Alerta de margem negativa, mas salva? (Validar comportamento). | |
| 19 | Perecível sem validade | SKU005 | Leite | UN | Marca X | R$ 3,00 | R$ 5,00 | Alimentos | Malharia Silva | 50 | Sim | (vazio) | Alerta: "Data de validade é obrigatória para produtos perecíveis". | |
| 20 | Excluir produto vinculado a venda | (produto com venda) | - | - | - | - | - | - | - | - | - | - | Alerta: "Produto possui vendas vinculadas". | |

#### Perfil C — Lanchonete

| # | Cenário | SKU | Descrição | Unid. | Preço Compra | Preço Venda | Categoria | Fornecedor | Estoque | Efeito Esperado | Erro |
|---|---------|-----|-----------|-------|-------------|-------------|-----------|-----------|---------|-----------------|------|
| 21 | Produto unitário | BEB001 | Refrigerante Lata 350ml | UN | R$ 2,50 | R$ 5,00 | Bebidas | RefriMax | 200 | Salvo com sucesso. | |
| 22 | Produto por kg | SAL001 | Coxinha (kg) | KG | R$ 12,00 | R$ 29,90 | Salgados | Distribuidora Horizonte | 10 | Salvo com sucesso. | |
| 23 | Produto ml | BEB002 | Suco Natural 500ml | ml | R$ 3,00 | R$ 7,00 | Bebidas | Distribuidora Horizonte | 30 | Salvo com sucesso. | |
| 24 | Fornecedor não selecionado | DOC001 | Pudim | UN | R$ 4,00 | R$ 9,00 | Doces | (nenhum) | 20 | Alerta: "Fornecedor é obrigatório". | |

#### Perfil D — Açougue

| # | Cenário | SKU | Descrição | Unid. | Preço Compra | Preço Venda | Categoria | Estoque | Efeito Esperado | Erro |
|---|---------|-----|-----------|-------|-------------|-------------|-----------|---------|-----------------|------|
| 25 | Produto kg | CAR001 | Picanha | KG | R$ 45,00 | R$ 79,90 | Bovinos | 50 | Salvo com sucesso. | |
| 26 | Produto kg aves | CAR002 | Coxa de Frango | KG | R$ 8,00 | R$ 14,90 | Aves | 80 | Salvo com sucesso. | |
| 27 | Produto em gramas | CAR003 | Bacon fatiado 500g | g | R$ 18,00 | R$ 35,00 | Embutidos | 30 | Salvo com sucesso com unidade "g". | |
| 28 | Unidade não selecionada | CAR004 | Linguiça | (vazio) | R$ 10,00 | R$ 19,90 | Embutidos | 40 | Alerta: "Unidade é obrigatória". | |
| 29 | Preço venda zerado | CAR005 | Alcatra | KG | R$ 35,00 | R$ 0,00 | Bovinos | 30 | Alerta: "Preço de venda deve ser maior que zero". | |

---

### 2.3 CategoriaScreen

| # | Cenário | Nome | Efeito Esperado | Erro |
|---|---------|------|-----------------|------|
| 30 | Cadastro válido - Loja Roupas | Masculino | Salvo com sucesso. | |
| 31 | Cadastro válido - PetShop | Cachorros | Salvo com sucesso. | |
| 32 | Cadastro válido - Lanchonete | Bebidas | Salvo com sucesso. | |
| 33 | Cadastro válido - Açougue | Bovinos | Salvo com sucesso. | |
| 34 | Cadastro válido - Mercado | Alimentos | Salvo com sucesso. | |
| 35 | Nome vazio | (vazio) | Alerta: "Nome é obrigatório". | |
| 36 | Nome duplicado | Masculino | Alerta: "Categoria já existe". | |
| 37 | Excluir categoria com produtos | (categoria vinculada) | Alerta: "Categoria possui produtos vinculados". | |
| 38 | Editar nome | Masculino -> Moda Masculina | Atualizado com sucesso. | |

---

### 2.4 FornecedorScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Nome Fantasia | CNPJ | Celular | IE | Email | UF | Cidade | Bairro | Rua | Número | Obs | Efeito Esperado | Erro |
|---|---------|--------------|------|---------|----|-------|----|--------|--------|-----|--------|-----|-----------------|------|
| 39 | Cadastro completo | Malharia Silva | 11.111.111/0001-11 | (11) 98888-7777 | 111.222.333.444 | silva@malharia.com | SP | São Paulo | Brás | Rua da Malharia | 123 | - | Salvo com sucesso. | |
| 40 | Cadastro mínimo obrigatório | Avan Calçados | 22.222.222/0001-22 | (11) 97777-6666 | - | - | SP | São Paulo | - | - | - | - | Salvo com sucesso (nome e CNPJ obrigatórios?). | |
| 41 | Nome vazio | (vazio) | 33.333.333/0001-33 | - | - | - | - | - | - | - | - | - | Alerta: "Nome é obrigatório". | |
| 42 | CNPJ duplicado | (CNPJ já existente) | 11.111.111/0001-11 | - | - | - | - | - | - | - | - | - | Alerta: "CNPJ já cadastrado". | |
| 43 | Editar dados do fornecedor | Malharia Silva (novo telefone) | - | (11) 99999-0000 | - | - | - | - | - | - | - | - | Atualizado com sucesso. | |

#### Perfil B — PetShop

| # | Cenário | Nome Fantasia | CNPJ | Celular | Email | Cidade | Efeito Esperado | Erro |
|---|---------|--------------|------|---------|-------|--------|-----------------|------|
| 44 | Cadastro válido | Purina Pet | 44.444.444/0001-44 | (21) 95555-4444 | purina@pet.com | Rio de Janeiro | Salvo com sucesso. | |
| 45 | Email inválido | Royal Canin | 55.555.555/0001-55 | (21) 94444-3333 | email-errado | Rio de Janeiro | Alerta: "Email inválido". | |

#### Perfil E — Mercado

| # | Cenário | Nome Fantasia | CNPJ | Celular | Cidade | Efeito Esperado | Erro |
|---|---------|--------------|------|---------|--------|-----------------|------|
| 46 | Cadastro válido | Alimentos Brasil | 66.666.666/0001-66 | (61) 93333-2222 | Brasília | Salvo com sucesso. | |

---

### 2.5 VendaMercadoriaScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Data | Cliente | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Obs | Efeito Esperado | Erro |
|---|---------|------|---------|----|---------|-----|-------|-------|-----------|---------|-----|-----------------|------|
| 47 | Venda à vista completa | 15/06/2026 | Maria Souza | NF001 | Camiseta M (SKU001) | 2 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Venda registrada. Estoque vai de 100 para 98. | |
| 48 | Venda com desconto | 15/06/2026 | João Pedro | NF002 | Calça Jeans (SKU004) | 1 | R$ 80,00 | R$ 10,00 | CREDITO | Sim | - | Venda registrada com total líquido R$ 70,00. | |
| 49 | Venda a prazo (parcelado) | 15/06/2026 | Carla Lima | - | Vestido (SKU002) | 1 | R$ 89,90 | R$ 0 | A PRAZO | Sim | - | Gera 3 parcelas no Contas a Receber. | |
| 50 | Produto sem estoque | 15/06/2026 | Maria Souza | - | Camiseta M | 500 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Alerta: "Estoque insuficiente". | |
| 51 | Cliente não selecionado | 15/06/2026 | (vazio) | - | Camiseta M | 1 | R$ 39,90 | R$ 0 | A VISTA | Sim | - | Alerta: "Selecione um cliente". | |
| 52 | Produto não encontrado | 15/06/2026 | Maria Souza | - | INEXISTENTE | 1 | - | - | - | - | - | Alerta: "Produto não encontrado". | |
| 53 | Venda sem refletir estoque | 15/06/2026 | Maria Souza | NF003 | Jaqueta (SKU003) | 1 | R$ 199,90 | R$ 0 | DEBITO | Nao | - | Venda registrada. Estoque permanece 30. | |
| 54 | Venda com observação | 15/06/2026 | João Pedro | NF004 | Camiseta M | 3 | R$ 39,90 | R$ 5,00 | PIX | Sim | "Presente" | Venda registrada com observação. | |

#### Perfil C — Lanchonete

| # | Cenário | Produto | Qtd | Preço | Pagamento | Estoque | Efeito Esperado | Erro |
|---|---------|---------|-----|-------|-----------|---------|-----------------|------|
| 55 | Venda balcão (padrão) | Coxinha KG | 0,5 | R$ 29,90/kg | A VISTA | Sim | Venda registrada R$ 14,95. Estoque 9,5 kg. | |
| 56 | Venda múltiplos itens | Refri Lata + Coxinha | 2 + 1 | R$ 5,00 + R$ 29,90 | DEBITO | Sim | Venda registrada total R$ 39,90. | |

#### Perfil D — Açougue

| # | Cenário | Data | Cliente | NF | Produto | Qtd (g) | Preço/g | Pagamento | Estoque | Efeito Esperado | Erro |
|---|---------|------|---------|----|---------|---------|---------|-----------|---------|-----------------|------|
| 57 | Venda de produto em gramas | 15/06/2026 | José Moura | NF-AC-001 | Bacon fatiado (CAR003) | 500 | R$ 0,07 | A VISTA | Sim | Venda registrada R$ 35,00 (500g x R$ 0,07/g). Estoque 5030 -> 4530 g. | |

---

### 2.6 ComprasScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Data | Fornecedor | NF | Produto | Qtd | Preço | Desc. | Pagamento | Estoque | Efeito Esperado | Erro |
|---|---------|------|-----------|----|---------|-----|-------|-------|-----------|---------|-----------------|------|
| 58 | Compra à vista | 14/06/2026 | Malharia Silva | NF-COM-001 | Camiseta M | 50 | R$ 15,00 | R$ 0 | A VISTA | Sim | Estoque 100 -> 150. Custo registrado. | |
| 59 | Compra com desconto | 14/06/2026 | Tecidos ABC | NF-COM-002 | Vestido | 20 | R$ 35,00 | R$ 50,00 | A PRAZO | Sim | Compra registrada. Gera contas a pagar. | |
| 60 | Não refletir no estoque | 14/06/2026 | Avan Calçados | - | Jaqueta | 10 | R$ 80,00 | R$ 0 | A VISTA | Nao | Compra registrada. Estoque não alterado. | |
| 61 | Fornecedor não selecionado | 14/06/2026 | (vazio) | - | Camiseta M | 10 | R$ 15,00 | - | A VISTA | Sim | Alerta: "Selecione um fornecedor". | |

#### Perfil D — Açougue

| # | Cenário | Fornecedor | Produto | Qtd | Unid. | Preço unit. | Pagamento | Efeito Esperado | Erro |
|---|---------|-----------|---------|-----|-------|------------|-----------|-----------------|------|
| 61 | Compra de carne em kg | Frigorífico Paraná | Picanha | 30 | KG | R$ 45,00 | A PRAZO | Compra registrada. Estoque 50 -> 80 kg. Gera contas a pagar. | |
| 62 | Compra de produto em gramas | Frigorífico Paraná | Bacon fatiado | 5000 | g | R$ 18,00 | A VISTA | Compra registrada em gramas. Estoque 30 -> 5030 g. | |

---

### 2.7 ComprasAPagarScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Descrição | Valor Original | Fornecedor | Status | Vencimento | Tipo Doc | Nº Doc | Observação | Efeito Esperado | Erro |
|---|---------|-----------|--------------|-----------|--------|------------|----------|--------|------------|-----------------|------|
| 63 | Cadastro manual de conta | Compra Tecidos ABC | R$ 700,00 | Tecidos ABC | PENDENTE | 15/07/2026 | DUPLICATA | NF-COM-002 | - | Salvo com sucesso. | |
| 64 | Pagamento parcial | (conta existente) | - | - | PARCIAL | - | - | - | - | Pagamento parcial registrado. Status atualizado. | |
| 65 | Pagamento total | (conta existente) | - | - | PAGO | - | - | - | - | Status alterado para PAGO. | |
| 66 | Descrição vazia | (vazio) | R$ 100,00 | Fornecedor X | PENDENTE | 15/07/2026 | - | - | - | Alerta: "Descrição é obrigatória". | |
| 67 | Valor zerado | Conta teste | R$ 0,00 | Fornecedor X | PENDENTE | 15/07/2026 | - | - | - | Alerta: "Valor deve ser maior que zero". | |
| 66 | Pagamento maior que restante | (conta R$ 100) | Pagamento: R$ 200 | - | - | - | - | - | - | Alerta: "Valor do pagamento excede o restante". | |

#### Perfil B — PetShop

| # | Cenário | Descrição | Valor | Fornecedor | Vencimento | Efeito Esperado | Erro |
|---|---------|-----------|-------|-----------|------------|-----------------|------|
| 69 | Conta gerada por compra | Compra Purina Pet | R$ 1.500,00 | Purina Pet | 15/08/2026 | Gerada automaticamente pela compra a prazo. | |

---

### 2.8 ContasAReceberScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Descrição | Valor Original | Cliente | Status | Vencimento | Tipo Doc | Efeito Esperado | Erro |
|---|---------|-----------|--------------|--------|--------|------------|----------|-----------------|------|
| 70 | Conta gerada por venda a prazo | Venda Carla Lima (Vestido) | R$ 89,90 | Carla Lima | PENDENTE | 15/07/2026 | - | Gerada automaticamente. 3 parcelas de ~R$ 29,97. | |
| 71 | Recebimento parcial | (conta existente) | Recebimento: R$ 50,00 | - | PARCIAL | - | - | Valor restante atualizado. | |
| 72 | Recebimento total | (conta existente) | Restante integral | - | RECEBIDO | - | - | Status alterado para RECEBIDO. | |
| 73 | Descrição vazia | (vazio) | R$ 200,00 | Maria Souza | PENDENTE | 15/08/2026 | - | Alerta: "Descrição é obrigatória". | |

#### Perfil C — Lanchonete

| # | Cenário | Descrição | Valor | Cliente | Status | Efeito Esperado | Erro |
|---|---------|-----------|-------|--------|--------|-----------------|------|
| 74 | Venda fiado no PDV | Venda fiado Pedro Alves | R$ 25,00 | Pedro Alves | PENDENTE | (automático) | Gera conta a receber automaticamente. | |

---

### 2.9 OrdemServicoScreen

#### Perfil A — Loja de Roupas (OS para máquina de costura)

| # | Cenário | Data | Cliente | Técnico | Equipamento | Mão de Obra | Peças | Pagamento | Status | Checklist | Efeito Esperado | Erro |
|---|---------|------|---------|---------|-------------|-------------|-------|-----------|--------|-----------|-----------------|------|
| 75 | OS completa | 15/06/2026 | Maria Souza | (técnico) | Máquina Costura Industrial | R$ 150,00 | R$ 80,00 | A VISTA | Aberto | "Trocar motor, lubrificar" | Salva com total R$ 230,00. | |
| 76 | OS com status Alterado | 16/06/2026 | João Pedro | (técnico) | Prensa estamparia | R$ 200,00 | R$ 50,00 | PIX | Finalizado | "Reparo concluído" | Status alterado. | |
| 77 | Cliente não selecionado | 15/06/2026 | (vazio) | (técnico) | Equipamento | R$ 100,00 | R$ 0 | A VISTA | Aberto | - | Alerta: "Selecione um cliente". | |
| 78 | Valor zerado total | 15/06/2026 | Maria Souza | (técnico) | Equipamento | R$ 0 | R$ 0 | - | Aberto | - | Alerta: "Informe valor de mão de obra ou peças". | |

---

### 2.10 TecnicoScreen

| # | Cenário | Nome | Efeito Esperado | Erro |
|---|---------|------|-----------------|------|
| 79 | Cadastro técnico - Loja Roupas | Carlos Mecânico | Salvo com sucesso. | |
| 80 | Cadastro técnico - OS Geral | Tecnico Padrao | Salvo com sucesso. | |
| 81 | Nome vazio | (vazio) | Alerta: "Nome é obrigatório". | |
| 82 | Nome duplicado | Carlos Mecânico | Alerta: "Técnico já cadastrado". | |
| 83 | Excluir técnico com OS | (técnico vinculado) | Alerta: "Técnico possui ordens de serviço vinculadas". | |

---

### 2.11 PDVScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Produto | Qtd | Total | Recebido | Fiado | Cliente (fiado) | Efeito Esperado | Erro |
|---|---------|---------|-----|-------|----------|-------|-----------------|-----------------|------|
| 84 | Venda avulsa (não fiada) | Camiseta M | 2 | R$ 79,80 | R$ 100,00 | Não | - | Troco: R$ 20,20. Pedido registrado. | |
| 85 | Venda fiada | Calça Jeans | 1 | R$ 80,00 | - | Sim | Maria Souza | Pedido registrado como fiado. Gera conta a receber. | |
| 86 | Venda fiada sem cliente | Jaqueta | 1 | R$ 199,90 | - | Sim | (vazio) | Alerta: "Selecione um cliente para venda fiada". | |
| 87 | Recebimento menor que total | Camiseta M | 3 | R$ 119,70 | R$ 50,00 | Não | - | Alerta: "Valor recebido é insuficiente". | |
| 88 | Carrinho vazio | (nenhum) | 0 | R$ 0 | - | - | - | Alerta: "Adicione produtos ao carrinho". | |

#### Perfil D — Açougue

| # | Cenário | Produto | Qtd | Unid. | Total | Recebido | Troco | Efeito Esperado | Erro |
|---|---------|---------|-----|-------|-------|----------|-------|-----------------|------|
| 89 | Venda produto em gramas | Bacon fatiado | 300 | g | R$ 21,00 | R$ 25,00 | R$ 4,00 | Venda registrada em gramas. Estoque 4530 -> 4230 g. | |

#### Perfil E — Mercado

| # | Cenário | Produto | Qtd | Total | Efeito Esperado | Erro |
|---|---------|---------|-----|-------|-----------------|------|
| 90 | Venda mercado | Arroz 5kg | 2 | R$ 20,00 | Venda registrada. Estoque atualizado. | |
| 91 | Venda múltiplos itens | Arroz + Feijão + Óleo | 1+2+1 | Total calculado | Venda registrada. | |

---

### 2.12 CadastroEmpresaScreen

#### Perfil A — Loja de Roupas

| # | Cenário | Nome | Celular | CEP | Cidade | Bairro | Rua | Local Pagamento | Resp. | Efeito Esperado | Erro |
|---|---------|------|---------|-----|--------|--------|-----|----------------|-------|-----------------|------|
| 93 | Cadastro completo | Moda & Estilo Ltda | (11) 98765-4321 | 01001000 | São Paulo | Centro | Rua da Moda | Pagável em qualquer banco | "Não aceitamos devoluções" | Salvo com sucesso. | |
| 94 | Nome vazio | (vazio) | - | - | - | - | - | - | - | Alerta: "Nome da empresa é obrigatório". | |

#### Perfil B — PetShop

| # | Cenário | Nome | Cidade | Efeito Esperado | Erro |
|---|---------|------|--------|-----------------|------|
| 95 | Cadastro PetShop | PetShop Amigo | Rio de Janeiro | Salvo com sucesso. | |

#### Perfil C — Lanchonete

| # | Cenário | Nome | Cidade | Local Pagamento | Efeito Esperado | Erro |
|---|---------|------|--------|----------------|-----------------|------|
| 96 | Cadastro Lanchonete | Sabor & Cia Lanches | Belo Horizonte | Pagável em qualquer lotérica | Salvo com sucesso. | |

#### Perfil D — Açougue

| # | Cenário | Nome | Cidade | Texto Responsabilidade | Efeito Esperado | Erro |
|---|---------|------|--------|----------------------|-----------------|------|
| 97 | Cadastro Açougue | Boi Nobre Carnes | Curitiba | "Vendemos somente carnes inspecionadas" | Salvo com sucesso. | |

#### Perfil E — Mercado

| # | Cenário | Nome | Cidade | Telefone | Efeito Esperado | Erro |
|---|---------|------|--------|---------|-----------------|------|
| 98 | Cadastro Mercado | SuperMax Mercado | Brasília | (61) 99666-5555 | Salvo com sucesso. | |

---

### 2.13 AuthScreen

| # | Cenário | Licença | Login | Senha | Efeito Esperado | Erro |
|---|---------|---------|-------|-------|-----------------|------|
| 99 | Login com credenciais corretas | (licença válida) | admin | admin123 | Acesso liberado. | |
| 100 | Licença inválida | LICENCA-INVALIDA | - | - | Alerta: "Licença inválida". | |
| 101 | Login sem credenciais (credenciais desabilitadas) | (licença válida) | - | - | Acesso liberado (pula tela de login). | |
| 102 | Senha incorreta | (licença válida) | admin | senha-errada | Alerta: "Login ou senha incorretos". | |

---

### 2.14 PreferenciasScreen

| # | Cenário | Habilitar Credenciais | Login | Senha | Efeito Esperado | Erro |
|---|---------|----------------------|-------|-------|-----------------|------|
| 103 | Habilitar credenciais | Sim | admin | admin123 | Salvo. AuthScreen passa a exigir login. | |
| 104 | Desabilitar credenciais | Nao | - | - | Salvo. AuthScreen é pulada. | |
| 105 | Habilitar com login vazio | Sim | (vazio) | admin123 | Alerta: "Login é obrigatório". | |
| 106 | Habilitar com senha vazia | Sim | admin | (vazio) | Alerta: "Senha é obrigatória". | |

---

## 3. Testes de Fluxo Completo (Extremos)

### Fluxo 1 — Loja de Roupas: Compra -> Venda -> Financeiro

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro |
|-------|------|------|-------|-----------------|------|
| 1 | Categoria | Criar "Masculino" | Nome: Masculino | OK | |
| 2 | Fornecedor | Criar "Malharia Silva" | CNPJ: 11.111.111/0001-11, SP | OK | |
| 3 | Produto | Criar "Camiseta M" | SKU001, R$ 15,00/R$ 39,90, UN, Est: 100 | OK | |
| 4 | Cliente | Criar "Maria Souza" | CPF: 123.456.789-09 | OK | |
| 5 | Compras | Comprar 50 Camisetas | Malharia Silva, R$ 15,00, A VISTA, refletir estoque | Estoque: 150 | |
| 6 | Vendas | Vender 3 Camisetas | Maria Souza, R$ 39,90, PIX, refletir estoque | Estoque: 147 | |
| 7 | PDV | Vender 2 Camisetas | Avulsa, R$ 79,80, R$ 100 recebido | Troco: R$ 20,20. Estoque: 145 | |
| 8 | Contas a Receber | Verificar | Nenhuma pendente (vendas à vista) | Vazio | |

### Fluxo 2 — Mercado: Compra a Prazo -> Pagamento

| Passo | Tela | Ação | Dados | Efeito Esperado | Erro |
|-------|------|------|-------|-----------------|------|
| 1 | Categoria | Criar "Alimentos" | Nome: Alimentos | OK | |
| 2 | Fornecedor | Criar "Alimentos Brasil" | CNPJ: 66.666.666/0001-66, DF | OK | |
| 3 | Produto | Criar "Arroz 5kg" | SKU-ARROZ, R$ 8,00/R$ 12,00, UN, Est: 200 | OK | |
| 4 | Compras | Comprar 100 Arroz a prazo | Alimentos Brasil, R$ 8,00, A PRAZO (3x) | Estoque: 300. Gera 3 contas a pagar. | |
| 5 | Contas a Pagar | Verificar | 3 parcelas geradas automaticamente | OK | |
| 6 | Contas a Pagar | Pagar 1ª parcela | Pagamento total da 1ª | Status: PARCIAL | |

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

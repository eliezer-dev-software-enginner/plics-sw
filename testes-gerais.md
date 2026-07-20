# Testes Gerais (não vinculados a perfil específico)

Testes de validação genérica e telas que não dependem de perfil de negócio.

---

## ClienteScreen

| # | Cenário | Nome | Tipo Pessoa | CPF/CNPJ | Celular | Email | Data Nasc. | Gestante | Bebê | CEP | UF | Cidade | Bairro | Rua | Número | Efeito Esperado | Erro/Inconsistência | Resultado |
|---|---------|------|-------------|----------|---------|-------|------------|----------|------|-----|----|--------|--------|-----|--------|-----------------|---------------------|-----------|
| 1 | Cadastro válido - Pessoa Física | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria@email.com | 15/03/1990 | Nao | - | 01001-000 | SP | São Paulo | Centro | Rua da Paz | 100 | Salvo com sucesso. Mostra na tabela. |                     | OK        |
| 2 | Cadastro válido - Pessoa Jurídica | Moda & Estilo Ltda | Juridica | 12.345.678/0001-95 | (11) 98765-4321 | contato@modaestilo.com.br | - | - | - | - | - | - | - | - | - | Salvo com sucesso. |                     | OK        |
| 3 | Campo Nome vazio | (vazio) | Fisica | 123.456.789-09 | (11) 98765-4321 | teste@email.com | - | Nao | - | - | - | - | - | - | - | Alerta de erro: "Nome é obrigatório". Não salva. |                     | OK        |
| 4 | Email inválido | João Pedro | Fisica | 987.654.321-00 | (11) 91234-5678 | email-invalido | - | Nao | - | - | - | - | - | - | - | Alerta de erro: "Email inválido". Não salva. |                     | OK        |
| 5 | CPF repetido | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria2@email.com | - | Nao | - | - | - | - | - | - | - | Alerta de erro: "CPF já cadastrado". Não salva. |                     | OK        |
| 6 | Todos campos opcionais vazios | Pedro Alves | Fisica | 111.222.333-44 | (vazio) | (vazio) | - | Nao | - | - | - | - | - | - | - | Salvo com sucesso (nome é único obrigatório). |                     | OK        |
| 7 | Editar cliente existente | Maria Souza (alterar celular) | Fisica | 123.456.789-09 | (11) 99999-8888 | maria@email.com | 15/03/1990 | Nao | - | 01001-000 | SP | São Paulo | Centro | Rua da Paz | 100 | Atualizado com sucesso. | | OK        |
| 8 | Excluir cliente com vendas | (cliente com vínculo) | - | - | - | - | - | - | - | - | - | - | - | - | - | Alerta: "Cliente possui vendas vinculadas". Não exclui. |                     |           |
| 9 | CNPJ duplicado | PetShop Amigo | Juridica | 98.765.432/0001-98 | (21) 91234-5678 | outro@email.com | - | - | - | - | - | - | - | - | - | Alerta: "CNPJ já cadastrado". |                     | OK        |
| 10 | Cliente gestante | Beatriz Gravidez | Fisica | 888.999.000-11 | (11) 97777-8888 | beatriz@email.com | 20/06/1995 | Sim | 15/10/2026 | 01234-567 | SP | São Paulo | Liberdade | Rua Galvão Bueno | 50 | Salvo com sucesso. Data nasc. bebê registrada. | |           |
| 11 | Gestante sem data do bebê | Ana Nascimentos | Fisica | 777.888.111-22 | (11) 96666-7777 | ana.n@email.com | 10/01/1993 | Sim | (vazio) | - | SP | São Paulo | - | - | - | Alerta: "Data de nascimento do bebê é obrigatória para gestantes". | |           |
| 12 | Endereço completo | Carlos Endereco | Fisica | 444.555.111-22 | (21) 95555-4444 | carlos.e@email.com | - | Nao | - | 22040-020 | RJ | Rio de Janeiro | Copacabana | Rua Barata Ribeiro | 1000 | Salvo com sucesso. Endereço completo registrado. | |           |
| 13 | Endereço com UF inválido | Pedro UF Errado | Fisica | 555.666.222-33 | (11) 94444-3333 | pedro.uf@email.com | - | Nao | - | 01001-000 | XX | São Paulo | Centro | Rua da Paz | 10 | Alerta: "UF inválida". | |           |

> **Dados de teste (cadastros válidos usados nas telas):** Os clientes abaixo devem ser cadastrados antes de executar os testes dos perfis. Todos testam a mesma regra de "cadastro válido PF/PJ" — basta cadastrar um deles e reutilizar o nome nos demais.
>
> | Nome | Tipo | CPF/CNPJ | Perfil |
> |------|------|----------|--------|
> | Maria Souza | Fisica | 123.456.789-09 | Loja Roupas |
> | João Pedro | Fisica | 987.654.321-01 | Loja Roupas |
> | Carla Lima | Fisica | 333.444.555-66 | Loja Roupas |
> | Ana Beatriz | Fisica | 222.333.444-55 | PetShop |
> | Carlos Mendes | Fisica | 444.555.666-77 | PetShop |
> | Sofia Rocha | Fisica | 555.666.777-89 | PetShop |
> | Pedro Alves | Fisica | 111.222.333-44 | Lanchonete |
> | José Moura | Fisica | 555.666.777-88 | Açougue |

---

## FornecedorScreen

| # | Cenário | Nome Fantasia | CNPJ | Celular | IE | Email | UF | Cidade | Bairro | Rua | Número | Obs | Efeito Esperado | Erro | Resultado |
|---|---------|--------------|------|---------|----|-------|----|--------|--------|-----|--------|-----|-----------------|------|-----------|
| 39 | Cadastro completo | Malharia Silva | 11.111.111/0001-91 | (11) 98888-7777 | 111.222.333.444 | silva@malharia.com | SP | São Paulo | Brás | Rua da Malharia | 123 | - | Salvo com sucesso. | | ok        |
| 40 | Cadastro mínimo obrigatório | Avan Calçados | 22.222.222/0001-91 | (11) 97777-6666 | - | - | SP | São Paulo | - | - | - | - | Salvo com sucesso (nome e CNPJ obrigatórios?). | | ok        |
| 41 | Nome vazio | (vazio) | 33.333.333/0001-91 | - | - | - | - | - | - | - | - | - | Alerta: "Nome é obrigatório". | | ok        |
| 42 | CNPJ duplicado | (CNPJ já existente) | 11.111.111/0001-91 | - | - | - | - | - | - | - | - | - | Alerta: "CNPJ já cadastrado". | | ok        |
| 43 | Editar dados do fornecedor | Malharia Silva (novo telefone) | - | (11) 99999-0000 | - | - | - | - | - | - | - | - | Atualizado com sucesso. | | ok        |
| 44 | Email inválido | Fornecedor Teste Email | 44.444.444/0001-91 | - | - | email-errado | RJ | Rio de Janeiro | - | - | - | - | Alerta: "Email inválido". | | ok        |
| 45 | Excluir fornecedor vinculado a produto | Malharia Silva | - | - | - | - | - | - | - | - | - | - | Alerta: "Fornecedor possui produtos vinculados". | |           |

> **Dados de teste (fornecedores usados nas telas):** Os fornecedores abaixo devem ser cadastrados antes de executar os testes dos perfis. Todos testam a mesma regra de "cadastro válido" — basta cadastrar um deles e reutilizar o nome nos demais.
>
> | Nome | CNPJ | Perfil |
> |------|------|--------|
> | Malharia Silva | 11.111.111/0001-91 | Loja Roupas |
> | Avan Calçados | 22.222.222/0001-91 | Loja Roupas |
> | Tecidos ABC | 77.777.777/0001-91 | Loja Roupas |
> | Purina Pet | 44.444.444/0001-91 | PetShop |
> | Royal Canin | 55.555.555/0001-91 | PetShop |
> | PetBrasil | 88.888.888/0001-91 | PetShop |
> | Distribuidora Horizonte | 99.999.999/0001-91 | Lanchonete |
> | RefriMax | 10.111.111/0001-29 | Lanchonete |
> | Frigorífico Paraná | 12.222.222/0001-38 | Açougue |
> | Alimentos Brasil | 66.666.666/0001-91 | Mercado |

---

## CategoriaScreen

| #  | Cenário                        | Nome | Efeito Esperado | Erro | Resultado                                                    |
|----|--------------------------------|------|-----------------|------|--------------------------------------------------------------|
| 33 | Nome vazio                     | (vazio) | Alerta: "Nome é obrigatório". |      | OK                                                           |
| 34 | Excluir categoria com produtos | (categoria vinculada) | Alerta: "Categoria possui produtos vinculados". |      |                                                              |
| 35 | Editar nome                    | Masculino -> Moda Masculina | Atualizado com sucesso. | OK   | Fix: race condition no modoEdicao dentro de Async.Run |
| 36 | Nome duplicado                 | Masculino | Alerta: "Categoria já existe". | OK    |                                                              |

---

## TecnicoScreen

| #  | Cenário                        | Nome | Efeito Esperado                               | Erro | Resultado |
|----|--------------------------------|--|-----------------------------------------------|------|-----------|
| 79 | Cadastro técnico - Loja Roupas | Carlos Mecânico | Salvo com sucesso.                            | | ok        |
| 80 | Cadastro técnico - OS Geral    | Tecnico Padrao | Salvo com sucesso.                            | |           |
| 81 | Nome vazio                     | (vazio) | Alerta: "Nome é obrigatório".                 | | ok        |
| 82 | Nome duplicado                 | Carlos Mecânico | Alerta: "Técnico já cadastrado".              | | ok        |
| 83 | Excluir técnico com OS         | (técnico vinculado) | Alerta: "Técnico possui ordens de serviço vinculadas". | |           |
| 84 | Excluir técnico sem OS          |  |Excluido com sucesso.                          | | ok        |

---

## AuthScreen

| # | Cenário | Licença | Login | Senha | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-------|-------|-----------------|------|---|
| 99 | Login com credenciais corretas | (licença válida) | admin | admin123 | Acesso liberado. | | |
| 100 | Licença inválida | LICENCA-INVALIDA | - | - | Alerta: "Licença inválida". | | |
| 101 | Login sem credenciais (credenciais desabilitadas) | (licença válida) | - | - | Acesso liberado (pula tela de login). | | |
| 102 | Senha incorreta | (licença válida) | admin | senha-errada | Alerta: "Login ou senha incorretos". | | |

---

## PreferenciasScreen

| # | Cenário | Habilitar Credenciais | Select Impressora | Login | Senha | Efeito Esperado | Erro                                               | Resultado |
|---|---------|----------------------|-------------------|-------|-------|-----------------|----------------------------------------------------|-----------|
| 103 | Habilitar credenciais | Sim | - | admin | admin123 | Salvo. AuthScreen passa a exigir login. |                                                    | ok        |
| 104 | Desabilitar credenciais | Nao | - | - | - | Salvo. AuthScreen é pulada. |                                                    | ok         |
| 105 | Habilitar com login vazio | Sim | - | (vazio) | admin123 | Alerta: "Login é obrigatório". | Login não era validado. Deixava salvar normalmente. | OK (fix: validar() em PreferenciasViewModel) |
| 106 | Habilitar com senha vazia | Sim | - | admin | (vazio) | Alerta: "Senha é obrigatória". | Senha não era validada. Deixava salvar normalmente. | OK (fix: validar() em PreferenciasViewModel) |
| 107 | Selecionar impressora | Nao | Impressora Padrão - Spooler | - | - | Impressora selecionada salva nas preferências. | | |
| 108 | Selecionar porta serial | Nao | COM3 - Serial | - | - | Porta serial selecionada salva. | | |

---

## PedidosScreen (Histórico de Vendas PDV)

| # | Cenário | Efeito Esperado | Erro | Resultado |
|---|---------|-----------------|------|-----------|
| 109 | Visualizar pedidos existentes | Lista exibe pedidos com ID, Cliente, Total, Pagamento, Data. | | |
| 110 | Filtrar pedidos por busca | Busca por nome do cliente filtra a lista. | | |
| 111 | Visualizar itens de um pedido | Duplo clique no pedido exibe itens com Produto, Qtd, Vl. Unit., Total. | | |
| 112 | Lista de pedidos vazia | Quando não há vendas PDV, tabela exibe "Nenhum pedido encontrado". | | |

---

## SugerirMelhoriaScreen

| # | Cenário | Texto (max 300) | Efeito Esperado | Erro | Resultado |
|---|---------|-----------------|-----------------|------|-----------|
| 113 | Sugestão válida | "Seria útil ter um relatório de vendas mensal" | Enviado com sucesso via Telegram. | | |
| 114 | Texto vazio | (vazio) | Botão de envio desabilitado ou alerta. | | |
| 115 | Texto excede 300 caracteres | (texto muito longo) | Texto truncado ou alerta de limite. | | |

---

## RelatarErroScreen

| # | Cenário | Texto (max 300) | Efeito Esperado | Erro | Resultado |
|---|---------|-----------------|-----------------|------|-----------|
| 116 | Relato válido | "Erro ao salvar produto com preço zero" | Enviado com sucesso via Telegram. | | ok        |
| 117 | Texto vazio | (vazio) | Botão de envio desabilitado ou alerta. | | ok        |
| 118 | Texto excede 300 caracteres | (texto muito longo) | Texto truncado ou alerta de limite. | | ok        |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
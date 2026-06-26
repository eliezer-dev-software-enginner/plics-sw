# Testes Gerais (não vinculados a perfil específico)

Testes de validação genérica e telas que não dependem de perfil de negócio.

---

## ClienteScreen

| # | Cenário | Nome | Tipo Pessoa | CPF/CNPJ | Celular | Email | Efeito Esperado | Erro/Inconsistência | Resultado |
|---|---------|------|-------------|----------|---------|-------|-----------------|---------------------|-----------|
| 1 | Cadastro válido - Pessoa Física | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria@email.com | Salvo com sucesso. Mostra na tabela. |                     | OK        |
| 2 | Cadastro válido - Pessoa Jurídica | Moda & Estilo Ltda | Juridica | 12.345.678/0001-90 | (11) 98765-4321 | contato@modaestilo.com.br | Salvo com sucesso. |                     | OK        |
| 3 | Campo Nome vazio | (vazio) | Fisica | 123.456.789-09 | (11) 98765-4321 | teste@email.com | Alerta de erro: "Nome é obrigatório". Não salva. |                     | OK        |
| 4 | Email inválido | João Pedro | Fisica | 987.654.321-00 | (11) 91234-5678 | email-invalido | Alerta de erro: "Email inválido". Não salva. |                     | OK        |
| 5 | CPF repetido | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria2@email.com | Alerta de erro: "CPF já cadastrado". Não salva. |                     | OK        |
| 6 | Todos campos opcionais vazios | Pedro Alves | Fisica | 111.222.333-44 | (vazio) | (vazio) | Salvo com sucesso (nome é único obrigatório). |                     | OK        |
| 7 | Editar cliente existente | Maria Souza (alterar celular) | Fisica | 123.456.789-09 | (11) 99999-8888 | maria@email.com | Atualizado com sucesso. | | OK        |
| 8 | Excluir cliente com vendas | (cliente com vínculo) | - | - | - | - | Alerta: "Cliente possui vendas vinculadas". Não exclui. |                     |           |
| 9 | Cadastro válido - Pessoa Jurídica (PetShop) | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 99876-5432 | contato@petshopamigo.com.br | Salvo com sucesso. |                     | OK        |
| 10 | Cadastro válido - Pessoa Física (PetShop) | Ana Beatriz | Fisica | 222.333.444-55 | (21) 99777-6666 | ana@email.com | Salvo com sucesso. |                     | OK        |
| 11 | CNPJ duplicado | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 91234-5678 | outro@email.com | Alerta: "CNPJ já cadastrado". |                     | OK         |

---

## FornecedorScreen

| # | Cenário | Nome Fantasia | CNPJ | Celular | IE | Email | UF | Cidade | Bairro | Rua | Número | Obs | Efeito Esperado | Erro | Resultado |
|---|---------|--------------|------|---------|----|-------|----|--------|--------|-----|--------|-----|-----------------|------|---|
| 39 | Cadastro completo (Loja Roupas) | Malharia Silva | 11.111.111/0001-11 | (11) 98888-7777 | 111.222.333.444 | silva@malharia.com | SP | São Paulo | Brás | Rua da Malharia | 123 | - | Salvo com sucesso. | | |
| 40 | Cadastro mínimo obrigatório (Loja Roupas) | Avan Calçados | 22.222.222/0001-22 | (11) 97777-6666 | - | - | SP | São Paulo | - | - | - | - | Salvo com sucesso (nome e CNPJ obrigatórios?). | | |
| 41 | Nome vazio | (vazio) | 33.333.333/0001-33 | - | - | - | - | - | - | - | - | - | Alerta: "Nome é obrigatório". | | |
| 42 | CNPJ duplicado | (CNPJ já existente) | 11.111.111/0001-11 | - | - | - | - | - | - | - | - | - | Alerta: "CNPJ já cadastrado". | | |
| 43 | Editar dados do fornecedor | Malharia Silva (novo telefone) | - | (11) 99999-0000 | - | - | - | - | - | - | - | - | Atualizado com sucesso. | | |
| 44 | Cadastro válido (PetShop) | Purina Pet | 44.444.444/0001-44 | (21) 95555-4444 | - | purina@pet.com | RJ | Rio de Janeiro | - | - | - | - | Salvo com sucesso. | | |
| 45 | Email inválido (PetShop) | Royal Canin | 55.555.555/0001-55 | (21) 94444-3333 | - | email-errado | RJ | Rio de Janeiro | - | - | - | - | Alerta: "Email inválido". | | |
| 46 | Cadastro válido (Mercado) | Alimentos Brasil | 66.666.666/0001-66 | (61) 93333-2222 | - | - | DF | Brasília | - | - | - | - | Salvo com sucesso. | | |
| 47 | Cadastro válido (Loja Roupas) | Tecidos ABC | 77.777.777/0001-77 | (11) 96666-5555 | - | tecidos@abc.com | SP | São Paulo | - | - | - | - | Salvo com sucesso. | | |
| 48 | Cadastro válido (PetShop) | PetBrasil | 88.888.888/0001-88 | (21) 93333-2222 | - | pet@brasil.com | RJ | Rio de Janeiro | - | - | - | - | Salvo com sucesso. | | |
| 49 | Cadastro válido (Lanchonete) | Distribuidora Horizonte | 99.999.999/0001-99 | (61) 92222-1111 | - | horizonte@dist.com | DF | Brasília | - | - | - | - | Salvo com sucesso. | | |
| 50 | Cadastro válido (Lanchonete) | RefriMax | 10.111.111/0001-11 | (61) 91111-0000 | - | refri@max.com | DF | Brasília | - | - | - | - | Salvo com sucesso. | | |
| 51 | Cadastro válido (Açougue) | Frigorífico Paraná | 12.222.222/0001-22 | (41) 90000-1111 | - | frig@parana.com | PR | Curitiba | - | - | - | - | Salvo com sucesso. | | |

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

| # | Cenário | Habilitar Credenciais | Login | Senha | Efeito Esperado | Erro                                               | Resultado |
|---|---------|----------------------|-------|-------|-----------------|----------------------------------------------------|-----------|
| 103 | Habilitar credenciais | Sim | admin | admin123 | Salvo. AuthScreen passa a exigir login. |                                                    | ok        |
| 104 | Desabilitar credenciais | Nao | - | - | Salvo. AuthScreen é pulada. |                                                    | ok         |
| 105 | Habilitar com login vazio | Sim | (vazio) | admin123 | Alerta: "Login é obrigatório". | Login não era validado. Deixava salvar normalmente. | OK (fix: validar() em PreferenciasViewModel) |
| 106 | Habilitar com senha vazia | Sim | admin | (vazio) | Alerta: "Senha é obrigatória". | Senha não era validada. Deixava salvar normalmente. | OK (fix: validar() em PreferenciasViewModel) |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
# Testes Gerais (não vinculados a perfil específico)

Testes de validação genérica e telas que não dependem de perfil de negócio.

---

## ClienteScreen

| # | Cenário | Nome | Tipo Pessoa | CPF/CNPJ | Celular | Email | Efeito Esperado | Erro/Inconsistência | Resultado |
|---|---------|------|-------------|----------|---------|-------|-----------------|---------------------|---|
| 1 | Cadastro válido - Pessoa Física | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria@email.com | Salvo com sucesso. Mostra na tabela. | | |
| 2 | Cadastro válido - Pessoa Jurídica | Moda & Estilo Ltda | Juridica | 12.345.678/0001-90 | (11) 98765-4321 | contato@modaestilo.com.br | Salvo com sucesso. | | |
| 3 | Campo Nome vazio | (vazio) | Fisica | 123.456.789-09 | (11) 98765-4321 | teste@email.com | Alerta de erro: "Nome é obrigatório". Não salva. | | |
| 4 | Email inválido | João Pedro | Fisica | 987.654.321-00 | (11) 91234-5678 | email-invalido | Alerta de erro: "Email inválido". Não salva. | | |
| 5 | Celular incompleto | Carla Lima | Fisica | 456.789.123-00 | (11) 9999-9999 | carla@email.com | Alerta de erro: "Celular inválido". Não salva. | | |
| 6 | CPF repetido | Maria Souza | Fisica | 123.456.789-09 | (11) 98765-4321 | maria2@email.com | Alerta de erro: "CPF já cadastrado". Não salva. | | |
| 7 | Todos campos opcionais vazios | Pedro Alves | Fisica | 111.222.333-44 | (vazio) | (vazio) | Salvo com sucesso (nome é único obrigatório). | | |
| 8 | Editar cliente existente | Maria Souza (alterar celular) | Fisica | 123.456.789-09 | (11) 99999-8888 | maria@email.com | Atualizado com sucesso. | | |
| 9 | Excluir cliente com vendas | (cliente com vínculo) | - | - | - | - | Alerta: "Cliente possui vendas vinculadas". Não exclui. | | |
| 10 | Cadastro válido - Pessoa Jurídica (PetShop) | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 99876-5432 | contato@petshopamigo.com.br | Salvo com sucesso. | | |
| 11 | Cadastro válido - Pessoa Física (PetShop) | Ana Beatriz | Fisica | 222.333.444-55 | (21) 99777-6666 | ana@email.com | Salvo com sucesso. | | |
| 12 | CNPJ duplicado | PetShop Amigo | Juridica | 98.765.432/0001-10 | (21) 91234-5678 | outro@email.com | Alerta: "CNPJ já cadastrado". | | |

---

## CategoriaScreen

| # | Cenário | Nome | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------------|------|---|
| 33 | Cadastro válido - Açougue | Bovinos | Salvo com sucesso. | | |
| 35 | Nome vazio | (vazio) | Alerta: "Nome é obrigatório". | | |
| 36 | Nome duplicado | Masculino | Alerta: "Categoria já existe". | | |
| 37 | Excluir categoria com produtos | (categoria vinculada) | Alerta: "Categoria possui produtos vinculados". | | |
| 38 | Editar nome | Masculino -> Moda Masculina | Atualizado com sucesso. | | |

---

## TecnicoScreen

| # | Cenário | Nome | Efeito Esperado | Erro | Resultado |
|---|---------|------|-----------------|------|-----------|
| 79 | Cadastro técnico - Loja Roupas | Carlos Mecânico | Salvo com sucesso. | | ok        |
| 80 | Cadastro técnico - OS Geral | Tecnico Padrao | Salvo com sucesso. | |           |
| 81 | Nome vazio | (vazio) | Alerta: "Nome é obrigatório". | | ok        |
| 82 | Nome duplicado | Carlos Mecânico | Alerta: "Técnico já cadastrado". | | ok         |
| 83 | Excluir técnico com OS | (técnico vinculado) | Alerta: "Técnico possui ordens de serviço vinculadas". | |           |

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

| # | Cenário | Habilitar Credenciais | Login | Senha | Efeito Esperado | Erro | Resultado |
|---|---------|----------------------|-------|-------|-----------------|------|---|
| 103 | Habilitar credenciais | Sim | admin | admin123 | Salvo. AuthScreen passa a exigir login. | | |
| 104 | Desabilitar credenciais | Nao | - | - | Salvo. AuthScreen é pulada. | | |
| 105 | Habilitar com login vazio | Sim | (vazio) | admin123 | Alerta: "Login é obrigatório". | | |
| 106 | Habilitar com senha vazia | Sim | admin | (vazio) | Alerta: "Senha é obrigatória". | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
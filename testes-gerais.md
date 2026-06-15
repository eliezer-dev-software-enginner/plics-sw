# Testes Gerais (não vinculados a perfil específico)

Testes de validação genérica e telas que não dependem de perfil de negócio.

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
|---|---------|------|-----------------|------|---|
| 79 | Cadastro técnico - Loja Roupas | Carlos Mecânico | Salvo com sucesso. | | |
| 80 | Cadastro técnico - OS Geral | Tecnico Padrao | Salvo com sucesso. | | |
| 81 | Nome vazio | (vazio) | Alerta: "Nome é obrigatório". | | |
| 82 | Nome duplicado | Carlos Mecânico | Alerta: "Técnico já cadastrado". | | |
| 83 | Excluir técnico com OS | (técnico vinculado) | Alerta: "Técnico possui ordens de serviço vinculadas". | | |

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
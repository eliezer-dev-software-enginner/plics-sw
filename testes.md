# Testes do Sistema Plics SW

## Como usar este documento

Os testes foram organizados por perfil de negócio em arquivos separados.
Cada arquivo contém os testes específicos daquele perfil, incluindo dados de entrada, efeito esperado e campo para anotar erros/inconsistências.

---

## Arquivos de Teste

| Perfil | Arquivo | Fluxo Completo |
|--------|---------|---------------|
| Loja de Roupas "Moda & Estilo" | [`testes-loja-de-roupas.md`](testes-loja-de-roupas.md) | Venda -> Financeiro |
| PetShop "PetShop Amigo" | [`testes-petshop.md`](testes-petshop.md) | Venda |
| Lanchonete "Sabor & Cia" | [`testes-lanchonete.md`](testes-lanchonete.md) | - |
| Açougue "Boi Nobre" | [`testes-acougue.md`](testes-acougue.md) | Venda |
| Mercado "SuperMax" | [`testes-mercado.md`](testes-mercado.md) | Cadastros base |
| Testes Gerais (validação, Auth, Prefs) | [`testes-gerais.md`](testes-gerais.md) | - |

---

## Screens Cobertas

1. [ClienteScreen](testes-gerais.md#clientescreen) (tests genéricos)
2. [ProdutoScreen](testes-loja-de-roupas.md#produtoscreen) (Loja Roupas) / [PetShop](testes-petshop.md#produtoscreen) / [Lanchonete](testes-lanchonete.md#produtoscreen) / [Açougue](testes-acougue.md#produtoscreen)
3. [CategoriaScreen](testes-gerais.md#categoriascreen) (validação genérica)
4. [FornecedorScreen](testes-gerais.md#fornecedorscreen) (validação genérica)
5. [VendaMercadoriaScreen](testes-loja-de-roupas.md#vendamercadoriascreen) (Loja Roupas) / [PetShop](testes-petshop.md#vendamercadoriascreen) / [Lanchonete](testes-lanchonete.md#vendamercadoriascreen) / [Açougue](testes-acougue.md#vendamercadoriascreen)
6. [ContasAReceberScreen](testes-loja-de-roupas.md#contasareceberscreen) (Loja Roupas) / [PetShop](testes-petshop.md#contasareceberscreen) / [Lanchonete](testes-lanchonete.md#contasareceberscreen)
7. [PDVScreen](testes-loja-de-roupas.md#pdvscreen) (Loja Roupas) / [PetShop](testes-petshop.md#pdvscreen) / [Açougue](testes-acougue.md#pdvscreen) / [Mercado](testes-mercado.md#pdvscreen)
8. [CadastroEmpresaScreen](testes-loja-de-roupas.md#cadastroempresascreen) (Loja Roupas) / [PetShop](testes-petshop.md#cadastroempresascreen) / [Lanchonete](testes-lanchonete.md#cadastroempresascreen) / [Açougue](testes-acougue.md#cadastroempresascreen) / [Mercado](testes-mercado.md#cadastroempresascreen)
9. [AuthScreen](testes-gerais.md#authscreen)
10. [PreferenciasScreen](testes-gerais.md#preferenciascreen)
11. [PedidosScreen](testes-gerais.md#pedidosscreen-histórico-de-vendas-pdv)
12. [SugerirMelhoriaScreen](testes-gerais.md#sugerirmelhoriascreen)
13. [RelatarErroScreen](testes-gerais.md#relatarerroscreen)
14. [RelatoriosScreen](testes-gerais.md#relatoriosscreen)
15. [Fluxo Loja de Roupas](testes-loja-de-roupas.md#fluxo-1--loja-de-roupas-venda---financeiro)
16. [Fluxo Mercado](testes-mercado.md#fluxo-2--mercado-cadastros-base)
17. [Fluxo Açougue](testes-acougue.md#fluxo-3--açougue-venda)
18. [Fluxo PetShop](testes-petshop.md#fluxo-4--petshop-venda)

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada

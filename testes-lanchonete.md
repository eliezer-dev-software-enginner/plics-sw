# Testes — Perfil Lanchonete "Sabor & Cia"

## Perfil do Negócio

| Campo | Valor | |
|-------|-------|---|
| Nome fantasia | Sabor & Cia Lanches | |
| CNPJ | 11.222.333/0001-81 | |
| Celular | (31) 99988-7766 | |
| Email | saborecia@email.com.br | |
| Cidade | Belo Horizonte - MG | |
| Produtos típicos | Pão de queijo, Coxinha, Refrigerante, Suco natural | |
| Categorias | Salgados, Bebidas, Doces, Porções | |
| Fornecedores | Distribuidora Horizonte, RefriMax | |
| Clientes | Pedro Alves | |

---

## ProdutoScreen

| # | Cenário | SKU | Descrição | Unid. | Marca | Cor | Tamanho | Modelo | Preço Compra | Preço Venda | Frete | Categoria | Fornecedor | Estoque | Est. Mínimo | Perecível | Validade | Garantia | Devolução | Observações | Imagem | Efeito Esperado | Erro | Resultado |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 21 | Produto unitário | BEB001 | Refrigerante Lata 350ml | UN | RefriMax | - | - | - | R$ 2,50 | R$ 5,00 | R$ 0,00 | Bebidas | RefriMax | 200 | 30 | Nao | - | - | Não | Refrigerante cola 350ml lata | - | Salvo com sucesso. |  |  |
| 22 | Produto por kg | SAL001 | Coxinha (kg) | KG | Generica | - | - | - | R$ 12,00 | R$ 29,90 | R$ 0,00 | Salgados | Distribuidora Horizonte | 10 | 3 | Sim | 20/08/2026 | - | Não | Coxinha de frango caseira | - | Salvo com sucesso. |  |  |
| 23 | Produto ml | BEB002 | Suco Natural 500ml | ml | Generica | - | - | - | R$ 3,00 | R$ 7,00 | R$ 0,00 | Bebidas | Distribuidora Horizonte | 30 | 5 | Sim | 25/07/2026 | - | Não | Suco natural sem conservantes | - | Salvo com sucesso. |  |  |
| 24 | Fornecedor não selecionado | DOC001 | Pudim | UN | Generica | - | - | - | R$ 4,00 | R$ 9,00 | R$ 0,00 | Doces | (nenhum) | 20 | 5 | Nao | - | - | Não | - | - | Alerta: "Fornecedor é obrigatório". |  |  |
| 125 | Produto com imagem | BEB003 | Café Especial 250g | g | RefriMax | - | - | - | R$ 8,00 | R$ 18,00 | R$ 0,00 | Bebidas | RefriMax | 50 | 10 | Nao | - | - | Não | Café torrado e moído | (arquivo jpg) | Salvo com sucesso. Imagem registrada. |  |  |
| 162 | Produto perecível sem validade | SAL002 | Coxinha de Frango Fresca | UN | Generica | - | - | - | R$ 3,50 | R$ 8,00 | R$ 0,00 | Salgados | Distribuidora Horizonte | 40 | 10 | Sim | (vazio) | - | Não | - | - | Alerta: "Data de validade é obrigatória para produtos perecíveis". |  |  |
| 184 | Produto com frete e aceita troca | BEB004 | Kit Copo Térmico | UN | RefriMax | - | - | - | R$ 20,00 | R$ 49,90 | R$ 5,00 | Bebidas | RefriMax | 15 | 3 | Nao | - | 6 meses | Sim | Aceita troca em até 7 dias, produto lacrado | - | Salvo com sucesso. Ganho líquido estimado descontado o frete. |  |  |

---


---

## VendaMercadoriaScreen

| # | Cenário | Produto | Qtd | Preço | Pagamento | Estoque | Efeito Esperado | Erro | Resultado |
|---|---------|---------|-----|-------|-----------|---------|-----------------|------|---|
| 55 | Venda balcão (padrão) | Coxinha KG | 0,5 | R$ 29,90/kg | A VISTA | Sim | Venda registrada R$ 14,95. Estoque 9,5 kg. | | |
| 56 | Venda múltiplos itens | Refri Lata + Coxinha | 2 + 1 | R$ 5,00 + R$ 29,90 | DEBITO | Sim | Venda registrada total R$ 39,90. | | |
| 195 | Venda com frete (entrega via app) | Coxinha KG | 0,5 | R$ 29,90/kg | A VISTA | Sim | Campo "Frete" preenchido com R$ 6,00. Linha "Total com frete" exibe R$ 20,95 (R$ 14,95 + R$ 6,00). Estoque desconta só os 0,5 kg vendidos, frete não afeta estoque. | | |
| 196 | Devolver venda (ex: venda #55) | Coxinha KG | - | - | - | - | Botão "Devolver venda" no detalhe. Estoque devolvido (9,5 -> 10 kg); coluna "Status" = "Devolvida"; venda permanece na lista. | | |

---

## ContasAReceberScreen

| # | Cenário | Descrição | Valor | Cliente | Status | Efeito Esperado | Erro | Resultado |
|---|---------|-----------|-------|--------|--------|-----------------|------|---|
| 74 | Venda fiado no PDV | Venda fiado Pedro Alves | R$ 25,00 | Pedro Alves | PENDENTE | Gerada automaticamente. Gera conta a receber. | | |

---

## CadastroEmpresaScreen

| # | Cenário | Nome | Cidade | Local Pagamento | Efeito Esperado | Erro | Resultado |
|---|---------|------|--------|----------------|-----------------|------|---|
| 96 | Cadastro Lanchonete | Sabor & Cia Lanches | Belo Horizonte | Pagável em qualquer lotérica | Salvo com sucesso. | | |

---

## Legenda

- **OK**: Funcionou conforme esperado
- **PENDENTE**: Aguardando teste
- **ERRO**: Comportamento inesperado (detalhar na coluna)
- **MELHORIA**: Sugestão de melhoria identificada
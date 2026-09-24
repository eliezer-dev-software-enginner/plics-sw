# Templates de exportação

As tabelas das telas baseadas em `ScreenContract` são exportadas por um motor orientado a dados.
O Java recebe apenas um snapshot com cabeçalhos/linhas e um mapa de variáveis; formato e layout
ficam nos arquivos JSONC de `src/main/resources/export-templates/`.

## PDF

`table.pdf.jsonc` define:

- `page`: tamanho (`A4`, `A3` ou `LETTER`), orientação e margem;
- `fonts`: recursos TTF regular e negrito;
- `content`: sequência de comandos interpretados na ordem;
- `table`: tamanhos, espaçamento, repetição do preâmbulo e colunas excluídas.

Comandos disponíveis em `content`:

```jsonc
{ "type": "TEXT", "align": "LEFT", "value": "${NOME_EMPRESA}", "fontSize": 16, "bold": true, "spacingAfter": 5 }
{ "type": "BREAK", "lines": 2 }
{ "type": "HORIZONTAL_LINE", "spacingAfter": 10 }
{ "type": "TABLE" }
```

`TEXT` aceita alinhamento `LEFT`, `CENTER` ou `RIGHT`. Deve existir exatamente um comando `TABLE`.
Os comandos anteriores a ele formam o preâmbulo que pode ser repetido em cada página; os
posteriores formam o epílogo.

Placeholders fornecidos atualmente pelo adaptador do Plics:

- `${NOME_EMPRESA}`
- `${DOCUMENTO_EMPRESA}`
- `${TELEFONE}`
- `${ENDERECO}`
- `${TITULO_TABELA}`
- `${GERADO_EM}`

Placeholder sem valor é substituído por texto vazio. Novas variáveis não exigem alterar o parser:
basta incluí-las no mapa montado por `TableExportActions` e referenciá-las no JSONC.

## CSV

`table.csv.jsonc` define charset, delimitador, BOM, inclusão de cabeçalho, uso obrigatório de
aspas e sequência de quebra de linha. O renderer aplica escape de aspas, delimitador e quebras
contidas nos valores.

## Fronteiras e evolução

- `SimpleTable`: somente extrai os dados visíveis conceitualmente (todas as linhas filtradas, antes
  da paginação); não conhece formato de arquivo.
- `TableExportEngine`: parser/renderizador genérico; não conhece JavaFX, banco ou models.
- `TableExportActions`: adaptador do app para seletor de arquivo e dados da empresa.

A evolução prevista é adicionar comandos específicos de papel térmico/ESC-POS, migrar as notas
hoje construídas em `EscPosPrinter` e, com o contrato estabilizado, extrair o motor para uma
biblioteca Megalodonte separada.

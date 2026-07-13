# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico
- **Updater embutido no mesmo JAR** via `--add-launcher`: `my_app.updater.Main` (não é projeto separado)

## Fluxo de atualização
```
 Plics SW instalado (MSI ou DEB)
       │
       └─ Menu "Suporte" > "Buscar atualização"
              │
              ├─ [args.length >= 2] ── Usa updaterPath + pkgPath fornecidos via CLI
              │
              └─ [args.length < 2] ── Produção:
                     ├─ Descobre "Plics SW Updater" (.exe no Windows, sem ext. no Linux)
                     └─ GitHub API → baixa .msi (Windows) ou .deb (Linux)
              │
              ▼
       Lança updater <PID> <pkgPath>  →  System.exit(0)
              │
              ▼
       Aguarda PID morrer (onExit().join())
              │
              ▼
       ┌── Windows ── run-update.bat + cmd /c:
       │    taskkill (mata java.exe, javaw.exe, Plics SW.exe)
       │    timeout 10s
       │    msiexec /i <msi> /quiet (retry 3x)
       │    msg.exe notifica
       │
       └── Linux ── run-update.sh + bash:
            pkill -f "Plics SW"
            sleep 10
            pkexec dpkg -i <deb> (retry 3x)
            notify-send notifica
              │
              ▼
       System.exit(0) — saída graciosa
```

## Scripts de empacotamento
- `scripts/config.py`: funções compartilhadas (`run_gradle()`, `run_jlink()`, `run_jpackage()`, etc.)
- `scripts/create-msi.py`: gera instalador Windows (.msi) sem updater (original)
- `scripts/create-deb.py`: gera instalador Linux (.deb) sem updater (original)
- `scripts/updater_config.py`: constantes do updater (nome, main class, UUID)
- `scripts/create-msi-with-updater.py`: gera MSI com updater via `--add-launcher`
- `scripts/create-deb-with-updater.py`: gera DEB com updater via `--add-launcher`

## Últimas alterações

### 2026-07-13: Correção — barra de título com maximize desabilitado após modal/alerta
- **`ShowModal()`**: removido `initOwner()` e alterado `WINDOW_MODAL` → `APPLICATION_MODAL` — elimina relação pai-filho que o window manager do Linux usava para desabilitar maximize da janela pai
- **`ShowAlertError()`**: trocado `showAndWait()` por `show()` — elimina event loop aninhado que impedia o window manager de restaurar controles da janela pai ao fechar o dialog

### 2026-07-12: Estoque mínimo no ProdutoScreen
- **Migration V29**: adicionada coluna `estoque_minimo REAL DEFAULT 0` à tabela `produtos`
- **`ProdutoModel`**: adicionado campo `estoqueMinimo` (BigDecimal)
- **`ProdutoScreenViewModel`**: state `estoqueMinimo`, preenchido em `fillModelFromForm`, `populateFromModel`, resetado em `clearForm`, copiado em `asyncAtualizar`
- **`ProdutoScreen`**: input numérico no formulário, coluna na tabela, detalhe no modal

### 2026-07-12: Busca/Filtro global em todas as telas CRUD
- **`ViewModelScreenContract<Model>`**: adicionado `searchState`, `allDataList`, `filteredList`, `matchesSearch()` abstrato, `fetchListData()` abstrato
- **Padrão**: cada ViewModel agora declara campos `matchesSearch()` e `fetchListData()`, substituindo listas locais por `allDataList`/`filteredList`
- **`Components.searchInput()`**: adicionado ao topo das telas que usam `mainView()` e às telas com `render()` customizado
- **Screens refatoradas**: Categoria, Tecnico, Fornecedor, Compras, VendaMercadoria, ContasAReceber, ComprasAPagar, OrdemServico, Pedidos

### 2026-07-08: Substituição do jSerialComm por JSSC
- **Problema**: jSerialComm 2.10.4 falhava ao extrair DLL nativa (Acesso negado + DLL ARM64 em CPU AMD64).
- **Solução**: Trocado para `io.github.java-native:jssc:2.10.2` (JSSC).
- **PreferenciasViewModel**: `SerialPortList.getPortNames()` em vez de `SerialPort.getCommPorts()`.
- **EscPosPrinter**: `new jssc.SerialPort(porta)` + OutputStream wrapper.
- **Main.java**: `corrigirArquiteturaNativa()` mantida (corrige `os.arch` se `aarch64` em CPU AMD64).

### 2026-07-08: Listagem de impressoras Windows no select de preferências
- **PreferenciasViewModel.load()**: agora também lista impressoras Windows via `PrintServiceLookup.lookupPrintServices()` como `"Nome - Spooler"`.
- **EscPosPrinter.resolverOutputStream()**: detecta se o nome é serial (`COM\d+`) ou impressora Windows. Se for impressora, busca `PrintService` por nome.
- **isPrinterAcceptingJobs()**: verifica atributo `PrinterIsAcceptingJobs` antes de criar `PrinterOutputStream`. Se offline/não aceitando, loga `warn` e tenta fallback (impressora padrão → preview .txt).
- **DevicesTest.java**: teste de diagnóstico para listar portas seriais e impressoras Windows.

### 2026-07-08: Porta da impressora salva em preferências e usada nas telas de venda
- **Migration V23**: adicionada coluna `porta_impressora TEXT` à tabela `preferencias`.
- **`PreferenciasModel`**: adicionado campo `portaImpressora` com `@Column(name = "porta_impressora")`.
- **`PreferenciasDto`**: adicionado campo `portaImpressora`.
- **`PreferenciasViewModel.load()`**: restaura `comportsStateSelected` a partir da porta salva.
- **`PreferenciasViewModel.salvar()`**: extrai o nome da porta do item selecionado (parte antes de " - ") e salva no model.
- **`VendaMercadoriaScreenViewModel`**: porta carregada de `PreferenciasService` em vez de hardcoded `/dev/rfcomm0`.
- **`PDVScreenViewModel`**: porta carregada de `PreferenciasService` e passada ao `EscPosPrinter`; se houver porta definida, usa o construtor com porta, senão usa o fallback (impressora do sistema).

### 2026-07-04: Adicionadas propriedades cor, tamanho, modelo ao ProdutoScreen
- **`ProdutoModel`**: adicionados campos `cor`, `tamanho`, `modelo` (String).
- **`ProdutoDto`**: adicionados campos `cor`, `tamanho`, `modelo`.
- **Migration V22**: `ALTER TABLE produtos ADD COLUMN cor/tamanho/modelo TEXT`.
- **`Data.java`**: adicionadas listas `listaCores` (16 cores) e `listaTamanhos` (PP, P, M, G, GG, XG, UN).
- **`ProdutoScreenViewModel`**: adicionados states `corSelected`, `tamanhoSelected`, `modelo`; atualizados `fillModelFromForm`, `clearForm`, `populateFromModel`.
- **`ProdutoScreen`**: adicionados selects de Cor/Tamanho e input de Modelo no formulário; colunas na tabela; detalhes no modal.
- **`ProdutoServiceTest`**: +1 teste (`deveSalvarProdutoComPropriedades`).
- **`ProdutoScreenViewModelTest`**: +1 teste (`deveSalvarProdutoComPropriedades`).
- **Arquivos `.md` de teste**: adicionadas colunas Cor, Tamanho, Modelo nas tabelas de ProdutoScreen de todos os 5 perfis.

### 2026-07-03: Impressão de nota de venda com ESC/POS
- **`EscPosPrinter`**: implementado `imprimir(VendaModel)` e `imprimirNotaVenda(PedidoModel, List<PedidoItemModel>, ClienteModel, EmpresaModel)` usando `escpos-coffee` (4.1.0). Gera ESC/POS com cabeçalho da empresa, itens, totais, pagamento, rodapé e corte de papel. Saída vai para impressora térmica padrão do sistema ou arquivo `.bin` como fallback.
- **`PDVScreenViewModel`**: `finalizarVenda()` agora armazena `lastPedido` (PedidoModel). `imprimirNota()` implementado — busca itens via `PedidoItemService`, empresa via `EmpresaService`, cliente via `ClienteService` e delega ao `EscPosPrinter`.
- **`ComprovanteBuilder`**: interface mantida com `imprimir(VendaModel)`.
- Dependência `escpos-coffee` já declarada no `build.gradle.kts` (não adicionada).

### 2026-07-03: Correção de valores gigantes em subtotal/troco no PDV
- **`PDVScreenViewModel`**: subtotal e troco agora armazenam centavos inteiros via `Utils.deRealParaCentavos()`.
- **`Utils.deRealParaCentavos()`**: substituído `intValue()` por `setScale(0, HALF_UP).toBigInteger()` — elimina truncamento e overflow.
- **`FornecedorServiceTest`**: corrigido CNPJ no teste de duplicidade.

### 2026-07-03: Validação de CNPJ simplificada — suporte ao formato alfanumérico
- **`Utils.isValidCnpj()`**: removido cálculo de dígitos verificadores (módulo 11). Agora valida apenas formato: 14 caracteres (0-9, A-Z), últimos 2 obrigatoriamente dígitos.
- **`calcMod11()` e constantes de peso removidos** por obsolescência.
- **Arquivos `.md` de teste**: todos os 16 CNPJs corrigidos para dígitos verificadores válidos.

### 2026-07-02: Testes para tipo de pessoa (Física/Jurídica) em FornecedorScreen
- **Select "Tipo de pessoa"** adicionado em FornecedorScreen com `Data.tiposPessoaList` ("Física", "Jurídica").
- **ViewModel**: `tipoPessoaSelected` (State), `tipoPessoaEhFisica` (ComputedState). Valida CPF se física, CNPJ se jurídica. Só valida CPF/CNPJ quando campo preenchido (opcional).
- **Service**: `validar()` agora aceita CPF (11 dígitos) ou CNPJ (14 dígitos) — antes só aceitava CNPJ.
- **FornecedorServiceTest**: +3 testes (CPF válido, CPF inválido, CPF no update) — total 14 testes, 0 falhas.
- **FornecedorScreenViewModelTest**: reescrito com 8 testes (tipoPessoa inicial, ComputedState, física, jurídica, sem doc, nome vazio, clearForm).

### 2026-07-01: Correção de requestFocus() no input de quantidade (ComprasScreen)
- **`InputRef.requestFocus()`**: agora percorre os filhos do `StackPane` e foca o `TextField` interno diretamente, em vez de chamar `requestFocus()` no `StackPane` (que era ignorado por não ser focusable).
- **Causa raiz**: `InputBase` (megalodonte) envolve o `TextField` em um `StackPane`. `getJavaFxNode()` retorna o `StackPane`, que tem `focusTraversable = false` — o `requestFocus()` no `StackPane` não delega para o `TextField`.

### 2026-06-30: Correção de vazamento de Sessions SQLite
- **`DB.closeAllSessions()`** agora é chamado no shutdown (`Main.handleClose()`)
- **`reloadProdutos()`** em ComprasScreenVM e VendaMercadoriaScreenVM reusa service existente em vez de criar novo
- **`ClienteService` e `EmpresaService`** alinhados ao padrão `DB.getPersismSession()`
- **Import não utilizado removido** de `CategoriaModel.java`

### 2026-06-30: Correção Flyway + SQLite — migrations modificadas e inicialização duplicada
- **V1 e V7 restaurados ao original**: checksums agora correspondem ao primeiro commit. V20 e V21 convertem tipos para `REAL` em bancos existentes.
- **`flyway.repair()` adicionado** antes de `migrate()` no startup — recalcula checksums automaticamente para qualquer banco, eliminando `FlywayValidateException` em upgrades.
- **Flyway removido de `DB.getPersismSession()`**: agora é chamado uma única vez em `Main.initialize()`.
- **`CategoriaService` alinhado** ao padrão `DB.getPersismSession()`.
- **Migration V21** criada para corrigir tipos das colunas de `compras` em bancos existentes.

### 2026-07-04: Correção — validação inconsistente da licença de teste
- **`AuthScreenViewModel.entrar()`**: agora usa `isLicensaTesteExpirada()` em vez de `day > 11` — alinhado com `load()`.
- **`HomeScreenViewModel.isLicensaTesteExpirada()`**: `day > 11` → `day > 3` — mesmo threshold do AuthScreenVM.
- **Efeito**: licença de teste expira no dia 4 (validade até dia 3). Antes, o campo de licença era exibido mas o login ainda era permitido até dia 11.

### 2026-06-29: Produtos sem cadastro válido em testes de venda
- `testes-loja-de-roupas.md`: SKU004 do #18 alterado para SKU006 (libera SKU004). Adicionados #143 (Jaqueta SKU003) e #144 (Calça Jeans SKU004) como cadastros válidos.
- `testes-mercado.md`: Adicionada seção ProdutoScreen com #145 (Arroz 5kg), #146 (Feijão 1kg), #147 (Óleo 900ml).
- Demais perfis verificados — nenhum outro arquivo .md apresenta o mesmo problema.

### 2026-06-29: Clientes de perfil sem cadastro válido em testes-gerais.md
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes (#12-#23)

### 2026-07-08: Tabela `cores` criada — cores vêm do banco em vez de lista fixa
- **Migration V24**: `CREATE TABLE cores` com as 16 cores pré-populadas
- **CorModel, CorRepository, CorService** criados seguindo padrão do projeto
- **ProdutoScreenViewModel**: `cores` carregado via `CorService`; `coresSelecionadas` mantém nomes selecionados
- **ProdutoScreen**: checkboxes agora iteram sobre `vm.cores` (do banco)
- **Data.java**: `listaCores` pode ser removida futuramente (não usada mais pela tela)
- **Arquivos criados**: `V24__criar_tabela_cores.sql`, `CorModel.java`, `CorRepository.java`, `CorService.java`
- **Arquivos alterados**: `ProdutoScreenViewModel.java`, `ProdutoScreen.java`

### 2026-07-08: Tabela de produtos não atualizava após CRUD
- **`handleClickMenuDelete()`**: adicionado `produtos.removeIf()` para remover item da lista após exclusão
- **`asyncAtualizar()`**: substituído recarregamento completo por `updateIf` com nova instância (padrão FornecedorScreenViewModel)
- **`loadInicial()`**: adicionado `this.produtos.clear()` antes de `addAll()` para evitar duplicação
- **Arquivo alterado**: `ProdutoScreenViewModel.java`

---

- **Correção do erro "For input string: 1797044400000" em produtos perecíveis**: A coluna `validade` da tabela `produtos` era `INTEGER`, mas Persism mapeia `INTEGER` do SQLite para `Integer` em Java. Como o valor armazenado é epoch millis (Long), o `Converter.convert()` do Persism tentava `Integer.parseInt("1797044400000")`, lançando `NumberFormatException`. Alterado para `REAL` (mesmo tipo usado por `vendas.data_validade` e `compras.data_validade`), que Persism mapeia para `Double` — compatível com `Long`. Adicionada migration V20 para converter tabelas existentes.
- **`fillModelFromForm()` no ProdutoScreenViewModel**: Só seta `validade` no model quando perecível é "Sim". Evita que data residual do DatePicker seja salva ao desmarcar "É perecível?".

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
- welcomeScreen (ViewModel criada)

## Funcionalidades
- **Excluir todos os dados**: botão destrutivo na PreferenciasScreen, apaga todas as 16 tabelas com confirmação e transação. Após exclusão, exibe popup modal sempre-no-topo com botão "Fechar aplicativo" que chama `Platform.exit()`.
- **Components.ShowPopupForced**: Stage modal (`APPLICATION_MODAL`) com `setAlwaysOnTop(true)` para mensagens que exigem ação do usuário antes de continuar.

## Documentação de testes
- `testes.md` criado na raiz do projeto com 104 casos de teste distribuídos por 14 telas
- 5 perfis de negócio simulados: Loja de Roupas, PetShop, Lanchonete, Açougue, Mercado
- Cada caso de teste inclui campo para registro de erro/inconsistência
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes faltantes (#12-#23) que eram referenciados em testes de perfil mas não tinham cadastro válido definido (João Pedro, Carla Lima, Carlos Mendes, Sofia Rocha, Luana Costa, Thiago Santos, José Moura, Renata Oliveira, Paulo Sérgio, Fernanda Lima, Ricardo Gomes, Juliana Costa)

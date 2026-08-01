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

### 2026-08-01: RelatoriosScreen — produtos sem venda no período
- **Card "Produtos sem venda no período"** na RelatoriosScreen, logo abaixo do top 3 mais vendidos: lista todos os produtos cadastrados que **não tiveram nenhuma venda no período selecionado** (mesma janela do ranking), ordenados por descrição, com unidade entre parênteses. Quando todos os produtos venderam, mostra "Todos os produtos tiveram venda no período".
- **`RelatorioService`**: agregação extraída para `quantidadesVendidas(long, long)` (privado, reutilizado por `produtosMaisVendidos` e `produtosSemVenda`); `produtosSemVenda` = todos os `produtos` (via `ProdutoService.listar()`) cujo `codigo_barras` não está no conjunto de vendidos do período, com `quantidade = ZERO` e descrição resolvida do cadastro. `RelatorioDados` ganhou o campo `produtosSemVenda`.
- **ViewModel/Screen**: `State<String> produtosSemVenda` renderizado por um `Text(State<String>)` multi-linha (`\n` separa cada produto) — o `Text` do framework subscribe o `ReadableState`, então reage a cada relatório novo.
- **PDF**: não incluído — o `RelatorioPdfExporter` é de página única e a lista pode ser longa (decisão em `docs/DECISIONS.md`).
- **Testes**: 273 → 275 (`RelatorioServiceTest` +2 sem-venda no período e venda fora do período; asserts extras nos casos existentes), 0 falhas.
- **Teste manual**: `testes-gerais.md` RelatoriosScreen +1 caso (#180 produtos sem venda no período).

### 2026-08-01: RelatoriosScreen — top 3 produtos mais vendidos do período
- **Card "Produtos mais vendidos do período"** na RelatoriosScreen: mostra os 3 produtos com maior quantidade vendida no período escolhido, somando as duas fontes de venda — `pedido_itens` (PDV, filtrado pelo período via `pedidos.dataCriacao`) e `vendas` (VendaMercadoriaScreen) — com descrição/unidade resolvidas da tabela `produtos`. Produto vendido sem cadastro cai para o `codigo_barras` como descrição.
- **`RelatorioService.produtosMaisVendidos(long, long)`** (novo): agrega em Java (`Map<codigo, quantidade>` com `BigDecimal::add`), ordena desc, limita a 3 — mesmo estilo dos "somarXPorPeriodo" (que já agregavam em Java, sem GROUP BY). `RelatorioDados` ganhou o campo `produtosMaisVendidos` (lista de `ProdutoMaisVendido` — record novo).
- **`VendaRepository`/`PedidoItemRepository`**: novos `listarPorPeriodo` (mesmo padrão de `somarVendasPorPeriodo`); delegates em `VendaService`/`PedidoItemService`.
- **PDF**: `RelatorioPdfExporter` ganhou a seção "PRODUTOS MAIS VENDIDOS DO PERÍODO" (espelha a tela; "Nenhum produto vendido no período" quando vazio).
- **Testes**: 265 → 273 (`VendaRepositoryTest` +2, `PedidoItemRepositoryTest` +2, `RelatorioServiceTest` +3 ranking e soma PDV+vendas, `RelatorioPdfExporterTest` +1 seção no PDF), 0 falhas.
- **Teste manual**: `testes-gerais.md` RelatoriosScreen +2 casos (#178 ranking, #179 período sem vendas).

### 2026-07-31: RelatoriosScreen — relatório financeiro por período, com gráficos e exportação em PDF
- **Nova tela** (`AppRoutes.Screens.RELATORIOS`, acessível via Home > menu "Gerencial" > "Relatórios"): seleciona Data Início/Fim (padrão: mês atual, igual à Home) e mostra Receitas/Despesas/Lucro Líquido do período + situação atual de contas em aberto — reaproveita os mesmos métodos `somarXPorPeriodo`/`getTotalEmAberto` já usados pela `HomeScreenViewModel`, só que com período livre em vez de fixo no mês atual
- **`RelatorioService`** (novo, `my_app.services`): orquestra `VendaService`, `PedidoService`, `ContaAreceberService`, `CompraService`, `ContasPagarService` — nenhuma lógica de agregação nova, só soma o que cada Service já expõe
- **Gráficos**: `javafx.scene.chart` nativo (`BarChart` Receitas x Despesas x Lucro; 2x `PieChart` de composição). O framework de UI do projeto não tem "reconstruir componente reativo" — os objetos `PieChart.Data`/`XYChart.Data` são criados uma vez e só têm o valor mutado a cada novo relatório, que é como o JavaFX Chart já atualiza sozinho
- **PDF**: `Apache PDFBox 2.0.29` (nova dependência, Apache 2.0) — `RelatorioPdfExporter` escreve o relatório em texto simples num PDF (cabeçalho com dados da empresa, igual ao já usado no ESC/POS). Botão "Baixar PDF" abre `FileChooser` nativo
- **Testes**: `RelatorioServiceTest` (soma real de vendas/pedidos/contas/compras num período + período vazio retorna zeros) e `RelatorioPdfExporterTest` (gera um PDF de verdade e confere o texto extraído via `PDFTextStripper`). 265 testes, 0 falhas
- **Teste manual**: nova seção RelatoriosScreen em `testes-gerais.md` (#171-#177) + `testes.md` reindexado

### 2026-07-31: Excluir venda do histórico do caixa (PedidosScreen)
- **`PedidosScreenViewModel`/`PedidosScreen`**: tela era só leitura (`handleClickMenuDelete()` vazio). Agora tem botão "Excluir venda selecionada" (só aparece quando há um pedido selecionado, via `ComputedState temPedidoSelecionado`), com confirmação antes de excluir.
- **`PDVService.excluirVenda(int pedidoId)`** (novo, transacional — reverso de `finalizarVenda()`): devolve o estoque de cada item vendido (`ProdutoService.incrementarEstoque`), apaga os `pedido_itens` (`PedidoItemRepository.excluirPorPedidoId`, novo), apaga as contas a receber vinculadas se a venda era fiada (`ContaAreceberService.excluirPorVendaId`, já existia) e por fim o `pedido`. Lança `IllegalArgumentException("Venda não encontrada")` se o id não existir — `Session.withTransaction()` do Persism envolve isso numa `PersismException`, mas preserva a mensagem original.
- **Reflexo nos cards da Home**: `EventBus.publish(DadosFinanceirosAtualizadosEvent.getInstance())` após excluir — `HomeScreenViewModel` já escuta esse evento e recalcula Receitas/Despesas/Lucro/"Hoje você fez" (que somam `pedidos.totalLiquido`), sem precisar de nenhuma mudança na Home.
- **Melhorias de UI aproveitadas na mesma tela**: campo de busca (`Components.searchInput` — a infraestrutura de `filteredList`/`matchesSearch` já existia mas não estava exposta na tela) e coluna "Cliente" agora mostra o nome (antes mostrava o `clienteId` numérico) — fecha a pendência registrada em 2026-07-27.
- **Testes**: `PDVServiceTest` +4 casos (estoque restaurado, contas a receber apagadas só da venda excluída — não mexe em outra venda fiada —, exceção em venda inexistente). 260 testes, 0 falhas.
- **Teste manual**: `testes-gerais.md` — seção PedidosScreen ganhou casos de busca (#167) e exclusão de venda à vista/fiada/sem seleção (#168-#170).

### 2026-07-28: APP_VERSION lido em runtime (fim do hardcode duplicado em Main.java)
- **Problema**: `Main.APP_VERSION` era uma string hardcoded (`"1.1.1.1_Patch_5"`) mantida manualmente em paralelo a `gradle.properties.appVersion` (`"1.1.1.1"`) — já tinham desincronizado (números diferentes) antes desta correção.
- **`gradle.properties`**: campo único `appVersion` virou dois campos — `appVersion` (base, x.x.x) e `appPatch` (contador). Versão final = `appVersion` se `appPatch=0`, senão `appVersion.appPatch`.
- **`Main.APP_VERSION`**: agora lê `System.getProperty("plics.appVersion", "dev")` — mesmo padrão do `isMicrosoftStore`. Setado via `-Dplics.appVersion=...` na task `run` do Gradle (`build.gradle.kts`) e em `scripts/config.py` (`run_jpackage()`, então todo build empacotado — msi/deb, com/sem updater — já embute automaticamente).
- **`scripts/bump_version.py`** (novo): `patch` incrementa `appPatch`; `release X.Y.Z` define nova base e zera o patch. Substitui a edição manual de `gradle.properties`.
- **`docs/AI_RULES.md`**: checklist de "alterar versão do app" atualizado — não há mais passo de editar `Main.java`.

### 2026-07-28: Desconto e impressão alternativa no PDVScreen
- **Desconto**: PDVScreen não tinha input de desconto — `PedidoModel.desconto`/`pedido_itens.desconto` já existiam na model/migration, mas `PDVService.finalizarVenda()` sempre gravava `BigDecimal.ZERO`. Adicionado `PDVScreenViewModel.desconto` (currency input) + `totalAPagar` (subtotal - desconto, nunca negativo) recalculados a cada mudança em itens/desconto/recebido. `PDVService.finalizarVenda()` ganhou overload com parâmetro `desconto`, aplicado ao `totalLiquido` do pedido e às parcelas geradas (fiado usa o valor líquido, não o bruto)
- **Impressão alternativa**: PDVScreen não tinha o botão "Imprimir (modo alternativo)" que já existe em VendaMercadoriaScreen (envia bytes ESC/POS crus via `WinRawPrinter`, fallback pra impressoras não reconhecidas pelo Java Print Service). `EscPosPrinter.gerarBytesEscPos(PedidoModel, ...)` criado (só existia a versão pra `VendaModel`); `PDVScreenViewModel.imprimirNotaAlternativa()` + `resolverNomeImpressoraTermica()` replicam o padrão da tela de vendas
- **Testes**: +3 casos em `PDVServiceTest` (desconto aplicado ao total líquido, total não fica negativo se desconto > total, parcelas geradas com valor já descontado) — 256 testes, 0 falhas

### 2026-07-28: Correções de publicação (publisher, erro no Telegram, NPE na 1ª execução, build sem updater pra Store)
- **Publisher "Unknown" no instalador**: `appVendor` existia no `gradle.properties` mas nenhum script chegava a lê-lo. `scripts/config.py` ganhou a constante `VENDOR` e `--vendor` no `run_jpackage()` (usado por todos os scripts de empacotamento). `appVendor` = "Coerente Inc."
- **Notificação de erro no Telegram chegava como "null"**: `Main.handleAppError()` mandava só `t.getMessage()` (null em exceções comuns como `NullPointerException` sem mensagem). Agora inclui tipo da exceção + 3 primeiras linhas do stack trace. `TelegramNotifier` também perdeu o `parse_mode=Markdown` (nunca usado pra formatação e arriscava rejeitar a mensagem inteira por causa de `_` em nomes de pacote como `my_app`) e ganhou `URLEncoder` no texto
- **NPE em `AuthScreenViewModel.isLicenseInvalid()` na primeira execução**: `LICENCAS_PRODUCAO.contains(license)` — lista imutável (`List.of`), `contains(null)` lança NPE. `licensa` é `null` em toda instalação nova (`V16__dados_padrao.sql` não preenche essa coluna), e `Main.initialize()` chama essa validação antes de sair da splash — quebrava a primeira execução em qualquer máquina nova (reportado durante teste de publicação na Microsoft). Fix: retorna `true` direto quando `license == null`
- **Updater com mensagem amigável quando aberto sem argumentos**: "Erro: args insuficientes" (visível em fonte grande pra quem abre o `.exe` do updater manualmente, ex. revisor de loja de app) virou "Updater deve ser aberto programaticamente, você não precisa executá-lo"
- **Build sem updater pra Microsoft Store**: `scripts/create-msi.py` (que estava desatualizado em relação a `create-msi-with-updater.py` — faltava `--win-upgrade-uuid`, não limpava `temp_dir`) foi atualizado e ganhou `--java-options -Dplics.microsoftStore=true`. `Main.isMicrosoftStore` lê essa property; `HomeScreen.menuBar()` não monta mais o item "Buscar atualização" nesse caso (mesmo padrão do `Main.isFlatpak`). `UPGRADE_UUID` movido de `updater_config.py` pra `config.py` (não é específico do updater, é usado em qualquer MSI)

### 2026-07-27: Padronização do contrato CRUD, validações movidas para as Services, cobertura de testes manuais
- **`ViewModelScreenContract.populateFromModel()` → `populateFieldsFromModel()`**: nome antigo era ambíguo (parecia "popular o model", mas populava os campos da UI a partir do model). Renomeado em `ViewModelScreenContract`, `ContratoTelaCrudV3` e nas 12 ViewModels que implementam o contrato
- **`populateModelFromFields()`**: novo método abstrato, inverso do anterior — monta e retorna um `Model` a partir do estado atual dos campos do formulário, sem parâmetros (decide internamente criação vs edição via `modoEdicao`). Padroniza um único jeito de montar Model em todas as 12 ViewModels, substituindo `getModelFromFields`/`fillModelFromForm` (com aridades diferentes) e trechos inline que cada ViewModel fazia à sua maneira. `VendaMercadoriaScreenViewModel` é a única exceção que sempre retorna um Model **novo** mesmo em edição (necessário porque `allDataList.updateIf()` compara por referência)
- **Validações de CRUD centralizadas nas Services**: removidas checagens de negócio duplicadas nas ViewModels (nome/técnico/cliente/produto obrigatórios, limites de pagamento/recebimento) que já existiam nas Services (`FornecedorService`, `OrdemServicoService`, `TecnicoService`, `VendaService`, `ContasPagarService`, `ContaAreceberService`). Duas validações que só existiam na ViewModel foram movidas: `ProdutoService.validar()` ganhou a checagem de validade não-passada; `PreferenciasService` ganhou `validar()` (login/senha obrigatórios com credenciais habilitadas — antes não tinha validação nenhuma)
- **`FornecedorModel.pessoaFisica`** (Migration V31): tipo de pessoa (Física/Jurídica) do form agora é persistido, igual a `ClienteModel`. `FornecedorService.validar()` passou a exigir CPF ou CNPJ de acordo com esse campo quando informado (fallback OR para registros legados sem o campo)
- **`CompraService.salvar(CompraModel)`**: novo overload Model-based (o `salvar(CompraDto)` existente passou a delegar pra ele), permitindo que `ComprasScreenViewModel` retorne um `CompraModel` de `populateModelFromFields()` como as demais telas
- **Testes**: suíte foi de 237 para 253 (`PreferenciasServiceTest` e `ContasPagarServiceTest` de `my_app.services` criados do zero; `FornecedorServiceTest`/`ProdutoServiceTest`/`CompraServiceTest` ganharam casos novos), 0 falhas
- **Testes manuais (`.md`)**: auditoria completa de campos de tela vs colunas testadas nos 7 arquivos `.md`; adicionadas colunas/casos faltantes (Estoque Mínimo, Tipo de Pessoa em Fornecedor, Observação em Compras, logomarca, PDV forma de pagamento/criar cliente, logout); corrigidas incoerências de cadastro referenciado sem existir (nova tabela de categorias de teste em `testes-gerais.md`, `ClienteScreen #9`, ordem de `#35`/`#36`, passos de "Fluxo Completo" que recriavam cadastros já feitos) e numeração duplicada (`#66` em loja-de-roupas, `#122-124` em petshop)
- **Achados de UI sem controle correspondente** (registrados em `docs/TODO.md`, não corrigidos nesta sessão): OrdemServicoScreen não tem select de Status no form; PedidosScreen não tem busca e mostra ID do cliente em vez do nome

### 2026-07-18: Release v1.1.0
- **Versão atualizada**: Main.java → "1.1.0", gradle.properties → 1.1.0, README.md → 1.1.0
- **updates.json**: nova entrada v1.1.0 com 25 notas (Feat, Fix, Chore)
- **Notas incluem**: splash screen, HotReload+Lombok, busca/filtro global, estoque mínimo, impressão crediário com parcelas, redesign boas-vindas, logout, maximize/minimize, FlowRow, ícones animados, e 10 correções



### 2026-07-14: Impressão de nota de venda no crediário com parcelas e assinatura
- **`PDVScreenViewModel`**: state `numeroParcelas` (default "1"), `finalizarVenda()` passa `numeroParcelas` e `formaPagamento` ("CREDIARIO" fiado, "A VISTA" contrário)
- **`PDVScreen`**: input "Nº Parcelas" visível quando fiado marcado
- **`PDVService.finalizarVenda()`**: novo param `numeroParcelas`, usa `Parcela.gerarParcelas()` para N parcelas mensais
- **`EscPosPrinter`**: sobrecarga `imprimir(VendaModel, List<ContaAreceberModel>)`, seção "PARCELAS" + assinatura do cliente na nota
- **`VendaMercadoriaScreenViewModel`**: `imprimirNotaDeVenda()` busca parcelas quando "A PRAZO"
- **`PDVServiceTest`**: atualizado para nova assinatura, teste fiado com 3 parcelas

### 2026-07-13: Correção — barra de título com maximize desabilitado após modal/alerta
- **`ShowModal()`**: adicionado `initOwner(context.selfStage())` sem `initModality` (default NONE), `setOnHidden(requestFocus + toFront)` no owner, conteúdo envolto em `Scroll` (width 800)
- **`ShowAlertError()`**: adicionado `alert.initModality(Modality.NONE)` — Glass toolkit não toca nas decorações da janela owner. Trocado `showAndWait()` por `show()`. Removido `setOnCloseRequest`

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
- relatoriosScreen (nova, 2026-07-31)

## Funcionalidades
- **Excluir todos os dados**: botão destrutivo na PreferenciasScreen, apaga todas as 16 tabelas com confirmação e transação. Após exclusão, exibe popup modal sempre-no-topo com botão "Fechar aplicativo" que chama `Platform.exit()`.
- **Components.ShowPopupForced**: Stage modal (`APPLICATION_MODAL`) com `setAlwaysOnTop(true)` para mensagens que exigem ação do usuário antes de continuar.

## Documentação de testes
- `testes.md` criado na raiz do projeto com 104 casos de teste distribuídos por 14 telas
- 5 perfis de negócio simulados: Loja de Roupas, PetShop, Lanchonete, Açougue, Mercado
- Cada caso de teste inclui campo para registro de erro/inconsistência
- `testes-gerais.md` ClienteScreen: adicionados 12 clientes faltantes (#12-#23) que eram referenciados em testes de perfil mas não tinham cadastro válido definido (João Pedro, Carla Lima, Carlos Mendes, Sofia Rocha, Luana Costa, Thiago Santos, José Moura, Renata Oliveira, Paulo Sérgio, Fernanda Lima, Ricardo Gomes, Juliana Costa)

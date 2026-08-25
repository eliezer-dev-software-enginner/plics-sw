# TODO

## Concluído (opção de troca na devolução — 2026-08-24)
- [x] **Feedback da cliente**: "se na parte de devolução já tivesse a opção de troca, sem
      precisar excluir seria mais prático" — trocar exigia excluir/devolver + lançar venda nova
      em dois passos
- [x] **Modelo acordado com o usuário**: devolve a venda original + cria pedido novo com os
      itens novos (busca por código/nome, vários itens, diferença reflete nos totais); só no
      Histórico do Caixa/PDV — detalhes em DECISIONS.md
- [x] **Fix**: `PDVService.trocarVenda()` (transação única, política "Aceita devolução/troca?"
      aplicada aos itens originais) + botão/modal no PedidosScreen; pedido novo nunca é fiado;
      sem migration nova
- [x] **Testes**: 302 → 308 (+6 PDVServiceTest), 0 falhas; casos manuais #208–#211
- [ ] Verificar o modal ao vivo (sem automação de mouse/clique neste ambiente)

## Pendências novas
- [ ] **RelatoriosScreen ainda conta vendas devolvidas nos cards "Produtos mais vendidos" e
      "Formas de pagamento mais usadas"** (achado em 2026-08-24 ao corrigir a receita da Home):
      `quantidadesVendidas()` e `formasPagamentoMaisUsadas()` usam `listarPorPeriodo`, que não
      filtra `devolvida=true` (de propósito — ver DECISIONS.md). Decidir se devolução deve
      tirar o produto do ranking e do gráfico de formas de pagamento também.
- [ ] **Revisar cadastros antigos de produtos**: com a nova regra de bloqueio de devolução
      (2026-08-24), todo produto ficou "Não" por default da V33 — quem aceita devolução/troca
      precisa ser editado pra "Sim" na ProdutoScreen, senão a devolução será bloqueada.

## Concluído (produtos devolvidos no relatório + PDF — 2026-08-24)
- [x] **Pedido**: no RelatoriosScreen e no PDF, mostrar quantos produtos foram devolvidos no
      período e quais foram
- [x] **Critério**: filtrado por `data_devolucao` (não pela data da venda) — ver DECISIONS.md
- [x] `ResumoDevolucoes` + `RelatorioService.resumoDevolucoes()` + `listarDevolvidasPorPeriodo`
      em Pedido/VendaRepository; card novo na tela + seção nova no PDF
- [x] **Testes**: 297 → 302 (+5), 0 falhas; casos manuais #206/#207

## Concluído ("Aceita devolução/troca?" não era respeitado — 2026-08-24)
- [x] **Reportado**: venda devolvida cujo produto estava marcado "Não" no campo
      Aceita devolução/troca — o sistema deixou; o campo era só informativo
- [x] **Decisões** (perguntado ao usuário): bloquear devolução; exclusão livre; produtos
      antigos continuam "Não" (sem migration) — detalhes em DECISIONS.md
- [x] **Fix**: validação em `PDVService.devolverVenda()` e `VendaService.devolver()`, antes do
      estoque, listando os produtos que impedem no alerta
- [x] **Testes**: 294 → 297 (+3), fixtures ajustados (`aceitaDevolucao=true`)

## Concluído (vendas devolvidas contavam como receita na Home — 2026-08-24)
- [x] **Reportado**: "Devolver venda selecionada" no PedidosScreen devolvia o estoque, mas
      "Receitas do mês" mantinha o valor cheio
- [x] **Causa**: `PedidoRepository.somarPedidosPorPeriodo`/`VendaRepository.somarVendasPorPeriodo`
      somavam vendas com `devolvida=true`; mesmo bug afetava "Hoje você feito" e RelatoriosScreen
      (mesmos métodos)
- [x] **Fix**: filtro `!Boolean.TRUE.equals(getDevolvida())` nas duas somas (Home/Relatórios
      herdam); excluir vs devolver inalterados (ver DECISIONS.md pra diferença entre os dois)
- [x] **Testes**: 291 → 294 (+3 em Pedido/VendaRepositoryTest), 0 falhas

## Concluído (log de produção crescendo por causa dos testes — 2026-08-19)
- [x] **Bug real encontrado e corrigido** (mesmo problema achado primeiro no
      `balanca-gobitech`): testes sem `logback.xml` próprio escreviam direto em
      `~/.plics-sw/logs/plics-sw.log` — cada rodada de `./gradlew test` recria o schema via
      Flyway do zero, gerando milhares de linhas de log só de migration. Pasta de logs tinha
      chegado a 19MB por causa disso, não de uso real do app
- [x] `src/test/resources/logback-test.xml` (console apenas, nível WARN) — verificado rodando a
      suíte completa duas vezes: arquivo de produção não ganha nenhuma linha nova
- [x] Logs antigos (~19MB) limpos
- [x] `./gradlew test`: BUILD SUCCESSFUL, sem regressão

## Concluído (auditoria de vazamento de recurso em onDestroy — 2026-08-18)
- [x] Auditado o projeto inteiro (todas as ~19 telas + serviços com thread/executor/porta)
      atrás do mesmo padrão do bug achado no `balanca-gobitech`: recurso de vida longa aberto
      em `onMount`/construtor sem teardown em `onDestroy`
- [x] Achado e corrigido: `HomeScreenViewModel.executor` (`ScheduledExecutorService`,
      thread não-daemon) nunca era desligado — vazava uma thread a cada navegação pra Home.
      `onDestroy()` agora chama `executor.shutdownNow()`. Ver `DECISIONS.md`.
- [x] `EscPosPrinter`, `UpdaterService`, `my_app.updater.*` e as outras 18 telas verificados e
      descartados (não têm o mesmo padrão de risco)
- [x] `./gradlew test`: **294 testes, BUILD SUCCESSFUL** (sem regressão)

## Concluído (Produtos sem venda no período no RelatoriosScreen — 2026-08-01)
- [x] **Card "Produtos sem venda no período"** na RelatoriosScreen abaixo do ranking: lista produtos cadastrados sem venda no período, ordenados por descrição, com unidade; mensagem "Todos os produtos tiveram venda no período" quando vazio
- [x] **`RelatorioService`**: agregação extraída para `quantidadesVendidas(long, long)` privado; `produtosSemVenda()` = produtos de `ProdutoService.listar()` fora do conjunto de vendidos do período (mesmo record `ProdutoMaisVendido`, quantidade ZERO)
- [x] **`RelatorioDados.produtosSemVenda`** (novo campo)
- [x] **ViewModel/Screen**: `State<String> produtosSemVenda` + `Text(State<String>)` multi-linha
- [x] **PDF**: decidido NÃO incluir (página única, lista pode ser longa) — decisão registrada em DECISIONS.md
- [x] **Testes**: 273 → 275 (`RelatorioServiceTest` +2 sem-venda/período e venda fora do período), 0 falhas
- [x] **Teste manual**: `testes-gerais.md` RelatoriosScreen +1 caso (#180)
- [x] **Docs**: CONTEXT.md, DECISIONS.md, TODO.md atualizados

## Concluído (Top 3 produtos mais vendidos no RelatoriosScreen — 2026-08-01)
- [x] **Card "Produtos mais vendidos do período"** na RelatoriosScreen (3 estados no ViewModel + Card na tela)
- [x] **`RelatorioService.produtosMaisVendidos(long, long)`**: agrega quantidade por `produto_cod` (PDV `pedido_itens` + `vendas` de mercadoria, ambos `listarPorPeriodo`), ordena desc, limita a 3; descrição/unidade via `ProdutoService`
- [x] **`ProdutoMaisVendido`** (record novo) e **`RelatorioDados.produtosMaisVendidos`** (novo campo)
- [x] **`VendaRepository.listarPorPeriodo`** + **`PedidoItemRepository.listarPorPeriodo`** (JOIN `pedidos`) + delegates em `VendaService`/`PedidoItemService`
- [x] **PDF** com seção "PRODUTOS MAIS VENDIDOS DO PERÍODO" (`RelatorioPdfExporter`)
- [x] **Testes**: 265 → 273 (`VendaRepositoryTest` +2, `PedidoItemRepositoryTest` +2, `RelatorioServiceTest` +3, `RelatorioPdfExporterTest` +1), 0 falhas
- [x] **Teste manual**: `testes-gerais.md` RelatoriosScreen +2 casos (#178-#179)
- [x] **Docs**: CONTEXT.md, DECISIONS.md, TODO.md atualizados

## Concluído
- PreferenciasScreen refatorada (ViewModel, Service, Repository, Test)
- FornecedorScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V4)
- ProdutoScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V1)
- TecnicoScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V12)
- PedidosScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V15)
- Eventos movidos para core/events com EntityEvent<T> genérico
- Interfaces ContratoTelaCrud e ContratoTelaCrudV2 removidas (depreciadas)
- FeedbackViewModel criado (compartilhado entre RelatarErroScreen e SugerirMelhoriaScreen)
- InfoUpdateScreenViewModel criado
- AuthScreen, Main, WelcomeScreen já utilizam PreferenciasService (padrão atual)
- OrdemServicoScreen já refatorada com ViewModel (ContratoTelaCrudV3)
- **ComprasRepositoryTest** criado com 6 testes (CRUD + somarComprasPorPeriodo)
- **WelcomeScreen** movido para pacote `welcomeScreen` com ViewModel
- **PedidosScreenViewModel** refatorado: substitui `PedidoItemRepository` por `PedidoItemService`
- **Migration V7**: tipos alinhados (INTEGER/TEXT → REAL) para compatibilidade com Persism
- **CompraModel.fornecedorId**: `Long` → `Integer` (consistente com demais models)

## Concluído (Updater)
- [x] **Suporte a Linux**: updater detecta SO e gera `.sh` com `pkill` + `pkexec dpkg -i` + `notify-send`
- [x] `UpdaterService.findPackageAsset()`: busca `.deb` no Linux, `.msi` no Windows
- [x] `discoverUpdaterPath()`: busca launcher Linux em `/opt/`, `/usr/lib/`, `/usr/local/lib/`
- [x] Pacote `my_app.updater` com Main, HomeScreen, HomeScreenViewModel (batch taskkill + msiexec + msg)
- [x] `my_app.infra.UpdaterService` com `getLatestVersion()`, `hasUpdate()`, `downloadLatestMsi()`
- [x] `Main.java`: `public static void main(String[] args)` + `start()` + `onEvent()`
- [x] Menu "Buscar atualização" no HomeScreen com verificação de versão
- [x] Scripts: `updater_config.py`, `create-msi-with-updater.py`, `create-deb-with-updater.py`
- [x] Docs atualizados (README, CONTEXT, DECISIONS, TODO)
- [x] **Corrigido deadlock do updater**: app principal agora fecha (`System.exit(0)`) após lançar o updater, permitindo que o `ph.onExit().join()` prossiga
- [x] **Updater com saída graciosa**: updater chama `System.exit(0)` após lançar batch script para liberar handles de DLLs; removido updater do `taskkill` no script

## Concluído (testes.md)
- [x] `testes.md` criado na raiz do plics-sw com 108 cenários de teste cobrindo 14 telas
- [x] 5 perfis de negócio (Loja Roupas, PetShop, Lanchonete, Açougue, Mercado)
- [x] Cada teste possui campo de erro/inconsistência para anotação manual

## Concluído (unidade gramas)
- [x] Adicionado "g" (gramas) à lista `unidadesDeMedidaList` em `Data.java`
- [x] Testes de produto, compra, venda e PDV com unidade "g" no perfil Açougue

## Concluído (excluir todos os dados)
- [x] Botão "Excluir todos os dados" na PreferenciasScreen (vermelho, destrutivo)
- [x] Confirmação com alert antes de excluir
- [x] Exclusão em todas as 16 tabelas com transação e FK desabilitado

## Concluído (validação login/senha PreferenciasScreen)
- [x] **PreferenciasViewModel.validar()**: método que retorna mensagem de erro ou null para login/senha quando credenciais habilitadas
- [x] **PreferenciasViewModel.salvar()**: chama `validar()` antes de `Async.Run()` — exibe alerta e retorna se falhar
- [x] **PreferenciasViewModelTest**: 4 novos testes para `validar()` (credenciais desabilitadas, login vazio, senha vazia, ambos preenchidos)
- [x] **testes-gerais.md**: cenários 105 e 106 marcados como OK

## Concluído (padronização CategoriaScreen entre .md)
- [x] **testes-gerais.md**: removidos testes de perfil (#33 Bovinos, #36 Masculino) — só validação genérica
- [x] **testes-acougue.md**: adicionada seção CategoriaScreen (#33 Bovinos)
- [x] **testes.md**: link CategoriaScreen listando todos os perfis

## Concluído (produtos sem cadastro válido)
- [x] **testes-loja-de-roupas.md**: #18 SKU004→SKU006 (libera SKU004). Adicionados #143 (Jaqueta SKU003) e #144 (Calça Jeans SKU004) como cadastros válidos.
- [x] **testes-mercado.md**: Adicionada seção ProdutoScreen com #145 (Arroz 5kg), #146 (Feijão 1kg), #147 (Óleo 900ml) para PDVScreen.
- [x] **Demais perfis verificados (PetShop, Lanchonete, Açougue)**: todos os produtos usados em vendas já possuíam cadastro válido.

## Concluído (clientes de perfil em testes-gerais.md)
- [x] **testes-gerais.md ClienteScreen**: adicionados 12 clientes (#12-#23) que estavam faltando — João Pedro, Carla Lima, Carlos Mendes, Sofia Rocha, Luana Costa, Thiago Santos, José Moura, Renata Oliveira, Paulo Sérgio, Fernanda Lima, Ricardo Gomes, Juliana Costa
- [x] **Fornecedores verificados**: todos os fornecedores usados em testes já estavam definidos — sem alteração necessária

## Concluído (PDV subtotal/troco gigantes — 2026-07-03)
- [x] **`PDVScreenViewModel`**: subtotal e troco agora usam `Utils.deRealParaCentavos()` em vez de `toPlainString()`.
- [x] **`Utils.deRealParaCentavos()`**: `intValue()` → `setScale(0, HALF_UP).toBigInteger()` (precisão total).
- [x] **`FornecedorServiceTest`**: CNPJ duplicado corrigido para `11222333000181`.
- [x] **`UtilsTest`**: testes renomeados para validar formato.

## Concluído (CNPJ alfanumérico + docs de teste — 2026-07-03)
- [x] **`Utils.isValidCnpj()`**: removido cálculo de dígitos verificadores. Agora valida: 14 chars alfanum (0-9, A-Z) + últimos 2 são dígitos.
- [x] **`calcMod11()` removido** junto com as constantes de peso.
- [x] **Arquivos `.md` de teste**: 16 CNPJs corrigidos (todos agora válidos).
- [x] **Docs atualizados** (DECISIONS.md, CONTEXT.md, TODO.md).

## Concluído (Tipo de pessoa FornecedorScreen — 2026-07-02)
- [x] **FornecedorService.validar()**: aceita CPF (11 dígitos) e CNPJ (14 dígitos) — antes só CNPJ
- [x] **FornecedorScreenViewModel.handleAddOrUpdate()**: valida nome primeiro (obrigatório); CPF/CNPJ validado conforme tipo de pessoa, apenas se preenchido
- [x] **FornecedorServiceTest**: +3 testes (CPF válido, CPF inválido, CPF no update)
- [x] **FornecedorScreenViewModelTest**: 8 testes (tipoPessoa inicial, ComputedState, física, jurídica, sem doc, nome vazio, clearForm)

## Concluído (Impressão nota de venda — 2026-07-03)
- [x] **`EscPosPrinter.imprimir(VendaModel)`**: implementado com ESC/POS via escpos-coffee (cabeçalho empresa, item, totais, corte).
- [x] **`EscPosPrinter.imprimirNotaVenda()`**: novo método para pedidos multi-item (PDV).
- [x] **`PDVScreenViewModel.finalizarVenda()`**: armazena `lastPedido` para impressão posterior.
- [x] **`PDVScreenViewModel.imprimirNota()`**: busca itens/cliente/empresa e imprime via `EscPosPrinter`.
- [x] **Saída**: impressora térmica padrão (`PrinterOutputStream`) com fallback para arquivo `.bin`.

## Concluído (Estoque mínimo — 2026-07-12)
- [x] **Migration V29**: `ALTER TABLE produtos ADD COLUMN estoque_minimo REAL DEFAULT 0`
- [x] **ProdutoModel**: campo `estoqueMinimo` (BigDecimal)
- [x] **ProdutoScreenViewModel**: state `estoqueMinimo`, preenchido em `fillModelFromForm`, `populateFromModel`, resetado em `clearForm`, copiado em `asyncAtualizar`
- [x] **ProdutoScreen**: input numérico no formulário, coluna na tabela, detalhe no modal

## Concluído (Busca/Filtro global — 2026-07-12)
- [x] **`ViewModelScreenContract<Model>`**: adicionado `searchState`, `allDataList`, `filteredList`, `matchesSearch()`, `fetchListData()`
- [x] **`ContratoTelaCrudV3.mainView()`**: adicionado `Components.searchInput()` no topo
- [x] **9 telas refatoradas**: Categoria, Tecnico, Fornecedor, Compras, VendaMercadoria, ContasAReceber, ComprasAPagar, OrdemServico, Pedidos
- [x] **Tabelas**: todas usam `vm.filteredList` em vez de listas locais

## Concluído (Impressão nota crediário com parcelas e assinatura — 2026-07-14)
- [x] **`PDVScreenViewModel`**: state `numeroParcelas` (default "1"), `finalizarVenda()` passa `numeroParcelas` e `formaPagamento` ("CREDIARIO" fiado)
- [x] **`PDVScreen`**: input "Nº Parcelas" visível quando fiado marcado
- [x] **`PDVService.finalizarVenda()`**: novo param `numeroParcelas`, usa `Parcela.gerarParcelas()` para N parcelas mensais
- [x] **`EscPosPrinter`**: sobrecarga `imprimir(VendaModel, List<ContaAreceberModel>)`, seção "PARCELAS" + assinatura
- [x] **`VendaMercadoriaScreenViewModel`**: `imprimirNotaDeVenda()` busca parcelas quando "A PRAZO"
- [x] **`PDVServiceTest`**: atualizado para nova assinatura, teste fiado com 3 parcelas

## Concluído (Correção barra de título maximize — 2026-07-13)
- [x] **`ShowModal()`**: adicionado `initOwner(context.selfStage())` sem `initModality` (default NONE), `setOnHidden(requestFocus + toFront)` no owner, conteúdo envolto em `Scroll` (width 800)
- [x] **`ShowAlertError()`**: adicionado `alert.initModality(Modality.NONE)` — Glass toolkit não toca nas decorações da janela owner. Trocado `showAndWait()` por `show()`. Removido `setOnCloseRequest`
- [x] **HotReload removido** de `Main.java` (código descontinuado)
- [x] **Arquivo alterado**: `Components.java`, `Main.java`, `AppRoutes.java`, `ProdutoScreen.java`

## Concluído (Release v1.1.0 — 2026-07-18)
- [x] **updates.json**: nova entrada v1.1.0 com 25 notas interpretadas dos commits desde 10/07/2026
- [x] **Main.java**: APP_VERSION "1.0.9 - patch 02" → "1.1.0"
- [x] **gradle.properties**: appVersion "1.0.9.2" → "1.1.0"
- [x] **README.md**: Versão "1.0.0" → "1.1.0"
- [x] **AI_RULES.md**: etapas obrigatórias para atualização de versão documentadas
- [x] **CONTEXT.md**: registro da release v1.1.0

## Concluído (Simplificação cadastros válidos clientes — 2026-07-19)
- [x] **testes-gerais.md ClienteScreen**: reduzido de 27 para 13 testes — removidos 14 "cadastro válido" redundantes
- [x] **Tabela de dados de teste**: adicionada seção com clientes usados nos perfis (8 clientes reutilizáveis)
- [x] **Clientes não usados removidos**: Luana Costa, Thiago Santos, Renata Oliveira, Paulo Sérgio, Juliana Costa
- [x] **testes-lanchonete.md**: cabeçalho atualizado (3→1 cliente)
- [x] **testes-acougue.md**: cabeçalho atualizado (3→1 cliente)
- [x] **testes-mercado.md**: cabeçalho atualizado (removido Juliana Costa)

## Concluído (Cobertura de testes manuais — campos ausentes — 2026-07-19)
- [x] **Análise completa** de todos os campos das screens vs colunas dos arquivos .md de teste
- [x] **ClienteScreen**: adicionadas colunas Data Nasc., Gestante, Bebê, CEP, UF, Cidade, Bairro, Rua, Número + 4 novos casos (#24-#27)
- [x] **ProdutoScreen** (5 perfis): adicionadas colunas Garantia, Comissão, Observações, Imagem + 1 novo caso por perfil
- [x] **PDVScreen** (loja/petshop): adicionada coluna Nº Parcelas + 1 novo caso fiado por perfil
- [x] **ContasAReceberScreen** (loja/petshop): adicionadas colunas Data Recebimento, N° Doc, Observação + 1 novo caso por perfil
- [x] **ComprasAPagarScreen** (loja/petshop): adicionadas colunas Data Pagamento, N° Doc, Observação + 1 novo caso por perfil
- [x] **PreferenciasScreen**: adicionada coluna Select Impressora + 2 novos casos (#107-#108)
- [x] **PedidosScreen**: nova seção em testes-gerais.md com 4 casos (#109-#112)
- [x] **SugerirMelhoriaScreen**: nova seção com 3 casos (#113-#115)
- [x] **RelatarErroScreen**: nova seção com 3 casos (#116-#118)
- [x] **testes.md**: index atualizado com links para 3 novas screens (18→21 screens)

## Pendências
- [x] Testar fluxo completo: gerar MSI com `python scripts/create-msi-with-updater.py`, instalar e clicar "Buscar atualização" ✅
- [x] Testar desinstalação: MSI remove sem deixar processos presos ✅
- [ ] Publicar megalodonte-base modificado (com suporte a `run(args, ...)`) no maven local se ainda não estiver
- [ ] Configurar GitHub Releases para plics-sw com assets .msi
- [ ] **OrdemServicoScreen não tem controle de Status no formulário** — toda OS criada fica fixa em "Aberto"; não há como alterar o status (nem "Finalizado") pela tela, só aparece como coluna somente-leitura na tabela/detalhes. Decidir se isso é intencional ou se falta um select de Status no form (achado durante auditoria de cobertura dos `.md` de teste, 2026-07-27; os `.md` foram ajustados para não descrever Status como campo editável)
- [x] **PedidosScreen não tinha campo de busca nem excluir venda** ✅ (2026-07-31): `Components.searchInput` adicionado (busca por nome do cliente/forma de pagamento — `matchesSearch()` já existia, só faltava o input na tela); coluna "Cliente" agora mostra o nome em vez do `clienteId`; botão "Excluir venda selecionada" adicionado (`PDVService.excluirVenda()`, devolve estoque, apaga contas a receber vinculadas, reflete nos cards da Home)
- [x] **Eliminar o updater no build para a Microsoft Store** ✅ (2026-07-28): `scripts/create-msi.py` (sem `--add-launcher`/updater) passou a ser o build oficial pra Store — atualizado pra ficar em paridade com `create-msi-with-updater.py` (`--win-upgrade-uuid`, limpeza do `temp_dir`) e ganhou `--java-options -Dplics.microsoftStore=true`. `Main.isMicrosoftStore` lê essa property e `HomeScreen.menuBar()` nem monta o item "Buscar atualização" nesse caso (mesmo padrão do `Main.isFlatpak`, ver decisão de 2026-07-24)

## Concluído (Correções de publicação — publisher, Telegram, NPE, updater, PDV, versão — 2026-07-28)
- [x] **Publisher "Unknown" no instalador**: `scripts/config.py` ganhou a constante `VENDOR` (lida de `gradle.properties.appVendor`, que existia mas nenhum script lia) e `--vendor` em `run_jpackage()`. `appVendor` = "Coerente Inc."
- [x] **Notificação de erro no Telegram chegava como "null"**: `Main.handleAppError()` mandava só `t.getMessage()` (null em exceções comuns). Agora inclui tipo da exceção + 3 primeiras linhas do stack trace. `TelegramNotifier` perdeu o `parse_mode=Markdown` (arriscava rejeitar a mensagem por `_` em nomes de pacote) e ganhou `URLEncoder`
- [x] **NPE em `AuthScreenViewModel.isLicenseInvalid()` na 1ª execução**: `LICENCAS_PRODUCAO.contains(null)` em lista imutável lançava NPE — `licensa` é `null` em toda instalação nova (`V16__dados_padrao.sql` não preenche essa coluna), quebrando a splash. Reportado via Telegram já com o stack trace correto (graças à correção acima) durante teste de publicação na Microsoft
- [x] **Updater com mensagem amigável sem argumentos**: "Erro: args insuficientes" → "Updater deve ser aberto programaticamente, você não precisa executá-lo" (evita parecer crash pra quem abre o `.exe` manualmente, ex. revisor de loja de app)
- [x] **Build sem updater pra Microsoft Store**: `Main.isMicrosoftStore` (lida via `--java-options`) esconde "Buscar atualização" no menu quando o build é o `create-msi.py`
- [x] **Desconto e impressão alternativa no PDVScreen**: inputs "DESCONTO"/"TOTAL A PAGAR"; `PDVService.finalizarVenda()` ganhou overload com `desconto`, aplicado ao `totalLiquido` e às parcelas de venda fiada (usa valor líquido, não bruto); `EscPosPrinter.gerarBytesEscPos(PedidoModel, ...)` e botão "Imprimir (modo alternativo)" (só existiam pra `VendaModel`/`VendaMercadoriaScreen`)
- [x] **`Main.APP_VERSION` deixa de ser hardcoded**: `gradle.properties` ganhou `appVersion` (base) + `appPatch` (contador); versão final composta em runtime via `-Dplics.appVersion=...` (mesmo padrão do `isMicrosoftStore`). `scripts/bump_version.py` (novo) substitui a edição manual — `patch` incrementa, `release X.Y.Z` define base nova e zera o patch
- [x] **Testes**: 237 → 256 (`PreferenciasServiceTest`, `ContasPagarServiceTest` de `my_app.services` criados do zero; casos novos em `FornecedorServiceTest`, `ProdutoServiceTest`, `CompraServiceTest`, `PDVServiceTest`), 0 falhas

## Concluído (Padronização CRUD, validações nas Services e cobertura de testes manuais — 2026-07-27)
- [x] **`ViewModelScreenContract.populateFromModel()`** renomeado para `populateFieldsFromModel()` (nome antigo era ambíguo — na verdade popula os campos da UI a partir do model)
- [x] **`populateModelFromFields()`** criado como método abstrato inverso — cada uma das 12 ViewModels CRUD passou a ter exatamente um método com essa assinatura (sem parâmetros) para montar o Model a partir dos campos do formulário, substituindo `getModelFromFields`/`fillModelFromForm`/código inline que cada ViewModel fazia de um jeito diferente
- [x] **`ContratoTelaCrudV3`** atualizado para a nova nomenclatura
- [x] **Validações de CRUD movidas das ViewModels para as Services**: removidas checagens duplicadas (nome obrigatório, técnico/cliente/produto obrigatórios, valor de pagamento/recebimento) que já existiam nos `validar()`/`registrarPagamento()`/`registrarRecebimento()` das Services
- [x] **`ProdutoService.validar()`**: nova checagem "validade não pode ser anterior a hoje" (antes só existia na ViewModel). A checagem "perecível ⇒ validade obrigatória" continua na ViewModel — `ProdutoModel` não persiste um campo `perecivel`, só infere pela presença de `validade`, então essa regra não é reconstruível a partir do Model sozinho
- [x] **`PreferenciasService`**: ganhou `validar()` (login/senha obrigatórios quando credenciais habilitadas) — antes a validação só existia na ViewModel e a Service não tinha nenhuma
- [x] **`FornecedorModel.pessoaFisica`** (Migration V31): tipo de pessoa selecionado no form agora é persistido (antes só existia na UI e se perdia ao editar). `FornecedorService.validar()` passou a exigir CPF quando `pessoaFisica=true` e CNPJ quando `false`; fornecedores legados (`pessoaFisica=null`) mantêm o fallback antigo (CPF OU CNPJ válido)
- [x] **`CompraService`**: novo `salvar(CompraModel)` (além do existente `salvar(CompraDto)`, que agora delega pra ele) — permite que `ComprasScreenViewModel.populateModelFromFields()` retorne um `CompraModel` já pronto, como as demais ViewModels
- [x] **`ContasPagarService` (`my_app.services`)**: ganhou construtor `(Session)` para testabilidade; `gerarContasDeCompra()` (que já validava parcelas vazias) passou a ser a única validação dessa regra — a checagem duplicada em `ComprasScreenViewModel` foi removida
- [x] **Testes novos**: `PreferenciasServiceTest` (não existia), `ContasPagarServiceTest` em `my_app.services` (não existia), + casos novos em `FornecedorServiceTest` (pessoaFisica) e `ProdutoServiceTest` (validade no passado) e `CompraServiceTest` (salvar via Model) — suíte foi de 237 para 253 testes, 0 falhas
- [x] **Auditoria de cobertura dos `.md` de teste manual** (`testes-gerais.md` + 5 perfis): campos de todas as Screens comparados com as colunas testadas; adicionadas colunas/casos faltantes (Estoque Mínimo em ProdutoScreen nos 5 perfis, Marca/Fornecedor/Perecível/Validade em açougue e lanchonete, Tipo de Pessoa em FornecedorScreen, Observação em ComprasScreen, caso de logomarca, caso de "+ Criar cliente" e formas de pagamento no PDV, caso de logout em PreferenciasScreen)
- [x] **Incoerências corrigidas nos `.md`**: nova tabela "Dados de teste — Categorias" em `testes-gerais.md` (nenhuma categoria usada nos 5 perfis tinha cadastro prévio documentado); ordem de `#35`/`#36` trocada (duplicidade de categoria era testada depois do rename, quando o nome original já não existia mais); `ClienteScreen #9` trocado de "PetShop Amigo" (nunca cadastrado como Cliente) para "Moda & Estilo Ltda" (caso #2); passos de "Fluxo Completo" que recriavam cadastros já feitos (loja-de-roupas, açougue, petshop, mercado) reescritos para "reaproveitar cadastro"; `#66` duplicado em `testes-loja-de-roupas.md` renumerado; `#122`/`#123`/`#124` duplicados dentro do mesmo arquivo em `testes-petshop.md` renumerados; entidades nunca usadas removidas dos cabeçalhos de perfil (Fernanda Lima, Ricardo Gomes, Bebidas DF, LimpMax, Laticínios MG, Avícola Sul, Distribuidora de Carnes PR)
- [x] **Achados de UI registrados como pendência** (sem alterar código, por decisão explícita): OrdemServicoScreen sem controle de Status no form; PedidosScreen sem busca e mostrando ID em vez de nome do cliente — os `.md` foram ajustados para descrever o comportamento real

## Concluído (Correção validade produtos — INTEGER → REAL)
- [x] **Root cause**: Persism mapeia `INTEGER` SQLite → `Integer` Java; `Converter.convert()` tenta `Integer.parseInt("1797044400000")` e lança `NumberFormatException`
- [x] **V1 schema**: `validade INTEGER` → `validade REAL` (mesmo padrão de `vendas.data_validade` e `compras.data_validade`)
- [x] **V20 migration**: recria tabela `produtos` com `validade REAL` para bancos existentes
- [x] **`fillModelFromForm()`**: condicional `"Sim".equals(perecivelSelected.get())` antes de setar validade — evita data residual do DatePicker ao desmarcar "É perecível?"
- [x] **testes-loja-de-roupas.md**: cenários 14 e 15 documentados com erro — erro resolvido

## Concluído (CPF/CNPJ duplicado)
- [x] **ClienteRepository.buscarPorCpfCnpj()**: busca cliente por CPF/CNPJ (padrão `ProdutoRepository.buscarPorCodigoBarras`)
- [x] **ClienteService.validarCampos()**: valida que CPF/CNPJ não está em uso por outro cliente (criação e atualização)
- [x] **Migration V18**: UNIQUE INDEX condicional em `clientes.cpfCnpj` como safety net
- [x] **ClienteServiceTest**: 4 novos testes (duplicado save, único save, mesmo CPF update, CPF de outro update) — total 13 testes, 0 falhas

## Concluído (Correção edição Categoria)
- [x] **CategoriaScreenViewModel**: capturar `editando` antes do `Async.Run()` — evita race condition que criava nova categoria em vez de atualizar
- [x] **CategoriaService.validarNome()**: `long` → `Integer` + `!=` → `.equals()` — consistente com `CategoriaModel.id`
- [x] **CategoriaScreenViewModel.clearForm()**: adicionado `modoEdicao.set(false)` — consistente com demais VMs
- [x] **CategoriaScreenViewModel.handleAddOrUpdate()**: usar retorno de `categoriaService.salvar()` — garante objeto com ID na lista
- [x] **CategoriaScreenViewModelTest.deveAtualizarCategoria()**: teste que cobre o cenário de erro (editar nome cria nova categoria)
- [x] **AI_RULES.md**: adicionar regra para analisar `*.md` de testes no início da sessão

## Concluído (Correção Flyway + SQLite — 2026-06-30)
- [x] **V1 restaurado**: `validade REAL` → `INTEGER` (checksum original)
- [x] **V7 restaurado**: colunas `TEXT`/`INTEGER` originais (checksum original)
- [x] **V21 criado**: converte colunas de `compras` para `REAL` em bancos existentes
- [x] **`flyway.repair()` adicionado** antes de `migrate()` — resolve checksum mismatch para qualquer versão anterior
- [x] **Flyway removido de `DB.getPersismSession()`** — única chamada em `Main.initialize()`
- [x] **`CategoriaService` alinhado** ao padrão `DB.getPersismSession()`
- [x] **Documentação** atualizada (DECISIONS.md, CONTEXT.md, TODO.md)

## Concluído (Correção requestFocus — 2026-07-01)
- [x] **Root cause**: `InputRef.requestFocus()` chamava `getJavaFxNode().requestFocus()` no `StackPane` wrapper, não no `TextField`. `StackPane` com `focusTraversable = false` ignora `requestFocus()`.
- [x] **Fix**: `InputRef.requestFocus()` percorre `Parent.getChildrenUnmodifiable()`, encontra o `TextInputControl` e chama `requestFocus()` nele.
- [x] **Arquivo alterado**: `Components.java` (InputRef + import Node)

## Concluído (Correção vazamento de Sessions — 2026-06-30)
- [x] **`DB.closeAllSessions()` adicionado** no `Main.handleClose()` antes de `Platform.exit()`
- [x] **`reloadProdutos()` corrigido** em `ComprasScreenViewModel` e `VendaMercadoriaScreenViewModel`: reusa `produtoService` em vez de criar `new ProdutoService()` a cada chamada
- [x] **`ClienteService` alinhado** ao padrão `DB.getPersismSession()`
- [x] **`EmpresaService` alinhado** ao padrão `DB.getPersismSession()`
- [x] **Import não utilizado removido** de `CategoriaModel.java` (`org.jetbrains.annotations.NotNull`)

## Concluído (Porta da impressora em preferências — 2026-07-08)
- [x] **Migration V23**: `ALTER TABLE preferencias ADD COLUMN porta_impressora TEXT`
- [x] **PreferenciasModel**: campo `portaImpressora` com `@Column(name = "porta_impressora")`
- [x] **PreferenciasDto**: campo `portaImpressora`
- [x] **PreferenciasViewModel.load()**: restaura select da porta salva
- [x] **PreferenciasViewModel.salvar()**: salva `systemPortName` (parte antes de " - ") no model
- [x] **VendaMercadoriaScreenViewModel**: porta carregada de `PreferenciasService` (remove hardcoded `/dev/rfcomm0`)
- [x] **PDVScreenViewModel**: porta carregada de `PreferenciasService`; usa construtor com porta se definida, fallback caso contrário

## Concluído (Tabela `cores` criada — 2026-07-08)
- [x] **Migration V24**: `CREATE TABLE cores` com 16 cores pré-populadas
- [x] **CorModel, CorRepository, CorService** seguindo padrão existente
- [x] **ProdutoScreenViewModel**: `cores` carregados via `CorService` em `loadInicial()`
- [x] **ProdutoScreen**: checkboxes iteram sobre `vm.cores` em vez de `Data.listaCores`
- [x] **Arquivos criados**: `V24__criar_tabela_cores.sql`, `CorModel.java`, `CorRepository.java`, `CorService.java`
- [x] **Arquivos alterados**: `ProdutoScreenViewModel.java`, `ProdutoScreen.java`

## Concluído (Tabela de produtos não atualizava após CRUD — 2026-07-08)
- [x] **`handleClickMenuDelete()`**: adicionado `produtos.removeIf()` para remover da lista e atualizar a tabela
- [x] **`asyncAtualizar()`**: substituído recarregamento completo por `updateIf` com nova instância (padrão FornecedorScreenViewModel)
- [x] **`loadInicial()`**: adicionado `clear()` antes de `addAll()` para evitar duplicação
- [x] **Arquivo alterado**: `ProdutoScreenViewModel.java`

## Concluído (Propriedades cor, tamanho, modelo — 2026-07-04)
- [x] **`ProdutoModel`**: campos `cor`, `tamanho`, `modelo` (String) adicionados
- [x] **`ProdutoDto`**: campos `cor`, `tamanho`, `modelo` adicionados
- [x] **Migration V22**: `ALTER TABLE` para adicionar colunas
- [x] **`Data.java`**: listas `listaCores` (16 cores), `listaTamanhos` (PP, P, M, G, GG, XG, UN)
- [x] **`ProdutoScreenViewModel`**: states corSelected/tamanhoSelected/modelo; fillModelFromForm/clearForm/populateFromModel atualizados
- [x] **`ProdutoScreen`**: selects Cor/Tamanho e input Modelo no formulário; colunas na tabela; detalhes no modal
- [x] **Testes**: `ProdutoServiceTest` (+1), `ProdutoScreenViewModelTest` (+1)
- [x] **Arquivos `.md` de teste**: adicionadas colunas Cor, Tamanho, Modelo em todos os 5 perfis

## Melhoria futura: Verificar carregamento da porta da impressora
- Testar fluxo completo: selecionar porta Bluetooth na PreferenciasScreen, salvar, e verificar impressão no PDV e VendaMercadoriaScreen
- Considerar método `dispose()` nas ViewModels para fechar `PreferenciasService` ao sair da tela (evitar acúmulo de Sessions)

## Melhoria futura: Método dispose() no Megalodonte (onDispose)
- Adicionar hook dispose nas ViewModels via `ViewModelScreenContract` para fechar services ao sair da tela
- Impedir acúmulo de Sessions entre navegações

## Melhoria futura: Startup resiliente a erros
- `Main.initialize()` não deve lançar exceção — sempre abrir uma janela, mesmo que seja uma tela de erro
- Try-catch em cada etapa (Flyway, PreferenciasService, AppRoutes) com fallback para valores seguros
- Nova `InitializationErrorScreen` com mensagem do erro e botões "Tentar novamente" / "Sair"
- `DB.getPersismSession()` tratar falha do Flyway sem quebrar a aplicação
- ViewModels tratar services opcionais (null) em vez de lançar NPE

## Concluído (PDV)
- **PDVScreenViewModel**: NPE em `finalizarVenda()` corrigido — usa "CLIENTE PADRÃO" (id=1) quando nenhum cliente é selecionado em vendas à vista
- **PDVService**: `dataCriacao` adicionado aos itens do pedido para evitar `NOT NULL constraint failed: pedido_itens.dataCriacao`
- **PDVService**: `clienteId` alterado de `Long` para `Integer` (consistente com demais models)
- **PDVService**: adicionado construtor `PDVService(Session)` para testabilidade
- **PedidoModel.clienteId**: `Long` → `Integer` (alinha com VendaModel, OrdemServicoModel, ContaAreceberModel)
- **ContaAreceberService.gerarContasDeVenda()**: adicionado `dataCriacao` para evitar `NOT NULL constraint failed`
- **PDVServiceTest**: criado com 4 testes (cliente padrão, sem cliente, fiado, não fiado)

## Concluído (licença de teste)
- Migration V17: coluna `license` adicionada à tabela `preferencias`
- `PreferenciasModel`: campo `license` adicionado
- `AuthScreenViewModel`: aceita licença de teste até dia 3; salva licença no banco; exibe campo novamente se expirou
- `HomeScreenViewModel`: método `isLicensaTesteExpirada()` adicionado
- `HomeScreen.onMount()`: redireciona para AuthScreen se licença de teste expirou
- Testes: `AuthScreenViewModelTest` e `HomeScreenViewModelTest` atualizados

## Concluído (correção inconsistência licença de teste — 2026-07-04)
- `AuthScreenViewModel.entrar()`: substituído `day > 11` por `isLicensaTesteExpirada()` — alinhado com `load()`
- `HomeScreenViewModel.isLicensaTesteExpirada()`: `day > 11` → `day > 3`
- Docs atualizados (DECISIONS.md, CONTEXT.md, TODO.md)

## Concluído (empacotamento)
- `scripts/config.py`: adicionado `UPDATER_DIR` e `build_updater()`
- `scripts/create-msi.py`: updater.jar incluso no pacote; smoke test adicionado
- `scripts/create-deb.py`: updater.jar incluso no pacote
- `scripts/config.py`: múltiplas correções — `_java_home()`, `jdeps` dinâmico, `copy_natives` busca em `temp/bin/`, `ICON_PATH` condicional, caminhos absolutos JDK

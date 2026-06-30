# Decisões Arquiteturais

## 2026-06-30: Correção do Flyway + SQLite — migrations modificadas e inicialização duplicada

**Problema:** Ao lançar nova versão com novas migrations, Flyway lançava `FlywayValidateException`. A causa raiz foi dupla:

1. **Migrations V1 e V7 foram alteradas após já terem sido aplicadas** em bancos de produção:
   - V1: `validade INTEGER` → `REAL` (commit 95ad12b)
   - V7: `preco_compra TEXT` → `REAL`, `desconto_em_reais TEXT` → `REAL`, `data_compra TEXT` → `REAL`, `dataCriacao INTEGER` → `REAL` (commit cd9aca5)
   - Isso quebrou o checksum armazenado em `flyway_schema_history`, causando falha de validação ao executar `migrate()` em bancos existentes.

2. **Flyway era inicializado em dois lugares**: `Main.initialize()` e `DB.getPersismSession()`, fazendo `migrate()` rodar múltiplas vezes desnecessariamente.

**Decisão:**
1. **Restaurar V1 e V7 ao estado original** (checksum do primeiro commit a1163cd):
   - V1: `validade REAL` → `INTEGER` (V20 já existe para converter)
   - V7: colunas restauradas para `TEXT`/`INTEGER`
2. **Criar V21** para converter colunas da tabela `compras` de `TEXT`/`INTEGER` para `REAL` em bancos existentes (mesmo padrão do V20).
3. **Adicionar `flyway.repair()`** antes de `flyway.migrate()` no startup — recalcula checksums automaticamente para qualquer banco, independente de qual versão das migrations foi aplicada originalmente. É a recomendação oficial do Flyway para cenários de checksum mismatch.
4. **Remover inicialização do Flyway de `DB.getPersismSession()`** — Flyway é chamado uma única vez em `Main.initialize()`.
5. **Alinhar `CategoriaService`** ao padrão dos demais Services (`DB.getPersismSession()` em vez de `new Session(DB.production().connection())`).

**Boas práticas violadas (e agora corrigidas):**
- **Imutabilidade de migrations**: toda migration aplicada a qualquer banco de produção NUNCA deve ser modificada. Correções de schema devem ser feitas em NOVAS migrations.
- **Single responsibility**: `DB.getPersismSession()` misturava criação de sessão com migração de banco.
- **Consistência**: `CategoriaService` usava padrão diferente dos demais Services para obter a Session.

**Arquivos alterados:**
- `src/main/resources/flyway_migrations/V1__criar_produtos.sql` (restaurado original)
- `src/main/resources/flyway_migrations/V7__criar_compras.sql` (restaurado original)
- `src/main/resources/flyway_migrations/V21__fix_types_compras.sql` (novo)
- `src/main/java/my_app/Main.java` (+repair(), Flyway refatorado para var)
- `src/main/java/my_app/db/DB.java` (Flyway removido de getPersismSession())
- `src/main/java/my_app/db/services/CategoriaService.java` (alinhado ao padrão)
- `docs/DECISIONS.md` (esta entrada)
- `docs/CONTEXT.md` (atualizado)
- `docs/TODO.md` (atualizado)

---

## 2026-06-29: Produtos usados em vendas sem cadastro válido nos testes .md

**Problema:** Em `testes-loja-de-roupas.md`, produtos como Jaqueta (SKU003) e Calça Jeans (SKU004) eram usados em testes de VendaMercadoriaScreen, ComprasScreen e PDVScreen, mas só existiam em testes negativos (#15 SKU duplicado, #16 descrição vazia, #18 preço compra > venda) que não os criavam. Em `testes-mercado.md`, não havia seção ProdutoScreen — os produtos Arroz, Feijão e Óleo usados em PDVScreen não eram definidos.

**Decisão:**
1. `testes-loja-de-roupas.md`: SKU004 do teste #18 alterado para SKU006 (libera SKU004 para uso). Adicionados #143 (Jaqueta SKU003) e #144 (Calça Jeans SKU004) como cadastros válidos.
2. `testes-mercado.md`: Adicionada seção ProdutoScreen com #145 (Arroz 5kg), #146 (Feijão 1kg), #147 (Óleo 900ml) como cadastros válidos.
3. Demais perfis (PetShop, Lanchonete, Açougue) verificados — todos os produtos usados em testes já possuíam cadastro válido.

**Arquivos alterados:**
- `testes-loja-de-roupas.md` (#18 SKU004→SKU006, +2 linhas #143-#144)
- `testes-mercado.md` (+seção ProdutoScreen com #145-#147)

---

## 2026-06-29: Clientes de perfil sem cadastro válido em testes-gerais.md

**Problema:** Clientes listados nos headers de perfis (ex: "João Pedro" em Loja de Roupas) eram usados em testes de Venda, PDV, OS etc. mas não possuíam um cadastro válido definido em `testes-gerais.md` ClienteScreen. O usuário não tinha instrução de como criá-los antes de usá-los.

**Decisão:**
1. Adicionar 12 novos cadastros válidos (#12-#23) em `testes-gerais.md` ClienteScreen, um para cada cliente faltante, com dados consistentes (CPF único, celular, email).
2. Fornecedores não apresentam o mesmo problema — todos os fornecedores usados em testes já estão definidos em `testes-gerais.md` FornecedorScreen.

**Arquivos alterados:**
- `testes-gerais.md` (+12 linhas na tabela ClienteScreen)

---

## 2026-06-26: Correção do tipo da coluna `validade` em produtos (INTEGER → REAL)

**Problema:** Ao cadastrar produto perecível com data de validade (ex: 15/12/2026), o valor epoch millis `1797044400000L` não cabia em `Integer`. Persism mapeia `INTEGER` do SQLite para `Integer` Java, e `Converter.convert()` tenta `Integer.parseInt("" + longValue)`, lançando `NumberFormatException: For input string: "1797044400000"`. Mesmo erro ocorria ao desmarcar "É perecível?" se o DatePicker ainda tivesse data residual.

**Decisão:**
1. Coluna `validade` alterada de `INTEGER` para `REAL` na migration V1 e V20. `REAL` é o mesmo tipo usado por `vendas.data_validade` e `compras.data_validade`, que também armazenam epoch millis em `Long` — Persism mapeia `REAL` para `Double`, e `Converter.convert()` trata `Long → Double` sem erro.
2. `fillModelFromForm()` no `ProdutoScreenViewModel` só seta `validade` no model quando `perecivelSelected = "Sim"`. Impede que data residual do DatePicker seja enviada ao salvar com perecível desmarcado.

**Arquivos alterados:**
- `src/main/java/my_app/screens/produtoScreen/ProdutoScreenViewModel.java` (condicional no setValidade)
- `src/main/resources/flyway_migrations/V1__criar_produtos.sql` (validade INTEGER → REAL)
- `src/main/resources/flyway_migrations/V20__fix_validade_type_produtos.sql` (migração para bancos existentes)
- `testes-loja-de-roupas.md` (cenários 14, 15 marcados como corrigidos)

---

## 2026-06-25: Padronização dos testes de CategoriaScreen entre arquivos .md

**Problema:** `testes-gerais.md` misturava testes de validação genérica de CategoriaScreen com exemplos específicos de perfil (Bovinos/Açougue, Masculino/Loja), enquanto cada perfil também tinha sua própria seção CategoriaScreen — causando duplicação ("Masculino" em dois lugares) e lacuna (Açougue sem seção própria).

**Decisão:**
1. `testes-gerais.md#categoriascreen`: manter apenas testes de validação genérica (nome vazio, excluir com produtos, editar, duplicado).
2. Cada perfil mantém sua própria seção CategoriaScreen com testes "Cadastro válido" específicos.
3. Removido #33 (Bovinos) e #36 (Masculino) de `testes-gerais.md`.
4. Adicionado #33 (Bovinos) em `testes-acougue.md#categoriascreen` (seção que não existia).
5. `testes.md` atualizado para listar todos os perfis com CategoriaScreen.

**Arquivos alterados:**
- `testes-gerais.md` (removido #33/#36, reordenado #35→#33, #37→#34, #38→#35, #39→#36)
- `testes-acougue.md` (+seção CategoriaScreen com #33 Bovinos)
- `testes.md` (link CategoriaScreen agora listando todos os perfis)

---

## 2026-06-25: Validação de login/senha obrigatórios ao habilitar credenciais

**Problema:** Na PreferenciasScreen, ao selecionar "Sim" em "Habilitar credenciais", os campos login e senha não eram validados. Era possível salvar preferências com login vazio ou senha vazia, resultando em credenciais inválidas.

**Decisão:**
1. Adicionar método `validar()` em `PreferenciasViewModel` que retorna mensagem de erro ou `null`.
2. `salvar()` chama `validar()` antes do `Async.Run()` — se houver erro, exibe alerta e retorna sem persistir.
3. Método `validar()` é público para testabilidade direta sem dependência de JavaFX.
4. Testes: 4 novos casos — `validar()` retorna null com credenciais desabilitadas, retorna erro com login vazio, retorna erro com senha vazia, retorna null com ambos preenchidos.

**Arquivos alterados:**
- `src/main/java/my_app/screens/preferenciasScreen/PreferenciasViewModel.java` (+validar(), refatorado salvar())
- `src/test/java/my_app/screens/preferenciasScreen/PreferenciasViewModelTest.java` (+4 testes)
- `testes-gerais.md` (cenários 105, 106 marcados como OK)

---

## 2026-06-22: Correção de race condition na edição de Categoria

**Problema:** `CategoriaScreenViewModel.handleAddOrUpdate()` verificava `modoEdicao.get()` dentro de `Async.Run()`. Como `ContratoTelaCrudV3.handleAddOrUpdate()` redefine `modoEdicao = false` imediatamente após chamar `viewModel().handleAddOrUpdate()`, a async task via `modoEdicao = false` e executava o branch `else` (criar) em vez de `if` (atualizar). Resultado: editar uma categoria criava uma nova em vez de atualizar a existente.

**Decisão:**
1. Capturar `boolean editando = modoEdicao.get()` antes de `Async.Run()`, usar o valor capturado dentro da async task.
2. `validarNome()` em `CategoriaService`: parâmetro `long idAtual` → `Integer idAtual` e `!=` → `.equals()` para consistência com `CategoriaModel.id` (Integer).
3. `clearForm()` agora reseta `modoEdicao.set(false)` (consistente com FornecedorScreenViewModel e TecnicoScreenViewModel).
4. `categoriaService.salvar()`: usar retorno (`var salvo = ...`) em vez de ignorar, para garantir que o objeto com ID seja adicionado à lista.
5. Teste `CategoriaScreenViewModelTest.deveAtualizarCategoria` cobre o cenário: cria "Masculino", edita para "Moda Masculina", verifica 1 registro com nome atualizado.
6. `AI_RULES.md`: adicionada regra para analisar arquivos de testes `*.md` no início de cada sessão.

**Arquivos alterados:**
- `src/main/java/my_app/screens/categoriaScreen/CategoriaScreenViewModel.java`
- `src/main/java/my_app/db/services/CategoriaService.java`
- `src/test/java/my_app/screens/categoriaScreen/CategoriaScreenViewModelTest.java`
- `docs/AI_RULES.md`
- `testes-gerais.md` (registro do erro)

---

## 2026-06-22: Validação de CPF/CNPJ duplicado em ClienteService

**Problema:** Era possível cadastrar múltiplos clientes com o mesmo CPF/CNPJ, pois não havia validação de unicidade nem no banco nem na service.

**Decisão:**
1. Adicionar `buscarPorCpfCnpj()` em `ClienteRepository` seguindo o padrão de `ProdutoRepository.buscarPorCodigoBarras()`.
2. Adicionar validação em `ClienteService.validarCampos()`: se `cpfCnpj` não estiver vazio, verificar se outro cliente já possui o mesmo valor. Na criação (`model.getId() == null`), qualquer existente bloqueia. Na atualização, permite o mesmo cliente (mesmo ID) mas bloqueia se pertencer a outro.
3. Migration V18: `UNIQUE INDEX` condicional (`WHERE cpfCnpj IS NOT NULL AND cpfCnpj != ''`) como safety net no banco, permitindo múltiplos clientes sem CPF/CNPJ.
4. Testes: 4 novos casos — CPF duplicado no save, CPF único no save, manter o mesmo CPF no update, CPF de outro cliente no update.

**Arquivos alterados:**
- `src/main/java/my_app/db/repositories/ClienteRepository.java` (adicionado `buscarPorCpfCnpj`)
- `src/main/java/my_app/db/services/ClienteService.java` (validação, cast para ClienteRepository, imports limpos)
- `src/main/resources/flyway_migrations/V18__add_unique_cpf_cnpj_clientes.sql` (novo)
- `src/test/java/my_app/db/services/ClienteServiceTest.java` (4 novos testes)

---

## 2026-06-15: ShowPopupForced — popup modal sempre-no-topo para ações destrutivas

**Problema:** Após "Excluir todos os dados", o `ShowPopup` existente era auto-hide e não bloqueava o usuário. Era necessário um popup que ficasse forçadamente sobre todas as janelas, com mensagem clara e botão de ação (fechar app).

**Decisão:** Criar `Components.ShowPopupForced(context, message, buttonText, onButtonClick)`:
1. Stage com `Modality.APPLICATION_MODAL` + `setAlwaysOnTop(true)` — bloqueia qualquer interação até o botão ser clicado.
2. `StageStyle.UTILITY` — janela minimalista, sem decoração extra.
3. Recebe callback para a ação do botão (no caso, `Platform::exit`).
4. `deletarTodosDados()` simplificado: remove re-insert de defaults, Session nova e event publishing, pois o app será fechado e tudo será recriado na próxima inicialização.

**Arquivos alterados:**
- `src/main/java/my_app/domain/components/Components.java` (+ShowPopupForced)
- `src/main/java/my_app/screens/preferenciasScreen/PreferenciasViewModel.java` (simplificado deletarTodosDados)

---

## 2026-06-15: Navegação no onMount() da HomeScreen com Platform.runLater

**Problema:** `HomeScreen.onMount()` chamava `ctx.navigate("entrar-com-credenciais")` diretamente quando a licença de teste expirava. Como `onMount()` é executado dentro de `Router.resolveWithStage()` (antes de `render()`), e o retorno de `resolveWithStage()` é consumido por `Context.useView()` — que seta o scene do stage — a navegação era anulada: o `useView()` sobrescrevia o scene da AuthScreen com o scene da HomeScreen.

**Decisão:** Envolver `ctx.navigate()` em `Platform.runLater()` para adiar a navegação para depois do pulse atual do JavaFX, permitindo que o scene da HomeScreen seja criado primeiro e depois substituído pela AuthScreen.

**Arquivo alterado:** `src/main/java/my_app/screens/homeScreen/HomeScreen.java:44-46`

---

## 2026-06-15: Opção "Excluir todos os dados" nas Preferências

**Problema:** Não havia forma de limpar todos os dados do sistema sem recriar o banco manualmente.

**Decisão:** Adicionar botão "Excluir todos os dados" na PreferenciasScreen, com fundo vermelho (`#dc2626`) para indicar ação destrutiva. A exclusão:
1. Usa `Components.ShowAlertAdvice` como confirmação
2. Fecha o service local (`preferenciasService.close()`)
3. Chama `DB.closeAllSessions()` que fecha todas as Sessions abertas (inclusive as 6 do HomeScreenViewModel)
4. Exclui o arquivo `erp.db` do disco via `DB.resolveDbPath()`
5. Após exclusão, `DB.limparBanco()` executa `clean_db.sql` (DELETE de todas as 16 tabelas + re-insert dos dados padrão da V16). Recria o service, publica `DadosFinanceirosAtualizadosEvent` no EventBus (HomeScreen recalcula os cards zerados) e chama `load()`. Tudo no mesmo stage spawnado, sem navegação.

**Problema resolvido:** O SQLite no Windows utiliza lock de arquivo por conexão. Como cada Service cria uma Session independente, era necessário fechar todas antes de deletar o arquivo. O `DB.java` agora rastreia todas as Sessions criadas via `getPersismSession()` em uma lista estática sincronizada.

**Arquivos alterados:**
- `src/main/java/my_app/screens/preferenciasScreen/PreferenciasViewModel.java`
- `src/main/java/my_app/screens/preferenciasScreen/PreferenciasScreen.java`

---

## 2026-06-15: Adicionado "g" (gramas) às unidades de medida

**Problema:** O perfil Açougue necessita de produtos vendidos em gramas (ex: bacon fatiado, linguiça), mas a lista de unidades não incluía "g".

**Decisão:** Adicionar "g" à `unidadesDeMedidaList` em `Data.java`, entre "KG" e "ml", mantendo a ordem alfabética relativa.

**Arquivo alterado:** `src/main/java/my_app/domain/Data.java:18`

---

## 2026-06-10: Updater inline (mesmo JAR) via --add-launcher

**Problema:** O updater do plics-sw era referenciado como subprojeto separado (`plics-sw-updater`), mas nunca foi implementado. O app-v1 do projeto `testes-atualizacao-app` demonstrou que é possível ter o updater dentro do próprio JAR usando `--add-launcher` do jpackage.

**Decisão:** Copiar a implementação do `testes-atualizacao-app/app-v1` para o plics-sw:
1. Pacote `my_app.updater` com Main, HomeScreen, HomeScreenViewModel (batch script com taskkill + msiexec + msg)
2. `my_app.infra.UpdaterService` para download do MSI via GitHub Releases
3. `Main.java`: adicionado `public static void main(String[] args)` que passa args para `MegalodonteApp.run(args, ...)`
4. Menu "Buscar atualização" descomentado no HomeScreen
5. Novos scripts `create-msi-with-updater.py` e `create-deb-with-updater.py` (não alteram os originais)

**Arquivos criados:**
- `src/main/java/my_app/updater/Main.java`
- `src/main/java/my_app/updater/HomeScreen.java`
- `src/main/java/my_app/updater/HomeScreenViewModel.java`
- `src/main/java/my_app/infra/UpdaterService.java`
- `scripts/updater_config.py`
- `scripts/create-msi-with-updater.py`
- `scripts/create-deb-with-updater.py`

**Arquivos modificados:**
- `src/main/java/my_app/Main.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreen.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreenViewModel.java`

---

## 2026-06-10: App principal fecha após lançar updater

**Problema:** Ao clicar "Buscar atualização", o app principal lançava o updater (`Plics SW Updater.exe`) mas não fechava. O updater ficava travado em "Aguardando aplicação fechar" porque usava `ph.onExit().join()` esperando o PID do app principal morrer, e isso nunca acontecia.

**Decisão:** Adicionar `System.exit(0)` logo após `pb.start()` bem-sucedido no método `HomeScreenViewModel.update()`. O updater roda em processo separado, portanto não é afetado pelo término do processo pai.

**Arquivo alterado:** `src/main/java/my_app/screens/homeScreen/HomeScreenViewModel.java:276`

---

## 2026-06-10: Updater com suporte a Linux (pkexec + dpkg + notify-send)

**Problema:** O updater era exclusivo Windows — usava `.bat`, `taskkill`, `msiexec`, `msg`, `cmd /c` e procurava por `.exe`. No Linux, o fluxo de update não funcionava.

**Decisão:**
1. `UpdaterService.findMsiAsset()` → `findPackageAsset()`: detecta SO e busca `.msi` (Windows) ou `.deb` (Linux) nos assets da release
2. `HomeScreenViewModel.discoverUpdaterPath()`: no Linux, busca launcher sem `.exe` em `/opt/plics-sw/`, `/usr/lib/plics-sw/`, `/usr/local/lib/plics-sw/`
3. `updater/HomeScreenViewModel`: bifurca por SO — Windows mantém `.bat` original, Linux gera `.sh` com `pkill`, `pkexec dpkg -i` (PolicyKit), `notify-send`
4. GitHub Releases precisa conter ambos `.msi` e `.deb`

**Arquivos alterados:**
- `src/main/java/my_app/infra/UpdaterService.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreenViewModel.java`
- `src/main/java/my_app/updater/HomeScreenViewModel.java`

---

## 2026-06-10: Updater sai graciosamente para liberar handles de arquivo

**Problema:** Após o app principal fechar, o updater criava o batch script e era morto via `taskkill /f`. O kill forçado não dava chance ao JVM de liberar handles dos DLLs carregados do runtime/bin (ex: `api-ms-win-core-console-l1-2-0.dll`), causando erro "Error writing to file" no msiexec.

**Decisão:**
1. Updater chama `System.exit(0)` após lançar o batch script — saída graciosa libera todos os handles.
2. Removido `"Plics SW Updater.exe"` do `taskkill` no batch script — evita race condition entre kill forçado e `System.exit`.

**Arquivo alterado:** `src/main/java/my_app/updater/HomeScreenViewModel.java:64,89`

---

## 2026-06-08: PDV — cliente padrão para vendas à vista e dataCriacao em itens

**Problema 1:** Ao clicar "Finalizar Venda" sem selecionar cliente (venda à vista), `clienteId` era `null` causando NPE em `Long.valueOf(clienteId)`.

**Problema 2:** Ao corrigir o problema 1, o fluxo avançava e revelava que `dataCriacao` não era setado nos `PedidoItemModel`, causando `NOT NULL constraint failed: pedido_itens.dataCriacao`.

**Decisão:**
1. Quando `clienteId` é `null` e a venda não é fiada, usar `1` (ID do "CLIENTE PADRÃO" inserido pela migration V16).
2. Adicionar `itemModel.setDataCriacao(LocalDateTime.now())` no loop de itens em `PDVService.finalizarVenda()`.
3. Alinhar `PedidoModel.clienteId` de `Long` para `Integer` (consistente com `VendaModel`, `OrdemServicoModel`, `ContaAreceberModel`). SQLite retorna `INTEGER` como `Integer` no Java, e Persism não faz conversão automática `Integer → Long`.
4. Adicionar `model.setDataCriacao(LocalDateTime.now())` em `ContaAreceberService.gerarContasDeVenda()` — mesma causa do problema 2, `dataCriacao` não era setado nas contas a receber.
5. Adicionar construtor `PDVService(Session)` para permitir testes com session em memória.
6. Criar `PDVServiceTest` com 4 testes: cliente padrão, cliente nulo, fiado gera contas, não fiado não gera contas.

**Arquivos alterados:**
- `src/main/java/my_app/db/models/PedidoModel.java:20`
- `src/main/java/my_app/db/dto/PedidoDto.java:6`
- `src/main/java/my_app/services/PDVService.java:26,70`
- `src/main/java/my_app/screens/pdvScreen/PDVScreenViewModel.java:211`
- `src/main/java/my_app/db/services/ContaAreceberService.java:140`
- `src/test/java/my_app/services/PDVServiceTest.java` (novo)

---

## 2026-05-31: Remoção de interfaces ContratoTelaCrud depreciadas

**Problema:** ContratoTelaCrud e ContratoTelaCrudV2 estavam marcadas como @Deprecated e sem nenhuma referência no código. Apenas ContratoTelaCrudV3 é utilizada atualmente.

**Decisão:** Remover as duas interfaces obsoletas para simplificar o código.

---

## 2026-05-31: FeedbackViewModel compartilhado

**Problema:** RelatarErroScreen e SugerirMelhoriaScreen tinham código quase idêntico (State, ComputedState, lógica de envio via Telegram), violando o princípio DRY.

**Decisão:** Criar `FeedbackViewModel` com a lógica compartilhada de formulário de feedback (estados `isSending`, `content`, `btnText` e método `send()`). Ambos os Screens agora instanciam o mesmo ViewModel, diferindo apenas no label do textarea.

**Benefícios:**
- Elimina duplicação de código entre duas screens
- Cada screen agora possui sua ViewModel correspondente (conforme regra do projeto)

---

## 2026-05-31: Eventos movidos para core/events com EntityEvent<T> genérico

**Problema:** ClienteEvents, TecnicoEvents e ProdutoEvents repetiam exatamente a mesma estrutura (Criado, Editado, Excluido records), além de ProdutoEvents conter um bug (importava TecnicoModel).

**Decisão:** Criar `EntityEvent<T>` genérico em `core/events` com factory methods estáticos (`criado`, `editado`, `excluido`) e método `is(EventType)` para pattern matching. Isso elimina a necessidade de uma classe de eventos por entidade.

**Benefícios:**
- Elimina duplicação de código (3 classes ~12 linhas cada → 1 classe ~30 linhas)
- Evita bugs de copy-paste (como o TecnicoModel em ProdutoEvents)
- Fácil de estender para novas entidades sem criar novas classes de evento
- Package `events` movido para dentro de `core` (organização consistente)

---

## 2026-05-31: Updater incluso no pacote de distribuição

**Problema:** O plics-sw-updater era um projeto separado sem integração com os scripts de empacotamento, impossibilitando que o instalador já incluísse o utilitário de atualização.

**Decisão:** Adicionar `build_updater()` em `scripts/config.py` que compila o `plics-sw-updater` e copia o JAR resultante para `temp_dir/updater.jar`. Ambos os scripts (`create-msi.py` e `create-deb.py`) chamam essa função antes do jlink/jpackage. O smoke test (execução da aplicação gerada) foi adicionado ao `create-msi.py` seguindo o mesmo padrão já existente no `create-deb.py`.

---

## 2026-05-31: Correções no empacotamento MSI

**Problema:** O script `create-msi.py` falhava com múltiplos erros:
1. `JAVA_HOME` com `\bin` no final quebrava o Gradle
2. `--java-options` com aspas literais inválidas para o `jpackage`
3. Ícone `.png` rejeitado pelo MSI (exige `.ico`)
4. Runtime mínimo (5 módulos fixos) causava "Failed to Launch JVM" por falta de módulos
5. `copy_natives` procurava DLLs em `temp/lib/` mas estavam em `temp/bin/`
6. `jdeps`, `jlink` e `jpackage` não estavam no PATH

**Decisão:**
1. Criada função `_java_home()` que remove `\bin` do `JAVA_HOME` automaticamente
2. `--java-options` sem aspas literais
3. `ICON_PATH` usa `.ico` no Windows
4. `run_jlink` usa `jdeps --print-module-deps` para detectar módulos necessários dinamicamente, mais fallback com módulos essenciais (`java.logging`, `java.xml`, etc.)
5. `copy_natives` busca DLLs em `temp/bin/` primeiro, com fallback para `temp/lib/`
6. Todos os comandos JDK usam caminho absoluto via `_java_home()`

**Benefícios:**
- Geração do MSI funciona sem dependências de PATH ou configuração manual
- Runtime inclui todos os módulos que a aplicação realmente precisa
- Aplicação instalada inicia sem "Failed to Launch JVM"

---

## 2026-06-04: Correção de column name mismatch em ComprasRepository

**Problema:** A query `SELECT * FROM compras WHERE data_criacao BETWEEN ? AND ?` usava snake_case `data_criacao`, mas a coluna foi criada no migration V7 como `dataCriacao` (camelCase). Isso causava `SQLException: no such column: data_criacao` ao carregar a HomeScreen.

**Decisão:** Alterar `data_criacao` para `dataCriacao` no SQL raw de `ComprasRepository.somarComprasPorPeriodo()`.

**Arquivo alterado:** `src/main/java/my_app/db/repositories/ComprasRepository.java:26`

---

## 2026-06-04: Alinhamento de tipos entre model e migration (Compras)

**Problema:** A migration V7 usava `dataCriacao INTEGER`, `data_compra TEXT`, `preco_compra TEXT`, `desconto_em_reais TEXT`. Persism mapeia `INTEGER` do SQLite para `Integer` em Java (não `long`), e `TEXT` para `String` (não `long`/`BigDecimal`). Isso causava `NumberFormatException` e `IllegalArgumentException` em testes e potencialmente em produção.

**Decisão:**
1. Migration V7: `dataCriacao INTEGER` → `REAL`, `data_compra TEXT` → `REAL`, `preco_compra TEXT` → `REAL`, `desconto_em_reais TEXT` → `REAL`
2. `CompraModel.fornecedorId` alterado de `Long` para `Integer` (consistente com `FornecedorModel`, `ProdutoModel`, `ContasPagarModel`)
3. `CompraDto.fornecedorId` alterado de `Long` para `Integer`
4. `ContasPagarService`: simplificado `setFornecedorId` (sem `.intValue()`)

**Arquivos alterados:**
- `src/main/resources/flyway_migrations/V7__criar_compras.sql`
- `src/main/java/my_app/db/models/CompraModel.java`
- `src/main/java/my_app/db/dto/CompraDto.java`
- `src/main/java/my_app/services/ContasPagarService.java`
- `src/main/java/my_app/screens/comprasScreen/ComprasScreenViewModel.java`

---

## 2026-06-04: WelcomeScreen movido para pacote próprio com ViewModel

**Problema:** WelcomeScreen estava solto em `my_app.screens` sem ViewModel, violando a convenção do projeto.

**Decisão:** Criar pacote `my_app.screens.welcomeScreen`, mover `WelcomeScreen` para dentro dele com uma `WelcomeScreenViewModel` (minimalista, sem lógica de negócio).

**Arquivos alterados:**
- `src/main/java/my_app/screens/WelcomeScreen.java` → movido para `welcomeScreen/`
- `src/main/java/my_app/screens/welcomeScreen/WelcomeScreenViewModel.java` (novo)
- `src/main/java/my_app/core/AppRoutes.java` (import atualizado)

---

## 2026-06-04: PedidosScreenViewModel refatorado para usar PedidoItemService

**Problema:** PedidosScreenViewModel usava `PedidoItemRepository` diretamente e geria `Session` manualmente, violando o padrão Service + Repository.

**Decisão:** Substituir `PedidoItemRepository` + `Session` por `PedidoItemService`. Adicionado método `listarPorPedido()` em `PedidoItemService`.

**Arquivos alterados:**
- `src/main/java/my_app/db/services/PedidoItemService.java` (adicionado `listarPorPedido`)
- `src/main/java/my_app/screens/pedidosScreen/PedidosScreenViewModel.java` (refatorado)

---

## 2026-06-08: Licença de teste com validade até dia 11

**Problema:** Usuários de teste usavam a licença `QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ` sem prazo de validade, e não havia mecanismo para expirá-la.

**Decisão:**
1. `AuthScreenViewModel`: aceitar tanto a licença de produção (`984e2bb76c7b627641b6b7dc080f8e23`) quanto a de teste (`QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ`). A de teste só é válida até o dia 11 do mês (inclusive).
2. Salvar a licença utilizada no campo `licensa` da tabela `preferencias` (Migration V17).
3. `AuthScreenViewModel.load()`: se a licença salva for a de teste e estiver expirada, exibir o campo de licença novamente para o usuário digitar uma válida.
4. `HomeScreenViewModel.isLicensaTesteExpirada()`: método público que consulta a licença salva e a data atual.
5. `HomeScreen.onMount()`: redirecionar para `AuthScreen` se a licença de teste expirou.

**Arquivos alterados:**
- `src/main/resources/flyway_migrations/V17__add_licensa_to_preferencias.sql` (novo)
- `src/main/java/my_app/db/models/PreferenciasModel.java`
- `src/main/java/my_app/screens/authScreen/AuthScreenViewModel.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreen.java`
- `src/main/java/my_app/screens/homeScreen/HomeScreenViewModel.java`
- `src/test/java/my_app/screens/authScreen/AuthScreenViewModelTest.java`
- `src/test/java/my_app/screens/homeScreen/HomeScreenViewModelTest.java`

---

## 2026-06-04: Testes de repository para ComprasRepository

**Decisão:** Criar `ComprasRepositoryTest` com 6 testes (salvar, listar, atualizar, excluirById, buscarById, somarComprasPorPeriodo) seguindo o padrão dos demais testes do projeto.

**Arquivo criado:** `src/test/java/my_app/db/repositories/ComprasRepositoryTest.java`

---

## 2026-06-04: Logging estruturado com SLF4J + appender de erros separado

**Problema:** Apenas 1 arquivo de produção usava SLF4J. Os demais usavam `e.printStackTrace()` ou `System.out/err`, sem persistência de logs em arquivo.

**Decisão:**
1. `logback.xml`: adicionado appender `ERROR-FILE` que grava apenas eventos ERROR em `error.log` com retenção de 30 dias
2. Substituídos todos os `printStackTrace` (11 arquivos) por `log.error("mensagem", e)`
3. Adicionado Logger em `DB.java` para registrar conexões e erros de driver

**Arquivos alterados:**
- `src/main/resources/logback.xml`
- `src/main/java/my_app/db/DB.java`
- `src/main/java/my_app/domain/ContratoTelaCrudV3.java`
- `src/main/java/my_app/hotreload/HotReload.java`
- `src/main/java/my_app/hotreload/Reloader.java`
- `src/main/java/my_app/screens/authScreen/AuthScreenViewModel.java`
- `src/main/java/my_app/screens/comprasScreen/ComprasScreenViewModel.java`
- `src/main/java/my_app/screens/pdvScreen/PDVScreenViewModel.java`
- `src/main/java/my_app/screens/pedidosScreen/PedidosScreenViewModel.java`
- `src/main/java/my_app/screens/vendaScreen/VendaMercadoriaScreenViewModel.java`
- `src/main/java/my_app/utils/TrayManager.java`
# TODO

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

## Pendências
- [x] Testar fluxo completo: gerar MSI com `python scripts/create-msi-with-updater.py`, instalar e clicar "Buscar atualização" ✅
- [x] Testar desinstalação: MSI remove sem deixar processos presos ✅
- [ ] Publicar megalodonte-base modificado (com suporte a `run(args, ...)`) no maven local se ainda não estiver
- [ ] Configurar GitHub Releases para plics-sw com assets .msi

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
- Migration V17: coluna `licensa` adicionada à tabela `preferencias`
- `PreferenciasModel`: campo `licensa` adicionado
- `AuthScreenViewModel`: aceita licença de teste até dia 11; salva licença no banco; exibe campo novamente se expirou
- `HomeScreenViewModel`: método `isLicensaTesteExpirada()` adicionado
- `HomeScreen.onMount()`: redireciona para AuthScreen se licença de teste expirou
- Testes: `AuthScreenViewModelTest` e `HomeScreenViewModelTest` atualizados

## Concluído (empacotamento)
- `scripts/config.py`: adicionado `UPDATER_DIR` e `build_updater()`
- `scripts/create-msi.py`: updater.jar incluso no pacote; smoke test adicionado
- `scripts/create-deb.py`: updater.jar incluso no pacote
- `scripts/config.py`: múltiplas correções — `_java_home()`, `jdeps` dinâmico, `copy_natives` busca em `temp/bin/`, `ICON_PATH` condicional, caminhos absolutos JDK

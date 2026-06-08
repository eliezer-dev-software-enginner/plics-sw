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

## Pendências
- (Nenhuma pendência identificada)

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

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

## Concluído (empacotamento)
- `scripts/config.py`: adicionado `UPDATER_DIR` e `build_updater()`
- `scripts/create-msi.py`: updater.jar incluso no pacote; smoke test adicionado
- `scripts/create-deb.py`: updater.jar incluso no pacote
- `scripts/config.py`: múltiplas correções — `_java_home()`, `jdeps` dinâmico, `copy_natives` busca em `temp/bin/`, `ICON_PATH` condicional, caminhos absolutos JDK

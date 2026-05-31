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

## Pendências
- Implementar testes de repository para telas que ainda não possuem (ex: CompraRepository)
- Refatorar telas que ainda usam padrão antigo (se houver)

## Concluído (empacotamento)
- `scripts/config.py`: adicionado `UPDATER_DIR` e `build_updater()`
- `scripts/create-msi.py`: updater.jar incluso no pacote; smoke test adicionado
- `scripts/create-deb.py`: updater.jar incluso no pacote

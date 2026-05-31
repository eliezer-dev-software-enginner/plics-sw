# TODO

## Concluído
- PreferenciasScreen refatorada (ViewModel, Service, Repository, Test)
- FornecedorScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V4)
- ProdutoScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V1)
- TecnicoScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V12)
- PedidosScreen refatorada (Model, Repository, Service, ViewModel, Screen, Test, Migration V15)
- Eventos movidos para core/events com EntityEvent<T> genérico (elimina repetição ClienteEvents/TecnicoEvents/ProdutoEvents)

## Pendências
- Refatorar AuthScreen, Main, WelcomeScreen para usar novo PreferenciasRepository
- Refatorar OrdemServicoScreen, RelatarErroScreen, SugerirMelhoriaScreen, AuthScreen, InfoUpdateScreen seguindo padrão ViewModel
- Implementar mais testes de repository para telas ainda não refatoradas

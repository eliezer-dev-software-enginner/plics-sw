# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)
- Persism (ORM) + SQLite
- Flyway (migrations)
- Padrão: Screen + ViewModel + Service + Repository
- Events movidos para core/events com EntityEvent<T> genérico
- plics-sw-updater: subprojeto separado, empacotado junto com a aplicação (updater.jar)

## Scripts de empacotamento
- `scripts/config.py`: funções compartilhadas (`build_updater()`, `run_gradle()`, `run_jlink()`, `run_jpackage()`, etc.)
- `scripts/create-msi.py`: gera instalador Windows (.msi) com updater incluso + smoke test
- `scripts/create-deb.py`: gera instalador Linux (.deb) com updater incluso + smoke test

## Última alteração
- Fix: `ComprasRepository.java` — query usava `data_criacao` (snake_case) mas a coluna no banco é `dataCriacao` (camelCase)
- Fix: Migration V7 — tipos das colunas `dataCriacao` (INTEGER→REAL), `data_compra` (TEXT→REAL), `preco_compra`/`desconto_em_reais` (TEXT→REAL) para alinhar com o que Persism espera
- Fix: `CompraModel.fornecedorId` alterado de `Long` para `Integer` (consistente com `FornecedorModel` e demais models)
- `WelcomeScreen` movido para pacote `welcomeScreen` com `WelcomeScreenViewModel` criada
- `PedidosScreenViewModel` refatorado: substitui `PedidoItemRepository` + Session manual por `PedidoItemService`
- Adicionado `ComprasRepositoryTest` com 6 testes (CRUD + somarComprasPorPeriodo)
- Adicionado `PedidoItemService.listarPorPedido()`

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
- welcomeScreen (ViewModel criada)

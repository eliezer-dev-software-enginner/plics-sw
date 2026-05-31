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
- Adicionado `build_updater()` em `scripts/config.py` que compila o plics-sw-updater
- `create-msi.py` e `create-deb.py` agora empacotam updater.jar junto com app.jar
- Smoke test (execução da aplicação gerada) adicionado ao `create-msi.py`
- Steps renomeados de 5 para 6 nos scripts

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)

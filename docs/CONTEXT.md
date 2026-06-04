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
- Fix: `ComprasRepository.java` — query usava `data_criacao` (snake_case) mas a coluna no banco é `dataCriacao` (camelCase) conforme migration V7
- Correções `scripts/config.py`: `_java_home()` sanitiza `JAVA_HOME`; `run_jlink` usa `jdeps` para detectar módulos; `copy_natives` busca DLLs em `temp/bin/`; `run_jpackage` sem `--java-options` desnecessário; comandos JDK usam caminho absoluto
- `ICON_PATH` usa `.ico` no Windows (MSI rejeita `.png`)
- MSI agora gera e executa corretamente sem "Failed to Launch JVM"

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)

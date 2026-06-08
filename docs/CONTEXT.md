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
- **Licença de teste**: `AuthScreenViewModel` agora aceita a licença de teste `QHd3fuX3mtoCo1gd9dmeKGTEBrxUJ31MxJ` válida até o dia 11 do mês (inclusive). Após essa data, o acesso é bloqueado com mensagem "Licença de teste expirada".
- **Redirecionamento**: `HomeScreen.onMount()` verifica se a licença salva é a de teste expirada e redireciona para `AuthScreen`.
- **Campo `licensa`**: Adicionado à `PreferenciasModel` e à migration V17 para persistir a licença usada no login.
- **Testes**: Adicionados testes para `AuthScreenViewModel` (licença produção, teste, nula/vazia) e `HomeScreenViewModel` (licença expirada/não expirada).

## Screens refatoradas
- categoriaScreen, clienteScreen, comprasScreen, empresaScreen, fornecedorScreen
- homeScreen, pdvScreen, comprasAPagarScreen, contasAReceberScreen
- pedidosScreen, produtoScreen, vendaScreen
- preferenciasScreen, tecnicoScreen
- RelatarErroScreen, SugerirMelhoriaScreen, InfoUpdateScreen (ViewModel adicionadas)
- welcomeScreen (ViewModel criada)
